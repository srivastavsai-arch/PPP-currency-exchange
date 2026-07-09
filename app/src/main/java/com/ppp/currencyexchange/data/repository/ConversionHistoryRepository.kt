package com.ppp.currencyexchange.data.repository

import com.ppp.currencyexchange.data.local.ConversionHistoryDao
import com.ppp.currencyexchange.data.local.ConversionHistoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversionHistoryRepository @Inject constructor(
    private val dao: ConversionHistoryDao
) {
    fun observeRecentHistory(limit: Int = 50): Flow<List<ConversionHistoryEntity>> {
        return dao.observeRecentHistory(limit)
    }

    suspend fun addEntry(
        fromCurrency: String,
        toCurrency: String,
        fromAmount: Double,
        toAmount: Double,
        rate: Double
    ) {
        dao.insert(
            ConversionHistoryEntity(
                fromCurrency = fromCurrency,
                toCurrency = toCurrency,
                fromAmount = fromAmount,
                toAmount = toAmount,
                rate = rate,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }

    suspend fun getCount(): Int {
        return dao.getCount()
    }
}
