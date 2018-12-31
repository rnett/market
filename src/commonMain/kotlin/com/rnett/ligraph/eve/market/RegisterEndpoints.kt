package com.rnett.ligraph.eve.market

import com.rnett.kframe.data.EndpointManager
import com.rnett.kframe.data.addEndpoint
import com.rnett.ligraph.eve.market.data.MarketTypeInfo
import com.rnett.ligraph.eve.market.data.getMarketInfo
import com.soywiz.klock.DateTime
import kotlinx.serialization.Serializer

@Serializer(forClass = DateTime::class)
object DateTimeSerializer

fun registerEndpoints() {
    EndpointManager.apply {
        addEndpoint(::getMarketInfo, MarketTypeInfo.serializer(), DateTimeSerializer)
    }
}
