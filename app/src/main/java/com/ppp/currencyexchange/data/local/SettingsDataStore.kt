package com.ppp.currencyexchange.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val AUTO_UPDATE = booleanPreferencesKey("auto_update")
        val UPDATE_FREQUENCY = intPreferencesKey("update_frequency")
        val DEFAULT_CURRENCY = stringPreferencesKey("default_currency")
        val DECIMAL_PLACES = intPreferencesKey("decimal_places")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val IS_ONLINE = booleanPreferencesKey("is_online")
        val LAST_FROM_CURRENCY = stringPreferencesKey("last_from_currency")
        val LAST_TO_CURRENCY = stringPreferencesKey("last_to_currency")
        val MANUAL_PAIR_RATES = stringPreferencesKey("manual_pair_rates_json")
    }

    val autoUpdate: Flow<Boolean> = context.dataStore.data.map { it[AUTO_UPDATE] ?: true }
    val updateFrequency: Flow<Int> = context.dataStore.data.map { it[UPDATE_FREQUENCY] ?: 30 }
    val defaultCurrency: Flow<String> = context.dataStore.data.map { it[DEFAULT_CURRENCY] ?: "USD" }
    val decimalPlaces: Flow<Int> = context.dataStore.data.map { it[DECIMAL_PLACES] ?: 2 }
    val themeMode: Flow<String> = context.dataStore.data.map { it[THEME_MODE] ?: "system" }
    val isOnline: Flow<Boolean> = context.dataStore.data.map { it[IS_ONLINE] ?: true }
    val lastFromCurrency: Flow<String> = context.dataStore.data.map { it[LAST_FROM_CURRENCY] ?: "USD" }
    val lastToCurrency: Flow<String> = context.dataStore.data.map { it[LAST_TO_CURRENCY] ?: "INR" }
    val manualPairRates: Flow<String> = context.dataStore.data.map { it[MANUAL_PAIR_RATES] ?: "{}" }

    suspend fun setAutoUpdate(enabled: Boolean) {
        context.dataStore.edit { it[AUTO_UPDATE] = enabled }
    }

    suspend fun setUpdateFrequency(minutes: Int) {
        context.dataStore.edit { it[UPDATE_FREQUENCY] = minutes }
    }

    suspend fun setDefaultCurrency(code: String) {
        context.dataStore.edit { it[DEFAULT_CURRENCY] = code }
    }

    suspend fun setDecimalPlaces(places: Int) {
        context.dataStore.edit { it[DECIMAL_PLACES] = places }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun setIsOnline(online: Boolean) {
        context.dataStore.edit { it[IS_ONLINE] = online }
    }

    suspend fun setLastCurrencies(from: String, to: String) {
        context.dataStore.edit {
            it[LAST_FROM_CURRENCY] = from
            it[LAST_TO_CURRENCY] = to
        }
    }

    suspend fun saveManualPairRatesJson(json: String) {
        context.dataStore.edit { it[MANUAL_PAIR_RATES] = json }
    }
}
