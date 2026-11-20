package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/**
 * Device-local SQLite cache for:
 * 1) Recent merchant queries (offline advisor presets).
 * 2) Indian (1,000+ cards) & global credit-card issuing banks and card products directory.
 * 3) Selected country/market filter preference.
 *
 * Primary user wallet cards & purchase logs remain durably persisted in Cloud Firestore.
 */
@Entity(tableName = "recent_merchant_searches")
data class RecentMerchantSearchEntity(
    @PrimaryKey val merchantKey: String,
    val userId: String,
    val merchantLabel: String,
    val categoryName: String,
    val paymentModeName: String,
    val lastAmount: Double,
    val searchedAtEpochMs: Long
)

@Entity(
    tableName = "catalog_banks",
    indices = [Index(value = ["countryCode"])]
)
data class CatalogBankEntity(
    @PrimaryKey val bankKey: String,
    val bankName: String,
    val countryCode: String,
    val countryName: String,
    val currencySymbol: String,
    val officialWebsiteUrl: String,
    val officialDomain: String,
    val isUserDiscovered: Boolean = false,
    val addedAtEpochMs: Long = 0L
)

@Entity(
    tableName = "catalog_cards",
    indices = [
        Index(value = ["bankKey"]),
        Index(value = ["countryCode"]),
        Index(value = ["isNewlyDiscovered"])
    ]
)
data class CatalogCardEntity(
    @PrimaryKey val cardKey: String,
    val bankKey: String,
    val bankName: String,
    val countryCode: String,
    val cardName: String,
    val defaultNetwork: String,
    val defaultTheme: String,
    val defaultBaseRatePercent: Double,
    val defaultRewardType: String,
    val isNewlyDiscovered: Boolean = false,
    val discoveredAtEpochMs: Long = 0L,
    val sourceUrl: String = ""
)

@Entity(tableName = "catalog_sync_state")
data class CatalogSyncStateEntity(
    @PrimaryKey val id: String = SYNC_ROW_ID,
    val selectedCountryCode: String = "IN",
    val lastSyncEpochMs: Long = 0L,
    val lastSyncStatus: String = "IDLE",
    val lastSyncMessage: String = "Scheduled daily at 2:00 AM",
    val lastNewCardsFoundCount: Int = 0,
    val nextScheduledEpochMs: Long = 0L
) {
    companion object {
        const val SYNC_ROW_ID = "default"
    }
}

@Dao
interface RecentMerchantSearchDao {
    @Query(
        "SELECT * FROM recent_merchant_searches " +
            "WHERE userId = :userId " +
            "ORDER BY searchedAtEpochMs DESC LIMIT 10"
    )
    fun observeRecentSearches(userId: String): Flow<List<RecentMerchantSearchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSearch(entry: RecentMerchantSearchEntity)

    @Query("DELETE FROM recent_merchant_searches WHERE userId = :userId")
    suspend fun clearForUser(userId: String)
}

@Dao
interface GlobalCatalogDao {
    @Query("SELECT * FROM catalog_banks ORDER BY countryName ASC, bankName ASC")
    fun observeAllBanks(): Flow<List<CatalogBankEntity>>

    @Query("SELECT * FROM catalog_cards ORDER BY isNewlyDiscovered DESC, discoveredAtEpochMs DESC, cardName ASC")
    fun observeAllCards(): Flow<List<CatalogCardEntity>>

    @Query("SELECT * FROM catalog_cards WHERE isNewlyDiscovered = 1 ORDER BY discoveredAtEpochMs DESC, cardName ASC")
    fun observeNewlyDiscoveredCards(): Flow<List<CatalogCardEntity>>

    @Query("SELECT * FROM catalog_sync_state WHERE id = 'default' LIMIT 1")
    fun observeSyncState(): Flow<CatalogSyncStateEntity?>

    @Query("SELECT * FROM catalog_sync_state WHERE id = 'default' LIMIT 1")
    suspend fun getSyncStateOnce(): CatalogSyncStateEntity?

    @Query("SELECT COUNT(*) FROM catalog_banks")
    suspend fun getBankCount(): Int

    @Query("SELECT * FROM catalog_banks WHERE countryCode = :countryCode ORDER BY bankName ASC")
    suspend fun getBanksByCountryOnce(countryCode: String): List<CatalogBankEntity>

    @Query("SELECT * FROM catalog_banks ORDER BY bankName ASC")
    suspend fun getAllBanksOnce(): List<CatalogBankEntity>

    @Query("SELECT cardKey FROM catalog_cards")
    suspend fun getAllCardKeysOnce(): List<String>

    @Query("SELECT * FROM catalog_cards WHERE bankKey = :bankKey")
    suspend fun getCardsForBankOnce(bankKey: String): List<CatalogCardEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBanksIgnore(banks: List<CatalogBankEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBank(bank: CatalogBankEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCardsIgnore(cards: List<CatalogCardEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSyncState(state: CatalogSyncStateEntity)

    @Query("UPDATE catalog_cards SET isNewlyDiscovered = 0 WHERE isNewlyDiscovered = 1")
    suspend fun markAllNewCardsSeen()

    @Query("UPDATE catalog_cards SET isNewlyDiscovered = 0 WHERE cardKey = :cardKey")
    suspend fun markCardSeen(cardKey: String)
}

@Database(
    entities = [
        RecentMerchantSearchEntity::class,
        CatalogBankEntity::class,
        CatalogCardEntity::class,
        CatalogSyncStateEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CardWiseLocalDatabase : RoomDatabase() {
    abstract fun recentMerchantSearchDao(): RecentMerchantSearchDao
    abstract fun globalCatalogDao(): GlobalCatalogDao

    companion object {
        @Volatile
        private var INSTANCE: CardWiseLocalDatabase? = null

        fun getInstance(context: Context): CardWiseLocalDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CardWiseLocalDatabase::class.java,
                    "cardwise_local_cache.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
