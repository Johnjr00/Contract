package com.thecontract.tv.voice

import android.content.Context
import com.thecontract.tv.ContractLog
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Puts the voice on the television, once.
 *
 * This is the only outbound network call the app makes, and it makes it exactly one time on one
 * device. Everything about the design follows from what a bad download would cost: 139 MB over a
 * home connection to a box under a television, where the failure modes are a sleeping Wi‑Fi
 * radio, a captive portal answering with a login page, and somebody pulling the power partway
 * through. So:
 *
 * - **Every file is verified by hash** before it counts as installed. A captive portal serving
 *   HTML with a 200 is indistinguishable from success at the HTTP layer, and an ONNX file that is
 *   secretly a login page fails somewhere much less legible than here.
 * - **Work happens in a staging directory** and the whole thing is promoted with a single
 *   rename. A half-installed model can never be picked up as a real one, whatever moment the
 *   power is cut at.
 * - **Verified files survive a failure.** Each is promoted within the staging directory as it
 *   passes, so a retry after a dropped connection resumes at file granularity rather than
 *   starting the 139 MB again.
 *
 * Byte-range resume within a single file is deliberately not attempted: `raw.githubusercontent`
 * advertises `accept-ranges` but answers a ranged request with a plain 200 and the whole body, so
 * code that trusted the header would silently append a second copy of the file onto the first.
 */
object VoiceInstaller {

    /** Where the model was found, which decides how sherpa-onnx is asked to open it. */
    sealed interface Source {
        /** Packaged in the APK — a build made with the files copied back into assets. */
        data object Assets : Source

        /** Downloaded into private storage. */
        data class Files(val dir: File) : Source
    }

    /** Progress of the one-time download, in bytes. */
    data class Progress(val bytesDone: Long, val bytesTotal: Long)

    private const val ATTEMPTS_PER_PART = 3
    private const val CONNECT_TIMEOUT_MS = 20_000
    private const val READ_TIMEOUT_MS = 30_000
    private const val BUFFER = 1 shl 16

    /** Reported often enough to look alive, rarely enough not to flood the view. */
    private const val PROGRESS_STEP_BYTES = 512L * 1024L

    private fun installedDir(context: Context) = File(context.filesDir, VoiceModel.DIR_NAME)
    private fun stagingDir(context: Context) = File(context.filesDir, "${VoiceModel.DIR_NAME}.partial")

    /**
     * True when the model is packaged in the APK. Cheap: [Context.getAssets] `list` on a missing
     * directory returns empty rather than throwing, so this doubles as the existence check.
     */
    private fun bundled(context: Context): Boolean = runCatching {
        val listed = context.assets.list(VoiceModel.DIR_NAME)?.toSet().orEmpty()
        VoiceModel.PARTS.all { it.name in listed }
    }.getOrDefault(false)

    /** True when a previous run finished, checked by size alone so it stays a cheap startup test. */
    private fun installed(context: Context): Boolean {
        val dir = installedDir(context)
        return VoiceModel.PARTS.all { File(dir, it.name).length() == it.bytes }
    }

    /** Whether anything at all has to be fetched, without fetching it. */
    fun ready(context: Context): Boolean = bundled(context) || installed(context)

    /**
     * Returns where the model is, downloading it first if it is not anywhere yet.
     *
     * Blocking, and slow the first time by design — call it off any thread that matters. Returns
     * null when the download could not be completed, having already reported why through
     * [onFailure]; calling again retries, which is the right response to a television that was
     * simply not on the network yet.
     */
    fun resolve(
        context: Context,
        onProgress: (Progress) -> Unit,
        onFailure: (String) -> Unit
    ): Source? {
        if (bundled(context)) return Source.Assets
        if (installed(context)) return Source.Files(installedDir(context))
        return download(context, onProgress, onFailure)
    }

