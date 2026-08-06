package com.thecontract.tv.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Persistence tables.
 *
 * Only the metadata needed to list and resume things is stored in the clear. The game state
 * itself — profiles, boundaries, signed terms, receipts, tokens — lives in [payload], which is
 * a Keystore-encrypted JSON document (see [Crypto]).
 */
@Entity(tableName = "active_session")
data class SessionEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val sessionId: String,
    val finished: Boolean,
    val savedAtMs: Long,
    val payload: ByteArray
) {
    companion object {
        const val SINGLETON_ID = 1
    }

    // Room data classes holding a ByteArray need these written out by hand.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SessionEntity) return false
        return id == other.id &&
            sessionId == other.sessionId &&
            finished == other.finished &&
            savedAtMs == other.savedAtMs &&
            payload.contentEquals(other.payload)
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + sessionId.hashCode()
        result = 31 * result + finished.hashCode()
        result = 31 * result + savedAtMs.hashCode()
        result = 31 * result + payload.contentHashCode()
        return result
    }
}

@Entity(tableName = "saved_contracts")
data class ContractEntity(
    @PrimaryKey val id: String,
    val title: String,
    val completedAtMs: Long,
    val termCount: Int,
    val payload: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ContractEntity) return false
        return id == other.id &&
            title == other.title &&
            completedAtMs == other.completedAtMs &&
            termCount == other.termCount &&
            payload.contentEquals(other.payload)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + completedAtMs.hashCode()
        result = 31 * result + termCount
        result = 31 * result + payload.contentHashCode()
        return result
    }
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM active_session WHERE id = :id LIMIT 1")
    fun load(id: Int): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(session: SessionEntity)

    @Query("DELETE FROM active_session")
    fun clear()
}

@Dao
interface ContractDao {
    @Query("SELECT * FROM saved_contracts ORDER BY completedAtMs DESC")
    fun all(): List<ContractEntity>

    @Query("SELECT * FROM saved_contracts WHERE id = :id LIMIT 1")
    fun byId(id: String): ContractEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(contract: ContractEntity)

    @Query("DELETE FROM saved_contracts WHERE id = :id")
    fun delete(id: String)
}

@Database(
    entities = [SessionEntity::class, ContractEntity::class],
    version = 1,
    exportSchema = true
)
abstract class ContractDatabase : RoomDatabase() {
    abstract fun sessions(): SessionDao
    abstract fun contracts(): ContractDao

    companion object {
        @Volatile
        private var instance: ContractDatabase? = null

        fun get(context: Context): ContractDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                ContractDatabase::class.java,
                "the-contract.db"
            )
                // The database is a local cache of one in-flight game plus saved contracts.
                // A schema change is not worth risking a corrupt restore, so rebuild instead.
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
        }
    }
}
