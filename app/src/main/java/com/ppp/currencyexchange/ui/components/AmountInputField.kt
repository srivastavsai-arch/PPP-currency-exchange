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
    val displayValue = remember(rawAmount, currencySymbol, currencyCode, decimalPlaces) {
        if (rawAmount.isEmpty()) "" else formatWithSymbol(rawAmount, currencySymbol, currencyCode, decimalPlaces)
    }

    var textFieldValue by remember(displayValue) {
        mutableStateOf(
            TextFieldValue(displayValue, selection = TextRange(displayValue.length))
        )
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            val raw = extractRawAmount(newValue.text)
            if (isValidRaw(raw)) {
                val formatted = if (raw.isEmpty()) "" else formatWithSymbol(raw, currencySymbol, currencyCode, decimalPlaces)
                val cursorAtEnd = newValue.text.length == newValue.selection.end
                val newCursorPos = if (cursorAtEnd || raw.isEmpty()) formatted.length
                    else newValue.selection.end.coerceAtMost(formatted.length)
                textFieldValue = TextFieldValue(formatted, selection = TextRange(newCursorPos))
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
