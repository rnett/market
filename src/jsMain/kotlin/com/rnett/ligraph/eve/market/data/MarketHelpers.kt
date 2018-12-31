package com.rnett.ligraph.eve.market.data

import com.rnett.kframe.data.callEndpoint
import com.rnett.ligraph.eve.sde.data.invtype
import com.soywiz.klock.DateTime
import io.ktor.client.HttpClient

val client = HttpClient()

actual fun getMarketInfo(type: invtype, date: DateTime): MarketTypeInfo =
    callEndpoint(::getMarketInfo, client, type, date)