    private fun download(
        context: Context,
        onProgress: (Progress) -> Unit,
        onFailure: (String) -> Unit
    ): Source? {
        val staging = stagingDir(context)
        if (!staging.isDirectory && !staging.mkdirs()) {
            onFailure("No room to store the voice.")
            return null
        }

        // Files already verified by an earlier attempt are counted, not fetched again.
        var done = VoiceModel.PARTS.sumOf { part ->
            if (File(staging, part.name).length() == part.bytes) part.bytes else 0L
        }
        onProgress(Progress(done, VoiceModel.TOTAL_BYTES))

        for (part in VoiceModel.PARTS) {
            val target = File(staging, part.name)
            if (target.length() == part.bytes) continue

            val base = done
            val failure = fetch(part, target) { within ->
                onProgress(Progress(base + within, VoiceModel.TOTAL_BYTES))
            }
            if (failure != null) {
                onFailure(failure)
                return null
            }
            done = base + part.bytes
            onProgress(Progress(done, VoiceModel.TOTAL_BYTES))
        }

        // One rename is the whole install. Anything that goes wrong before this line leaves a
        // staging directory that the next attempt picks up, and nothing that looks installed.
        val finalDir = installedDir(context)
        finalDir.deleteRecursively()
        if (!staging.renameTo(finalDir)) {
            onFailure("Could not finish installing the voice.")
            return null
        }
        ContractLog.i("Voice installed (${VoiceModel.TOTAL_BYTES / 1_000_000} MB)")
        return Source.Files(finalDir)
    }

    /**
     * Fetches one file, retrying a few times. Returns null on success or a sentence fit for the
     * television on failure.
     *
     * The download goes to a `.part` sibling and is renamed onto [target] only after its hash
     * matches, so [target] existing at the right size always means verified.
     */
    private fun fetch(part: VoiceModel.Part, target: File, onProgress: (Long) -> Unit): String? {
        val work = File(target.parentFile, "${part.name}.part")
        var last = "The voice could not be downloaded."

        repeat(ATTEMPTS_PER_PART) { tries ->
            if (tries > 0) {
                // 2s, then 4s. A television that has just woken its radio needs a moment more
                // than an immediate retry gives it.
                runCatching { Thread.sleep(2000L shl (tries - 1)) }
            }
            work.delete()
            val outcome = runCatching { attemptOnce(part, work, onProgress) }
            val problem = outcome.getOrElse { it.message ?: it::class.java.simpleName }
            if (problem == null) {
                work.delete()
                return null
            }
            last = problem
            ContractLog.w("Voice download of ${part.name} failed: $problem")
        }
        work.delete()
        return last
    }

    /** One attempt at one file. Returns null on success, or what went wrong. */
    private fun attemptOnce(part: VoiceModel.Part, work: File, onProgress: (Long) -> Unit): String? {
        val connection = (URL("${VoiceModel.BASE_URL}/${part.name}").openConnection() as HttpURLConnection)
        connection.connectTimeout = CONNECT_TIMEOUT_MS
        connection.readTimeout = READ_TIMEOUT_MS
        connection.instanceFollowRedirects = true
        connection.setRequestProperty("Accept", "application/octet-stream")

        try {
            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                return "The voice download was refused (HTTP $code)."
            }

            val digest = MessageDigest.getInstance("SHA-256")
            var written = 0L
            var announced = 0L
            connection.inputStream.use { input ->
                work.outputStream().use { output ->
                    val buffer = ByteArray(BUFFER)
                    while (true) {
                        val n = input.read(buffer)
                        if (n < 0) break
                        // Bail rather than fill the disk if the body is longer than declared.
                        if (written + n > part.bytes) return "The voice download was the wrong size."
                        output.write(buffer, 0, n)
                        digest.update(buffer, 0, n)
                        written += n
                        if (written - announced >= PROGRESS_STEP_BYTES) {
                            announced = written
                            onProgress(written)
                        }
                    }
                }
            }

            if (written != part.bytes) return "The voice download was cut short."
            if (hex(digest.digest()) != part.sha256) return "The voice download arrived damaged."
        } catch (e: IOException) {
            return e.message ?: "The television could not reach the network."
        } finally {
            connection.disconnect()
        }

        // Renaming last is what makes a present, correctly sized file mean "verified".
        return if (work.renameTo(File(work.parentFile, part.name))) null else "Could not store the voice."
    }

    private fun hex(bytes: ByteArray): String {
        val out = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            val v = b.toInt() and 0xff
            if (v < 0x10) out.append('0')
            out.append(Integer.toHexString(v))
        }
        return out.toString()
    }
}
