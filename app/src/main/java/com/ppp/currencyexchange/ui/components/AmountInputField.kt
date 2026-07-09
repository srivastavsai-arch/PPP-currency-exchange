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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
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
    var textFieldValue by remember { mutableStateOf("") }
    var lastExternalRaw by remember { mutableStateOf("") }

    val cleanRaw = extractRawAmount(rawAmount)
    if (cleanRaw != lastExternalRaw) {
        lastExternalRaw = cleanRaw
        textFieldValue = cleanRaw
    }

    val transformation = remember(currencySymbol, currencyCode, decimalPlaces) {
        CurrencyVisualTransformation(currencySymbol, currencyCode, decimalPlaces)
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            val raw = extractRawAmount(newValue)
            if (isValidRaw(raw)) {
                textFieldValue = raw
                lastExternalRaw = raw
                onRawAmountChange(raw)
            }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        visualTransformation = transformation,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

private class CurrencyVisualTransformation(
    private val currencySymbol: String,
    private val currencyCode: String,
    private val decimalPlaces: Int
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = extractRawAmount(text.text)
        if (raw.isEmpty()) return TransformedText(text, OffsetMapping.Identity)
        val formattedInt = formatNumber(raw, currencyCode, decimalPlaces)
        val sym = if (currencyCode == "AED") " $currencySymbol" else currencySymbol
        val formatted = "$sym$formattedInt"
        val annotated = AnnotatedString(formatted)
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val rawPrefix = text.text.filter { it.isDigit() || it == '.' }.take(offset)
                val formattedPrefix = formatNumber(rawPrefix, currencyCode, decimalPlaces)
                return sym.length + formattedPrefix.length
            }
            override fun transformedToOriginal(offset: Int): Int {
                return extractRawAmount(formatted.take(offset)).length
            }
        }
        return TransformedText(annotated, offsetMapping)
    }
}