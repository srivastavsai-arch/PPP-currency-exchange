package com.ppp.currencyexchange.data.remote

import com.google.gson.annotations.SerializedName

data class ExchangeRateResponse(
    @SerializedName("date")
    val date: String,
    @SerializedName("usd")
    val rates: Map<String, Double>
)
