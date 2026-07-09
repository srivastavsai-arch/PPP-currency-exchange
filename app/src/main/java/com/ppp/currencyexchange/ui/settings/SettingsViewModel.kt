package com.ppp.currencyexchange.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ppp.currencyexchange.data.local.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val autoUpdate: Boolean = true,
    val updateFrequency: Int = 30,
    val defaultCurrency: String = "USD",
    val decimalPlaces: Int = 2,
    val themeMode: String = "system",
    val isOnline: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    autoUpdate = settingsDataStore.autoUpdate.first(),
                    updateFrequency = settingsDataStore.updateFrequency.first(),
                    defaultCurrency = settingsDataStore.defaultCurrency.first(),
                    decimalPlaces = settingsDataStore.decimalPlaces.first(),
                    themeMode = settingsDataStore.themeMode.first(),
                    isOnline = settingsDataStore.isOnline.first()
                )
            }
        }
    }

    fun setAutoUpdate(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setAutoUpdate(enabled)
            _uiState.update { it.copy(autoUpdate = enabled) }
        }
    }

    fun setUpdateFrequency(minutes: Int) {
        viewModelScope.launch {
            settingsDataStore.setUpdateFrequency(minutes)
            _uiState.update { it.copy(updateFrequency = minutes) }
        }
    }

    fun setDefaultCurrency(code: String) {
        viewModelScope.launch {
            settingsDataStore.setDefaultCurrency(code)
            _uiState.update { it.copy(defaultCurrency = code) }
        }
    }

    fun setDecimalPlaces(places: Int) {
        viewModelScope.launch {
            settingsDataStore.setDecimalPlaces(places)
            _uiState.update { it.copy(decimalPlaces = places) }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsDataStore.setThemeMode(mode)
            _uiState.update { it.copy(themeMode = mode) }
        }
    }

    fun resetManualRates() {
        viewModelScope.launch {
            settingsDataStore.saveManualPairRatesJson("{}")
        }
    }
}
