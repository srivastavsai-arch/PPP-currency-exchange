package com.ppp.currencyexchange.data.remote

import retrofit2.http.GET

interface ExchangeRateApi {
    @GET("v1/currencies/usd.json")
    suspend fun getLatestRates(): ExchangeRateResponse
}
