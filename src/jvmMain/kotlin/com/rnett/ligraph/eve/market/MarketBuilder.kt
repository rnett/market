package com.rnett.ligraph.eve.market

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.rnett.ligraph.eve.sde.data.invtype
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

private val client = HttpClient()

data class EsiMarketData(
    val average: Double, val date: String,
    val highest: Double, val lowest: Double,
    @SerializedName("order_count") val orderCount: Long, val volume: Long
)

suspend fun getRecentEsiDataFor(type: Int, regionID: Int) =
    client.get<String>("https://esi.evetech.net/latest/markets/$regionID/history/?datasource=tranquility&type_id=$type").let {
        JsonParser().parse(it).asJsonArray.last().toString().let {
            Gson().fromJson<EsiMarketData>(it)
        }
    }

data class FuzzworksMarketDataSingle(
    val weightedAverage: Double,
    val max: Double, val min: Double,
    val stddev: Double, val median: Double,
    val volume: Double, val orderCount: Int,
    val percentile: Double
)

data class FuzzworksMarketData(val buy: FuzzworksMarketDataSingle, val sell: FuzzworksMarketDataSingle)

suspend fun getFuzzworksData(types: List<Int>, regionID: Int): Map<Int, FuzzworksMarketData> =
    client.get<String>("https://market.fuzzwork.co.uk/aggregates/?region=$regionID&types=${types.joinToString(",")}").let {
        Gson().fromJson(it)
    }

fun getDataForAll(types: List<Int>, regionID: Int) =
    runBlocking(Dispatchers.Default) {
        val f = types.chunked(10).map {
            async { getFuzzworksData(it, regionID) }
        }
        val e = types.associate {
            it to async { getRecentEsiDataFor(it, regionID) }
        }

        val esi = e.mapValues { it.value.await() }
        val fuzzworks = f.map { it.await() }.reduce { acc, map -> acc + map }

        types.associate { it to Pair(esi[it]!!, fuzzworks[it]!!) }
    }

fun Map<Int, Pair<EsiMarketData, FuzzworksMarketData>>.toMarketInfos(date: String) =
    mapValues {
        val vols = getTransactionVol(
            it.value.second.buy.max,
            it.value.second.sell.min,
            it.value.first.average,
            it.value.first.volume
        )

        MarketTypeInfo(
            invtype[it.key], date,
            it.value.let {
                PriceInfo(
                    it.second.sell.min, it.second.sell.max, it.second.sell.weightedAverage,
                    it.second.sell.orderCount.toLong(),
                    vols.sellVol
                )
            },
            it.value.let {
                PriceInfo(
                    it.second.buy.min, it.second.buy.max, it.second.buy.weightedAverage,
                    it.second.buy.orderCount.toLong(),
                    vols.buyVol
                )
            },
            0//TODO destroyed
        )
    }

data class TransactionVolumes(val buyVol: Long, val sellVol: Long)

fun getTransactionVol(buy: Double, sell: Double, average: Double, volume: Long): TransactionVolumes {
    val sellVol = ((average - buy) * volume / (sell - buy)).toLong()
    return TransactionVolumes(volume - sellVol, sellVol)

}

fun main() {
    val r = getDataForAll(listOf(34, 35, 36), 10000002)
    println()
}