package com.ppp.currencyexchange.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.ppp.currencyexchange.data.model.extractRawAmount
import com.ppp.currencyexchange.data.model.formatNumber
import com.ppp.currencyexchange.data.model.isValidRaw

@Composable
fun AmountInputField(
    rawAmount: String,
    onRawAmountChange: (String) -> Unit,
    currencySymbol: String = "",
    currencyCode: String = "USD",
    decimalPlaces: Int = 2,
    label: String = "Amount",
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var lastExternalRaw by remember { mutableStateOf("") }

    val cleanExternal = extractRawAmount(rawAmount)
    if (cleanExternal != lastExternalRaw) {
        lastExternalRaw = cleanExternal
        val formatted = if (cleanExternal.isEmpty()) ""
        else formatDisplay(cleanExternal, currencySymbol, currencyCode, decimalPlaces)
        textFieldValue = TextFieldValue(formatted, selection = TextRange(formatted.length))
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            val text = newValue.text
            val cursorPos = newValue.selection.start.coerceAtMost(text.length)
            val rawCharsBeforeCursor = text.substring(0, cursorPos).count { c -> c.isDigit() || c == '.' }
            val newRaw = extractRawAmount(text)
            if (isValidRaw(newRaw) && newRaw.length <= 15) {
                val formatted = if (newRaw.isEmpty()) "" else formatDisplay(newRaw, currencySymbol, currencyCode, decimalPlaces)
                val newCursor = cursorInFormatted(formatted, rawCharsBeforeCursor, currencySymbol, currencyCode)
                textFieldValue = TextFieldValue(formatted, selection = TextRange(newCursor.coerceAtMost(formatted.length)))
                lastExternalRaw = newRaw
                onRawAmountChange(newRaw)
            }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

private fun formatDisplay(raw: String, symbol: String, code: String, places: Int): String {
    val formatted = formatNumber(raw, code, places)
    return if (code == "AED") "$formatted $symbol" else "$symbol$formatted"
}

private fun cursorInFormatted(formatted: String, rawCharsBeforeCursor: Int, symbol: String, code: String): Int {
    val symLen = if (code == "AED") 0 else symbol.length
    val body = formatted.substring(symLen.coerceAtMost(formatted.length))
    var count = 0
    var pos = 0
    while (pos < body.length && count < rawCharsBeforeCursor) {
        val c = body[pos]
        if (c == '.' || c.isDigit()) count++
        pos++
    }
    return symLen + pos
}