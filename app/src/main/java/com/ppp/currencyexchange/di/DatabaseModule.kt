package com.ppp.currencyexchange.di

import android.content.Context
import androidx.room.Room
import com.ppp.currencyexchange.data.local.AppDatabase
import com.ppp.currencyexchange.data.local.ConversionHistoryDao
import com.ppp.currencyexchange.data.local.ExchangeRateDao
import com.ppp.currencyexchange.data.local.FavoriteDao
import com.ppp.currencyexchange.data.local.SettingsDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "currency_exchange.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideExchangeRateDao(database: AppDatabase): ExchangeRateDao {
        return database.exchangeRateDao()
    }

    @Provides
    @Singleton
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao {
        return database.favoriteDao()
    }

    @Provides
    @Singleton
    fun provideConversionHistoryDao(database: AppDatabase): ConversionHistoryDao {
        return database.conversionHistoryDao()
    }

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore {
        return SettingsDataStore(context)
    }
}
