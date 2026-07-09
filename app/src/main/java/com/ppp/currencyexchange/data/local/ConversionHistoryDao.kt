package com.ppp.currencyexchange.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversionHistoryDao {
    @Insert
    suspend fun insert(entry: ConversionHistoryEntity)

    @Query("SELECT * FROM conversion_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentHistory(limit: Int = 50): List<ConversionHistoryEntity>

    @Query("SELECT * FROM conversion_history ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecentHistory(limit: Int = 50): Flow<List<ConversionHistoryEntity>>

    @Query("DELETE FROM conversion_history")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM conversion_history")
    suspend fun getCount(): Int
}
