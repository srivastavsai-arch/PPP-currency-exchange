package com.ppp.currencyexchange.data.repository

import com.ppp.currencyexchange.data.local.FavoriteDao
import com.ppp.currencyexchange.data.local.FavoriteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepository @Inject constructor(
    private val dao: FavoriteDao
) {
    fun observeFavorites(): Flow<List<FavoriteEntity>> {
        return dao.observeFavorites()
    }

    suspend fun getAllFavorites(): List<FavoriteEntity> {
        return dao.getAllFavorites()
    }

    suspend fun addFavorite(fromCurrency: String, toCurrency: String) {
        dao.insert(
            FavoriteEntity(
                fromCurrency = fromCurrency,
                toCurrency = toCurrency,
                addedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun removeFavorite(fromCurrency: String, toCurrency: String) {
        dao.removeFavorite(fromCurrency, toCurrency)
    }

    suspend fun isFavorite(fromCurrency: String, toCurrency: String): Boolean {
        return dao.isFavorite(fromCurrency, toCurrency) > 0
    }
}
