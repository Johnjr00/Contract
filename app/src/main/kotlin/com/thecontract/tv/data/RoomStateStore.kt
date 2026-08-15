package com.thecontract.tv.data

import android.content.Context
import com.thecontract.core.model.SavedContract
import com.thecontract.core.model.SessionRecord
import com.thecontract.core.model.SetupPreset
import com.thecontract.core.persistence.StateStore
import kotlinx.serialization.json.Json

/**
 * The Android implementation of the core [StateStore], backed by Room with a Keystore-encrypted
 * payload.
 *
 * All calls are synchronous and are made from the server's own threads (the embedded HTTP/WS
 * worker threads and the session lock), never from the main thread — the TV surface is driven
 * by a state flow the service publishes, so no UI code touches the database.
 */
class RoomStateStore(context: Context) : StateStore {

    private val db = ContractDatabase.get(context)
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    override fun loadActiveSession(): SessionRecord? {
        val row = db.sessions().load(SessionEntity.SINGLETON_ID) ?: return null
        val plaintext = Crypto.decrypt(row.payload) ?: return null
        return runCatching { json.decodeFromString<SessionRecord>(plaintext) }.getOrNull()
    }

    override fun saveSession(record: SessionRecord) {
        db.sessions().save(
            SessionEntity(
                sessionId = record.sessionId,
                finished = record.finished,
                savedAtMs = record.savedAtMs,
                payload = Crypto.encrypt(json.encodeToString(record))
            )
        )
    }

    override fun deleteActiveSession() {
        db.sessions().clear()
    }

    override fun listContracts(): List<SavedContract> =
        db.contracts().all().mapNotNull { row ->
            Crypto.decrypt(row.payload)?.let {
                runCatching { json.decodeFromString<SavedContract>(it) }.getOrNull()
            }
        }

    override fun loadContract(id: String): SavedContract? {
        val row = db.contracts().byId(id) ?: return null
        val plaintext = Crypto.decrypt(row.payload) ?: return null
        return runCatching { json.decodeFromString<SavedContract>(plaintext) }.getOrNull()
    }

    override fun saveContract(contract: SavedContract) {
        db.contracts().save(
            ContractEntity(
                id = contract.id,
                title = contract.title,
                completedAtMs = contract.completedAtMs,
                termCount = contract.signedTerms.size,
                payload = Crypto.encrypt(json.encodeToString(contract))
            )
        )
    }

    override fun deleteContract(id: String) {
        db.contracts().delete(id)
    }

    override fun listSetupPresets(): List<SetupPreset> =
        db.setupPresets().all().mapNotNull { row ->
            Crypto.decrypt(row.payload)?.let {
                runCatching { json.decodeFromString<SetupPreset>(it) }.getOrNull()
            }
        }

    override fun loadSetupPreset(id: String): SetupPreset? {
        val row = db.setupPresets().byId(id) ?: return null
        val plaintext = Crypto.decrypt(row.payload) ?: return null
        return runCatching { json.decodeFromString<SetupPreset>(plaintext) }.getOrNull()
    }

    override fun saveSetupPreset(preset: SetupPreset) {
        db.setupPresets().save(
            SetupPresetEntity(
                id = preset.id,
                name = preset.name,
                savedAtMs = preset.savedAtMs,
                payload = Crypto.encrypt(json.encodeToString(preset))
            )
        )
    }

    override fun deleteSetupPreset(id: String) {
        db.setupPresets().delete(id)
    }
}
