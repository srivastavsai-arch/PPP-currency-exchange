package com.ppp.currencyexchange.data.model

data class PppFactor(
    val currencyCode: String,
    val ratePerInternationalDollar: Double,
    val source: String = "World Bank (2023)",
    val lastUpdated: String = "2023"
)

val pppFactors = listOf(
    PppFactor("USD", 1.0),
    PppFactor("INR", 22.0),
    PppFactor("EUR", 0.73),
    PppFactor("GBP", 0.65),
    PppFactor("CAD", 1.24),
    PppFactor("AUD", 1.44),
    PppFactor("JPY", 108.0),
    PppFactor("CNY", 4.12),
    PppFactor("AED", 3.67),
    PppFactor("SGD", 0.93),
    PppFactor("CHF", 0.98),
    PppFactor("SAR", 3.75)
)

fun getPppRate(fromCode: String, toCode: String): Double? {
    val from = pppFactors.find { it.currencyCode == fromCode } ?: return null
    val to = pppFactors.find { it.currencyCode == toCode } ?: return null
    return to.ratePerInternationalDollar / from.ratePerInternationalDollar
}

fun calculatePppValue(amount: Double, fromCode: String): Double? {
    val fromFactor = pppFactors.find { it.currencyCode == fromCode }?.ratePerInternationalDollar ?: return null
    val inrFactor = pppFactors.find { it.currencyCode == "INR" }?.ratePerInternationalDollar ?: return null
    return amount / fromFactor * inrFactor
}

fun calculateReversePppValue(amount: Double, toCode: String): Double? {
    val toFactor = pppFactors.find { it.currencyCode == toCode }?.ratePerInternationalDollar ?: return null
    val inrFactor = pppFactors.find { it.currencyCode == "INR" }?.ratePerInternationalDollar ?: return null
    return amount / inrFactor * toFactor
}
