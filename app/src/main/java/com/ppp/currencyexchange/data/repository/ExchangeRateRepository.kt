package com.ppp.currencyexchange.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ppp.currencyexchange.data.local.ExchangeRateDao
import com.ppp.currencyexchange.data.local.ExchangeRateEntity
import com.ppp.currencyexchange.data.remote.ExchangeRateApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExchangeRateRepository @Inject constructor(
    private val api: ExchangeRateApi,
    private val dao: ExchangeRateDao,
    private val gson: Gson
) {
    companion object {
        private const val CACHE_DURATION_MS = 24 * 60 * 60 * 1000L
    }

    fun getLatestRates(): Flow<Result<Map<String, Double>>> = flow {
        val cached = dao.getRates("USD")
        if (cached != null && System.currentTimeMillis() - cached.lastUpdated < CACHE_DURATION_MS) {
            val type = object : TypeToken<Map<String, Double>>() {}.type
            val rates: Map<String, Double> = gson.fromJson(cached.ratesJson, type)
            emit(Result.success(rates))
            return@flow
        }

        try {
            val response = api.getLatestRates()
            val upperRates = response.rates.mapKeys { it.key.uppercase() }
            val ratesJson = gson.toJson(upperRates)
            dao.insertRates(
                ExchangeRateEntity(
                    baseCurrency = "USD",
                    ratesJson = ratesJson,
                    lastUpdated = System.currentTimeMillis()
                )
            )
            emit(Result.success(upperRates))
        } catch (e: Exception) {
            if (cached != null) {
                val type = object : TypeToken<Map<String, Double>>() {}.type
                val rates: Map<String, Double> = gson.fromJson(cached.ratesJson, type)
                emit(Result.success(rates))
            } else {
                emit(Result.failure(e))
            }
        }
    }

    suspend fun clearCache() {
        dao.clearAll()
    }
}
