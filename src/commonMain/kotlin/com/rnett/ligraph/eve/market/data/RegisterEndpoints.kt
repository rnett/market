package com.rnett.ligraph.eve.market.data


import com.rnett.kframe.data.EndpointManager
import com.rnett.kframe.data.addEndpoint
import com.rnett.ligraph.eve.market.data.marketinfo
import kotlinx.serialization.internal.LongSerializer
import kotlinx.serialization.list

fun registerEndpoints() {
    EndpointManager.addEndpoint(marketinfo.Companion::getItem, marketinfo, LongSerializer)
    EndpointManager.addEndpoint(marketinfo.Companion::allItems, marketinfo.list)

}
