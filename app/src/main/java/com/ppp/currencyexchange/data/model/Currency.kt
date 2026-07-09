package com.ppp.currencyexchange.data.model

data class Currency(
    val code: String,
    val name: String,
    val symbol: String
)

val currencies = listOf(
    Currency("INR", "Indian Rupee", "\u20B9"),
    Currency("USD", "US Dollar", "$"),
    Currency("EUR", "Euro", "\u20AC"),
    Currency("GBP", "British Pound", "\u00A3"),
    Currency("CAD", "Canadian Dollar", "C$"),
    Currency("AUD", "Australian Dollar", "A$"),
    Currency("JPY", "Japanese Yen", "\u00A5"),
    Currency("CNY", "Chinese Yuan", "\u00A5"),
    Currency("AED", "UAE Dirham", "Dhs"),
    Currency("SGD", "Singapore Dollar", "S$"),
    Currency("CHF", "Swiss Franc", "CHF"),
    Currency("SAR", "Saudi Riyal", "SR")
)
