package com.ppp.currencyexchange.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_rates")
data class ExchangeRateEntity(
    @PrimaryKey
    val baseCurrency: String,
    val ratesJson: String,
    val lastUpdated: Long
)
