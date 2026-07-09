package com.ppp.currencyexchange.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ExchangeRateEntity::class,
        FavoriteEntity::class,
        ConversionHistoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exchangeRateDao(): ExchangeRateDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun conversionHistoryDao(): ConversionHistoryDao
}
