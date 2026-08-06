package com.thecontract.core.persistence

import com.thecontract.core.model.SavedContract
import com.thecontract.core.model.SessionRecord
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
}

internal val StoreJson: Json = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
    explicitNulls = false
}

/** Non-durable store used by tests that do not exercise restart behaviour. */
class InMemoryStateStore : StateStore {
    private val lock = ReentrantLock()
    private var session: SessionRecord? = null
    private val contracts = LinkedHashMap<String, SavedContract>()

    override fun loadActiveSession(): SessionRecord? = lock.withLock { session }

    override fun saveSession(record: SessionRecord) = lock.withLock { session = record }

    override fun deleteActiveSession() = lock.withLock { session = null }

    override fun listContracts(): List<SavedContract> = lock.withLock { contracts.values.toList() }

    override fun loadContract(id: String): SavedContract? = lock.withLock { contracts[id] }

    override fun saveContract(contract: SavedContract) = lock.withLock { contracts[contract.id] = contract; Unit }

    override fun deleteContract(id: String) = lock.withLock { contracts.remove(id); Unit }
}

/**
 * Durable JSON-on-disk store. Writes go to a temporary file and are then atomically renamed, so
 * a process kill in the middle of a write cannot leave a half-written session behind.
 */
class JsonFileStateStore(private val directory: File) : StateStore {

    private val lock = ReentrantLock()
    private val sessionFile = File(directory, "active-session.json")
    private val contractsDir = File(directory, "contracts")

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
}
