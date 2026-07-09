package com.ppp.currencyexchange.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ExchangeRateDao {
    @Upsert
    suspend fun insertRates(rates: ExchangeRateEntity)

    @Query("SELECT * FROM exchange_rates WHERE baseCurrency = :baseCurrency")
    suspend fun getRates(baseCurrency: String): ExchangeRateEntity?

    @Query("DELETE FROM exchange_rates")
    suspend fun clearAll()
}
