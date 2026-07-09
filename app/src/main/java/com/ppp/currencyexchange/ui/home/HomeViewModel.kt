package com.ppp.currencyexchange.ui.home

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ppp.currencyexchange.data.local.SettingsDataStore
import com.ppp.currencyexchange.data.model.Currency
import com.ppp.currencyexchange.data.model.calculatePppValue
import com.ppp.currencyexchange.data.model.currencies
import com.ppp.currencyexchange.data.repository.ConversionHistoryRepository
import com.ppp.currencyexchange.data.repository.ExchangeRateRepository
import com.ppp.currencyexchange.data.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val application: Application,
    private val repository: ExchangeRateRepository,
    private val favoritesRepository: FavoritesRepository,
    private val historyRepository: ConversionHistoryRepository,
    private val settingsDataStore: SettingsDataStore,
    private val gson: Gson
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val decimalPlaces = settingsDataStore.decimalPlaces.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 2
    )

    private var autoRefreshJob: Job? = null
    private var hasLoadedInitialData = false

    init {
        viewModelScope.launch {
            val isOnline = settingsDataStore.isOnline.first()
            val lastFrom = settingsDataStore.lastFromCurrency.first()
            val lastTo = settingsDataStore.lastToCurrency.first()
            val fromCurrency = currencies.find { it.code == lastFrom } ?: currencies.first { it.code == "USD" }
            val toCurrency = currencies.find { it.code == lastTo } ?: currencies.first { it.code == "INR" }

            _uiState.update {
                it.copy(
                    isOnline = isOnline,
                    fromCurrency = fromCurrency,
                    toCurrency = toCurrency
                )
            }

            observeFavorites()
            observeHistory()
            loadManualPairRates()

            if (isOnline) {
                fetchRates()
                startAutoRefresh()
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }

            hasLoadedInitialData = true
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun toggleOnlineMode() {
        viewModelScope.launch {
            val newMode = !_uiState.value.isOnline
            _uiState.update { it.copy(isOnline = newMode, error = null) }
            settingsDataStore.setIsOnline(newMode)

            if (newMode) {
                fetchRates()
                startAutoRefresh()
            } else {
                autoRefreshJob?.cancel()
                _uiState.update { it.copy(lastUpdated = null) }
            }
        }
    }

    fun onAmountChanged(amount: String) {
        val cleaned = amount.replace(",", ".")
        if (cleaned.isEmpty() || cleaned.matches(Regex("^\\d*\\.?\\d{0,10}$"))) {
            _uiState.update { it.copy(amount = cleaned, inputError = null) }
            performConversion()
        }
    }

    fun onManualRateChanged(rate: String) {
        if (rate.isEmpty() || rate.matches(Regex("^\\d*\\.?\\d{0,6}$"))) {
            _uiState.update { it.copy(manualRateInput = rate) }
            performConversion()
        }
    }

    fun saveManualRate() {
        val state = _uiState.value
        val rateText = state.manualRateInput
        val rate = rateText.toDoubleOrNull() ?: return
        val pairKey = "${state.fromCurrency.code}_${state.toCurrency.code}"

        val updatedRates = state.manualPairRates + (pairKey to rate)

        viewModelScope.launch {
            val json = gson.toJson(updatedRates)
            settingsDataStore.saveManualPairRatesJson(json)
            _uiState.update {
                it.copy(manualPairRates = updatedRates, manualRateInput = "")
            }
        }
    }

    fun showCurrencyPicker(role: CurrencyPickerRole) {
        _uiState.update {
            it.copy(showCurrencyPicker = true, currencyPickerRole = role, currencyPickerSearchQuery = "")
        }
    }

    fun hideCurrencyPicker() {
        _uiState.update { it.copy(showCurrencyPicker = false) }
    }

    fun onCurrencyPickerSearchQueryChanged(query: String) {
        _uiState.update { it.copy(currencyPickerSearchQuery = query) }
    }

    fun selectCurrencyFromPicker(currency: Currency) {
        val role = _uiState.value.currencyPickerRole
        if (role == CurrencyPickerRole.FROM) {
            onFromCurrencyChanged(currency)
        } else {
            onToCurrencyChanged(currency)
        }
        hideCurrencyPicker()
    }

    fun toggleFavorite() {
        val state = _uiState.value
        val from = state.fromCurrency.code
        val to = state.toCurrency.code
        viewModelScope.launch {
            if (state.isCurrentPairFavorite) {
                favoritesRepository.removeFavorite(from, to)
            } else {
                favoritesRepository.addFavorite(from, to)
            }
        }
    }

    fun useFavoritePair(pair: FavoritePair) {
        _uiState.update {
            it.copy(fromCurrency = pair.fromCurrency, toCurrency = pair.toCurrency)
        }
        rememberCurrencies()
        performConversion()
    }

    fun copyResult() {
        val state = _uiState.value
        val result = state.convertedAmount
        if (result.isNotBlank()) {
            val clipboard = application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Converted Amount", "$result ${state.toCurrency.code}")
            clipboard.setPrimaryClip(clip)
            _uiState.update { it.copy(copiedToClipboard = true) }
            viewModelScope.launch {
                delay(2000)
                _uiState.update { it.copy(copiedToClipboard = false) }
            }
        }
    }

    fun shareResult() {
        val state = _uiState.value
        val result = state.convertedAmount
        val fromAmount = state.amount
        val fromCode = state.fromCurrency.code
        val toCode = state.toCurrency.code
        if (result.isNotBlank()) {
            val text = "$fromAmount $fromCode = $result $toCode"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            application.startActivity(Intent.createChooser(intent, "Share Conversion"))
        }
    }

    fun clearHistory() {
        viewModelScope.launch { historyRepository.clearHistory() }
    }

    fun onFromCurrencyChanged(currency: Currency) {
        _uiState.update { it.copy(fromCurrency = currency) }
        rememberCurrencies()
        performConversion()
    }

    fun onToCurrencyChanged(currency: Currency) {
        _uiState.update { it.copy(toCurrency = currency) }
        rememberCurrencies()
        performConversion()
    }

    fun swapCurrencies() {
        _uiState.update {
            it.copy(fromCurrency = it.toCurrency, toCurrency = it.fromCurrency)
        }
        rememberCurrencies()
        performConversion()
    }

    fun refreshRates() {
        if (_uiState.value.isOnline) {
            _uiState.update { it.copy(isRefreshing = true) }
            fetchRates()
        }
    }

    fun onPppAmountChanged(amount: String) {
        val cleaned = amount.replace(",", ".")
        if (cleaned.isEmpty() || cleaned.matches(Regex("^\\d*\\.?\\d{0,10}$"))) {
            _uiState.update { it.copy(pppAmount = cleaned) }
            calculatePpp()
        }
    }

    fun onPppCurrencyChanged(currency: Currency) {
        _uiState.update { it.copy(pppCurrency = currency) }
        calculatePpp()
    }

    private fun calculatePpp() {
        val state = _uiState.value
        val amountText = state.pppAmount
        if (amountText.isBlank() || amountText == ".") {
            _uiState.update { it.copy(pppResult = "", pppMarketComparison = "") }
            return
        }
        val amount = amountText.toDoubleOrNull() ?: run {
            _uiState.update { it.copy(pppResult = "", pppMarketComparison = "") }
            return
        }

        val pppValue = calculatePppValue(amount, state.pppCurrency.code)
        if (pppValue == null) {
            _uiState.update { it.copy(pppResult = "", pppMarketComparison = "") }
            return
        }

        val marketRates = state.rates
        val marketInrRate = marketRates["INR"] ?: 0.0
        val marketFromRate = if (state.pppCurrency.code == "USD") 1.0
            else marketRates[state.pppCurrency.code]
        val marketValue = if (marketFromRate != null && marketInrRate > 0) {
            amount / marketFromRate * marketInrRate
        } else null

        val places = decimalPlaces.value
        val formattedPpp = formatAmount(pppValue, "INR", places)
        val formattedMarket = if (marketValue != null) formatAmount(marketValue, "INR", places) else "N/A"

        _uiState.update {
            it.copy(
                pppResult = "\u20B9$formattedPpp",
                pppMarketComparison = if (marketValue != null) "\u20B9$formattedMarket" else "N/A"
            )
        }
    }

    private fun performConversion() {
        val state = _uiState.value
        val amountText = state.amount

        if (amountText.isBlank() || amountText == "." || amountText == "-") {
            _uiState.update { it.copy(convertedAmount = "", inputError = null) }
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null) {
            _uiState.update { it.copy(convertedAmount = "", inputError = "Invalid amount") }
            return
        }

        if (amount < 0) {
            _uiState.update { it.copy(convertedAmount = "", inputError = "Amount cannot be negative") }
            return
        }

        if (amount > 999999999999.0) {
            _uiState.update { it.copy(convertedAmount = "", inputError = "Amount too large") }
            return
        }

        val rate = calculateRate(state)
        if (rate == null) {
            _uiState.update { it.copy(convertedAmount = "", inputError = "Rate not available. Enter a rate below.") }
            return
        }

        val converted = amount * rate
        val places = decimalPlaces.value
        val formatted = formatAmount(converted, state.toCurrency.code, places)
        _uiState.update { it.copy(convertedAmount = formatted, inputError = null) }

        if (hasLoadedInitialData) {
            viewModelScope.launch {
                historyRepository.addEntry(
                    fromCurrency = state.fromCurrency.code,
                    toCurrency = state.toCurrency.code,
                    fromAmount = amount,
                    toAmount = converted,
                    rate = rate
                )
            }
        }
    }

    private fun calculateRate(state: HomeUiState): Double? {
        val fromCode = state.fromCurrency.code
        val toCode = state.toCurrency.code
        if (fromCode == toCode) return 1.0

        if (state.isOnline) {
            val rates = state.rates
            if (rates.isEmpty()) return null
            return when {
                rates.containsKey(fromCode) && rates.containsKey(toCode) -> rates[toCode]!! / rates[fromCode]!!
                rates.containsKey(toCode) -> rates[toCode]!!
                rates.containsKey(fromCode) -> 1.0 / rates[fromCode]!!
                else -> null
            }
        }

        val pairKey = "${fromCode}_${toCode}"
        return state.manualPairRates[pairKey]
    }

    fun formatAmount(amount: Double, currencyCode: String, places: Int): String {
        val actualPlaces = if (currencyCode == "JPY") 0 else places
        return String.format(Locale.US, "%,.${actualPlaces}f", amount)
    }

    private fun formatErrorMessage(error: Throwable): String {
        val msg = error.message ?: ""
        return when {
            msg.contains("Unable to resolve host") || msg.contains("No address") ->
                "No internet connection. Please check your network and try again."
            msg.contains("timeout", ignoreCase = true) -> "Request timed out. Please try again."
            msg.contains("400") || msg.contains("401") || msg.contains("403") || msg.contains("50") ->
                "Server issue. Please try again later."
            else -> "Something went wrong. $msg"
        }
    }

    private fun rememberCurrencies() {
        viewModelScope.launch {
            val s = _uiState.value
            settingsDataStore.setLastCurrencies(s.fromCurrency.code, s.toCurrency.code)
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            favoritesRepository.observeFavorites().collect { favorites ->
                val pairs = favorites.mapNotNull { fave ->
                    val from = currencies.find { it.code == fave.fromCurrency }
                    val to = currencies.find { it.code == fave.toCurrency }
                    if (from != null && to != null) FavoritePair(from, to) else null
                }
                val s = _uiState.value
                val isFav = favorites.any {
                    it.fromCurrency == s.fromCurrency.code && it.toCurrency == s.toCurrency.code
                }
                _uiState.update { it.copy(favorites = pairs, isCurrentPairFavorite = isFav) }
            }
        }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            historyRepository.observeRecentHistory().collect { entries ->
                val history = entries.mapNotNull { entry ->
                    val from = currencies.find { it.code == entry.fromCurrency }
                    val to = currencies.find { it.code == entry.toCurrency }
                    if (from != null && to != null) {
                        val df = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                        HistoryEntry(
                            fromCurrency = from, toCurrency = to,
                            fromAmount = entry.fromAmount, toAmount = entry.toAmount,
                            rate = entry.rate, timestamp = df.format(Date(entry.timestamp))
                        )
                    } else null
                }
                _uiState.update { it.copy(history = history) }
            }
        }
    }

    private fun loadManualPairRates() {
        viewModelScope.launch {
            val json = settingsDataStore.manualPairRates.first()
            try {
                val type = object : TypeToken<Map<String, Double>>() {}.type
                val rates: Map<String, Double> = gson.fromJson(json, type)
                _uiState.update { it.copy(manualPairRates = rates) }
            } catch (_: Exception) {
                _uiState.update { it.copy(manualPairRates = emptyMap()) }
            }
        }
    }

    private fun fetchRates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getLatestRates().collect { result ->
                result.fold(
                    onSuccess = { rates ->
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                        _uiState.update {
                            it.copy(
                                rates = rates,
                                isLoading = false,
                                isRefreshing = false,
                                error = null,
                                lastUpdated = dateFormat.format(Date())
                            )
                        }
                        performConversion()
                        calculatePpp()
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = formatErrorMessage(error)
                            )
                        }
                    }
                )
            }
        }
    }

    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            val enabled = settingsDataStore.autoUpdate.first()
            if (!enabled) return@launch
            val frequency = settingsDataStore.updateFrequency.first()
            while (true) {
                delay(frequency * 60 * 1000L)
                if (_uiState.value.isOnline) fetchRates()
            }
        }
    }
}
