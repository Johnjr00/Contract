package com.thecontract.tv.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import com.thecontract.core.engine.GameEngine
import com.thecontract.core.server.ContractServer
import com.thecontract.core.protocol.Narration
import com.thecontract.core.server.SessionManager
import com.thecontract.tv.ContractLog
import com.thecontract.tv.R
import com.thecontract.tv.ServerHolder
import com.thecontract.tv.data.RoomStateStore
import com.thecontract.tv.net.AndroidNetworkMonitor
import com.thecontract.tv.ui.ChimePlayer
import com.thecontract.tv.ui.NarrationPlayer
import com.thecontract.tv.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Hosts the embedded server for the whole life of a session (section 5).
 *
 * Running the server in a foreground service is what makes the rest of the specification
 * achievable: the game survives the activity being recreated, the app being backgrounded and
 * the launcher reclaiming the task. State is written through on every mutation and again
 * whenever the service is shutting down, so process death costs at most the current timer's
 * countdown — which is deliberately restored paused rather than silently continued.
 */
class ContractService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tickJob: Job? = null
    private var chimeJob: Job? = null
    private var chime: ChimePlayer? = null
    private var narrationJob: Job? = null
    private var narrator: NarrationPlayer? = null
    private var server: ContractServer? = null
    private var monitor: AndroidNetworkMonitor? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var manager: SessionManager

    companion object {
        const val ACTION_START = "com.thecontract.tv.START"
        const val ACTION_STOP = "com.thecontract.tv.STOP"
        const val EXTRA_RESUME = "resume_unfinished"

        private const val CHANNEL_ID = "session"
        private const val NOTIFICATION_ID = 1001
        private const val TICK_MS = 500L
        private const val RUNNING_STATE = "RUNNING"
        private const val COMPLETED_STATE = "COMPLETED"

        fun start(context: Context, resumeUnfinished: Boolean = false) {
            val intent = Intent(context, ContractService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_RESUME, resumeUnfinished)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.startService(Intent(context, ContractService::class.java).apply { action = ACTION_STOP })
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForegroundCompat()

        chime = ChimePlayer(applicationContext)
        narrator = NarrationPlayer(applicationContext).also { it.warmUp() }

        val store = RoomStateStore(applicationContext)
        manager = SessionManager(store, GameEngine())
        manager.tvListener = SessionManager.TvListener { ServerHolder.publish(it) }
        ServerHolder.attach(manager)

        scope.launch {
            // AndroidNetworkMonitor.start() synchronously builds and broadcasts the first TV
            // view, which reads saved session/contract state from Room — must not run on the
            // main thread that onCreate() is executing on.
            monitor = AndroidNetworkMonitor(applicationContext) { interfaces ->
                manager.refreshInterfaces(interfaces)
            }.also { it.start() }

            runCatching {
                val started = ContractServer.startWithFallback(manager)
                server = started
                ServerHolder.boundPort = started.boundPort
                ServerHolder.serverRunning = true
                ContractLog.i("Local server listening on port ${started.boundPort}")
            }.onFailure {
                ServerHolder.serverRunning = false
                ContractLog.w("Could not start the local server: ${it.message}")
            }
            manager.refreshInterfaces(monitor?.current() ?: emptyList())
        }

        tickJob = scope.launch {
            while (isActive) {
                delay(TICK_MS)
                runCatching { manager.tick() }
            }
        }

        // Read the screen out loud, from the same published TV view the screen itself renders.
        // The engine decides what may be spoken; this only has to notice when the answer
        // changes, because the view republishes on every tick whether anything moved or not.
        narrationJob = scope.launch {
            var spokenKey: String? = null
            ServerHolder.tvView.collect { view ->
                val enabled = manager.sessionRecord?.state?.setup?.narrationEnabled ?: true
                val line = Narration.script(view, enabled)
                when {
                    line == null -> {
                        // Nothing to say on this screen: cut off whatever the last one was, so
                        // a term is not still being read over the screen that replaced it.
                        if (spokenKey != null) runCatching { narrator?.stop() }
                        spokenKey = null
                    }
                    line.key != spokenKey -> {
                        spokenKey = line.key
                        runCatching { narrator?.speak(line.text) }
                    }
                }
            }
        }

        // Sound the chime the moment a timer stops running, watching the same published view
        // the screen renders so the two can never disagree about when a timer ended.
        chimeJob = scope.launch {
            var previous = emptyMap<String, String>()
            ServerHolder.tvView.collect { view ->
                val current = view.timers.associate { it.id to it.state }
                val justFinished = current.any { (id, state) ->
                    state == COMPLETED_STATE && previous[id] == RUNNING_STATE
                }
                if (justFinished) runCatching { chime?.play() }
                previous = current
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                shutDown()
                stopSelf()
                return START_NOT_STICKY
            }

            ACTION_START -> {
                if (intent.getBooleanExtra(EXTRA_RESUME, false)) {
                    scope.launch { runCatching { manager.resumeSavedSession() } }
                }
            }
        }
        acquireWakeLock()
        // START_STICKY so Android brings the service back after a low-memory kill; the session
        // is then restored from storage by the activity or the boot receiver.
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // The launcher swiped the task away. Persist before anything else happens. Blocks this
        // (main) thread until done, but the actual Room write runs on Dispatchers.IO, since Room
        // forbids running it on the main thread directly.
        runBlocking(Dispatchers.IO) { runCatching { manager.persistNow() } }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        shutDown()
        super.onDestroy()
    }

    private fun shutDown() {
        // Persist first, so a running timer is frozen at a safe value before we go away. Same
        // main-thread-blocking-but-IO-executing shape as onTaskRemoved, for the same reason.
        runBlocking(Dispatchers.IO) { runCatching { manager.persistNow() } }
        tickJob?.cancel()
        chimeJob?.cancel()
        chime?.release()
        chime = null
        narrationJob?.cancel()
        narrator?.release()
        narrator = null
        monitor?.stop()
        runCatching { server?.stop() }
        server = null
        releaseWakeLock()
        ServerHolder.detach()
        scope.cancel()
    }

    // ------------------------------------------------------------------ plumbing

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_description)
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_SECRET
        }
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val open = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        // Deliberately generic: a notification must never describe what is happening in the game.
        return builder
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setContentIntent(open)
            .setOngoing(true)
            .setVisibility(Notification.VISIBILITY_SECRET)
            .build()
    }

    private fun startForegroundCompat() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val power = getSystemService(POWER_SERVICE) as? PowerManager ?: return
        wakeLock = power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TheContract:session").apply {
            setReferenceCounted(false)
            runCatching { acquire(TIMEOUT_MS) }
        }
    }

    private fun releaseWakeLock() {
        runCatching { if (wakeLock?.isHeld == true) wakeLock?.release() }
        wakeLock = null
    }
}

/** Eight hours is far longer than any session and still bounded. */
private const val TIMEOUT_MS = 8L * 60L * 60L * 1000L
