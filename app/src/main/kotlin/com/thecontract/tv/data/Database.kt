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
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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

/**
 * A named shared setup, kept between games.
 *
 * The name and the date are in the clear so the list can be built and sorted without decrypting
 * anything; the setup itself — names, roles, boundaries, equipment — is in the encrypted
 * [payload], like everything else the game stores.
 */
@Entity(tableName = "setup_presets")
data class SetupPresetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val savedAtMs: Long,
    val payload: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SetupPresetEntity) return false
        return id == other.id &&
            name == other.name &&
            savedAtMs == other.savedAtMs &&
            payload.contentEquals(other.payload)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + savedAtMs.hashCode()
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

@Dao
interface SetupPresetDao {
    @Query("SELECT * FROM setup_presets ORDER BY savedAtMs DESC")
    fun all(): List<SetupPresetEntity>

    @Query("SELECT * FROM setup_presets WHERE id = :id LIMIT 1")
    fun byId(id: String): SetupPresetEntity?

    /** Replace-on-conflict is what makes saving under an existing name an overwrite. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(preset: SetupPresetEntity)

    @Query("DELETE FROM setup_presets WHERE id = :id")
    fun delete(id: String)
}

@Database(
    entities = [SessionEntity::class, ContractEntity::class, SetupPresetEntity::class],
    version = 2,
    exportSchema = true
)
abstract class ContractDatabase : RoomDatabase() {
    abstract fun sessions(): SessionDao
    abstract fun contracts(): ContractDao
    abstract fun setupPresets(): SetupPresetDao

    companion object {
        @Volatile
        private var instance: ContractDatabase? = null

        /**
         * Adds the saved-settings table.
         *
         * Written out rather than left to the destructive fallback below, which would have taken
         * every saved contract on the television with it on the upgrade that introduced the
         * feature. The statement must match what Room generates for [SetupPresetEntity] exactly,
         * or the identity check fails on first open.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `setup_presets` (" +
                        "`id` TEXT NOT NULL, `name` TEXT NOT NULL, `savedAtMs` INTEGER NOT NULL, " +
                        "`payload` BLOB NOT NULL, PRIMARY KEY(`id`))"
                )
            }
        }

        fun get(context: Context): ContractDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                ContractDatabase::class.java,
                "the-contract.db"
            )
                .addMigrations(MIGRATION_1_2)
                // Still the safety net for any version pair with no migration written: the
                // database is a local cache of one in-flight game plus saved material, and a
                // corrupt restore is worse than a rebuild.
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
        }
    }
}
