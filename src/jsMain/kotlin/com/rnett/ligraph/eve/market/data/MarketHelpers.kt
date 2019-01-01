package com.rnett.ligraph.eve.market.data

import io.ktor.client.HttpClient

val client = HttpClient()

//TODO cache (need to make core multiplatform)
/*

actual fun getMarketInfo(type: invtype, date: DateTime): MarketTypeInfo =
    callEndpoint(::getMarketInfo, client, type, date)*/
