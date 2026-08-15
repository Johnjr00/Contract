package com.thecontract.core.persistence

import com.thecontract.core.model.SavedContract
import com.thecontract.core.model.SessionRecord
import com.thecontract.core.model.SetupPreset
import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Persistence contract (section 11).
 *
 * The authoritative state is written after every meaningful mutation, so an unfinished game
 * survives activity recreation, process death, a server restart, an app relaunch and a TV
 * reboot. Timer countdowns are deliberately *not* written every second; timers are persisted
 * only on transitions and when the server is shutting down or being backgrounded.
 *
 * The Android module implements this on Room with the payload encrypted under a key held in the
 * Android Keystore. This JVM implementation is used by the test suite and as a fallback.
 */
interface StateStore {
    /** The single unfinished session, if any. */
    fun loadActiveSession(): SessionRecord?

    fun saveSession(record: SessionRecord)

    fun deleteActiveSession()

    fun listContracts(): List<SavedContract>

    fun loadContract(id: String): SavedContract?

    fun saveContract(contract: SavedContract)

    fun deleteContract(id: String)

    /**
     * Named shared setups, newest first. Outlive every session: they are keyed to the television,
     * not to a game, so ending, abandoning or finishing a session leaves them alone.
     */
    fun listSetupPresets(): List<SetupPreset>

    fun loadSetupPreset(id: String): SetupPreset?

    /** Replaces any preset with the same id, which is how saving under an existing name works. */
    fun saveSetupPreset(preset: SetupPreset)

    fun deleteSetupPreset(id: String)
}

internal val StoreJson: Json = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
    explicitNulls = false
    // A contract saved by an older build can name a setting this build no longer has — the
    // ask-again and midway check-in conditions, for instance. Without this the whole record
    // fails to decode and the contract silently vanishes from the saved list; with it, the
    // property falls back to its default and the rest of the contract survives.
    coerceInputValues = true
}

/** Non-durable store used by tests that do not exercise restart behaviour. */
class InMemoryStateStore : StateStore {
    private val lock = ReentrantLock()
    private var session: SessionRecord? = null
    private val contracts = LinkedHashMap<String, SavedContract>()
    private val presets = LinkedHashMap<String, SetupPreset>()

    override fun loadActiveSession(): SessionRecord? = lock.withLock { session }

    override fun saveSession(record: SessionRecord) = lock.withLock { session = record }

    override fun deleteActiveSession() = lock.withLock { session = null }

    override fun listContracts(): List<SavedContract> = lock.withLock { contracts.values.toList() }

    override fun loadContract(id: String): SavedContract? = lock.withLock { contracts[id] }

    override fun saveContract(contract: SavedContract) = lock.withLock { contracts[contract.id] = contract; Unit }

    override fun deleteContract(id: String) = lock.withLock { contracts.remove(id); Unit }

    override fun listSetupPresets(): List<SetupPreset> =
        lock.withLock { presets.values.sortedByDescending { it.savedAtMs } }

    override fun loadSetupPreset(id: String): SetupPreset? = lock.withLock { presets[id] }

    override fun saveSetupPreset(preset: SetupPreset) = lock.withLock { presets[preset.id] = preset; Unit }

    override fun deleteSetupPreset(id: String) = lock.withLock { presets.remove(id); Unit }
}

/**
 * Durable JSON-on-disk store. Writes go to a temporary file and are then atomically renamed, so
 * a process kill in the middle of a write cannot leave a half-written session behind.
 */
class JsonFileStateStore(private val directory: File) : StateStore {

    private val lock = ReentrantLock()
    private val sessionFile = File(directory, "active-session.json")
    private val contractsDir = File(directory, "contracts")
    private val presetsFile = File(directory, "setup-presets.json")

    init {
        directory.mkdirs()
        contractsDir.mkdirs()
    }

    private fun writeAtomically(target: File, content: String) {
        val tmp = File(target.parentFile, "${target.name}.tmp")
        tmp.writeText(content)
        if (!tmp.renameTo(target)) {
            target.delete()
            tmp.renameTo(target)
        }
    }

    override fun loadActiveSession(): SessionRecord? = lock.withLock {
        if (!sessionFile.exists()) return null
        runCatching { StoreJson.decodeFromString<SessionRecord>(sessionFile.readText()) }.getOrNull()
    }

    override fun saveSession(record: SessionRecord) = lock.withLock {
        writeAtomically(sessionFile, StoreJson.encodeToString(record))
    }

    override fun deleteActiveSession() = lock.withLock {
        sessionFile.delete()
        Unit
    }

    override fun listContracts(): List<SavedContract> = lock.withLock {
        contractsDir.listFiles { f -> f.extension == "json" }
            ?.mapNotNull { f -> runCatching { StoreJson.decodeFromString<SavedContract>(f.readText()) }.getOrNull() }
            ?.sortedByDescending { it.completedAtMs }
            ?: emptyList()
    }

    override fun loadContract(id: String): SavedContract? = lock.withLock {
        val f = File(contractsDir, "$id.json")
        if (!f.exists()) return null
        runCatching { StoreJson.decodeFromString<SavedContract>(f.readText()) }.getOrNull()
    }

    override fun saveContract(contract: SavedContract) = lock.withLock {
        writeAtomically(File(contractsDir, "${contract.id}.json"), StoreJson.encodeToString(contract))
    }

    override fun deleteContract(id: String) = lock.withLock {
        File(contractsDir, "$id.json").delete()
        Unit
    }

    // Presets live in one small file rather than a file each: the list is capped, and it is always
    // read and written whole.
    private fun readPresetsLocked(): List<SetupPreset> {
        if (!presetsFile.exists()) return emptyList()
        return runCatching { StoreJson.decodeFromString<List<SetupPreset>>(presetsFile.readText()) }
            .getOrDefault(emptyList())
    }

    override fun listSetupPresets(): List<SetupPreset> =
        lock.withLock { readPresetsLocked().sortedByDescending { it.savedAtMs } }

    override fun loadSetupPreset(id: String): SetupPreset? =
        lock.withLock { readPresetsLocked().firstOrNull { it.id == id } }

    override fun saveSetupPreset(preset: SetupPreset) = lock.withLock {
        val next = readPresetsLocked().filterNot { it.id == preset.id } + preset
        writeAtomically(presetsFile, StoreJson.encodeToString(next))
    }

    override fun deleteSetupPreset(id: String) = lock.withLock {
        val next = readPresetsLocked().filterNot { it.id == id }
        writeAtomically(presetsFile, StoreJson.encodeToString(next))
    }
}
