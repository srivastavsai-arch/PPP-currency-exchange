package com.ppp.currencyexchange.data.model

fun formatNumber(raw: String, currencyCode: String, decimalPlaces: Int = 2): String {
    if (raw.isEmpty()) return ""
    if (raw == ".") return "."
    val parts = raw.split(".")
    val intPart = parts[0]
    val decPart = if (parts.size > 1) parts[1] else ""
    val formattedInt = if (currencyCode == "INR") formatIndian(intPart) else formatInternational(intPart)
    val formattedDec = when {
        decPart.isNotEmpty() -> "." + decPart.take(decimalPlaces)
        raw.endsWith(".") -> "."
        else -> ""
    }
    return formattedInt + formattedDec
}

private fun formatIndian(intPart: String): String {
    if (intPart.isEmpty()) return ""
    if (intPart.length <= 3) return intPart
    val lastThree = intPart.takeLast(3)
    val rest = intPart.dropLast(3)
    val parts = rest.reversed().chunked(2)
    val reversed = mutableListOf<String>()
    for (p in parts) reversed.add(p.reversed().toString())
    val restFormatted = reversed.reversed().joinToString(",")
    return "$restFormatted,$lastThree"
}

private fun formatInternational(intPart: String): String {
    if (intPart.isEmpty()) return ""
    val parts = intPart.reversed().chunked(3)
    val reversed = mutableListOf<String>()
    for (p in parts) reversed.add(p.reversed().toString())
    return reversed.reversed().joinToString(",")
}

fun formatWithSymbol(raw: String, symbol: String, currencyCode: String, decimalPlaces: Int = 2): String {
    if (raw.isEmpty()) return ""
    val formatted = formatNumber(raw, currencyCode, decimalPlaces)
    return if (currencyCode == "AED") "$formatted $symbol" else "$symbol$formatted"
}

fun extractRawAmount(displayText: String): String {
    return displayText.filter { it.isDigit() || it == '.' }
}

fun isValidRaw(raw: String): Boolean {
    if (raw.isEmpty()) return true
    if (raw.count { it == '.' } > 1) return false
    val digits = raw.replace(".", "")
    if (digits.length > 15) return false
    return raw.matches(Regex("^\\d*\\.?\\d*$"))
}

fun getCurrencySymbol(code: String): String {
    return currencies.find { it.code == code }?.symbol ?: code
}
