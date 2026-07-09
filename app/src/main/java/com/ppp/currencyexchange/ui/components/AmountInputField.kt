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
import com.ppp.currencyexchange.data.model.formatWithSymbol
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
    var lastRawAndSymbol by remember { mutableStateOf(Triple("", "", "")) }

    val formatted = remember(rawAmount, currencySymbol, currencyCode, decimalPlaces) {
        if (rawAmount.isEmpty()) "" else formatWithSymbol(rawAmount, currencySymbol, currencyCode, decimalPlaces)
    }

    val key = Triple(rawAmount, currencySymbol, currencyCode)
    if (key != lastRawAndSymbol) {
        lastRawAndSymbol = key
        textFieldValue = TextFieldValue(formatted, selection = TextRange(formatted.length))
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            val raw = extractRawAmount(newValue.text)
            if (isValidRaw(raw)) {
                val formattedText = if (raw.isEmpty()) "" else formatWithSymbol(raw, currencySymbol, currencyCode, decimalPlaces)
                val rawBeforeCursor = extractRawAmount(newValue.text.substring(0, newValue.selection.start))
                val cursorInRaw = rawBeforeCursor.length.coerceAtMost(raw.length)
                val symbolLen = if (raw.isNotEmpty()) {
                    val sym = if (currencyCode == "AED") " $currencySymbol" else currencySymbol
                    sym.length
                } else 0
                val rawPrefix = raw.substring(0, cursorInRaw)
                val formattedPrefixLen = if (rawPrefix.isEmpty()) 0 else formatNumber(rawPrefix, currencyCode, decimalPlaces).length
                val cursorPos = symbolLen + formattedPrefixLen
                textFieldValue = TextFieldValue(formattedText, selection = TextRange(cursorPos.coerceAtMost(formattedText.length)))
                lastRawAndSymbol = Triple(raw, currencySymbol, currencyCode)
                onRawAmountChange(raw)
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