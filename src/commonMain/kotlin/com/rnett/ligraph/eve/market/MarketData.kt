package com.rnett.ligraph.eve.market

import kotlinx.serialization.Serializable

@Serializable
data class MarketPrices(
    val buy: MarketPricesTyped,
    val sell: MarketPricesTyped
)

@Serializable
data class MarketPricesTyped(
    val min: Double,
    val max: Double,
    val average: Double,
    val stdDev: Double,
    val percentile: Double,
    val orderCount: Double
)