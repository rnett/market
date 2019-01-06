package com.rnett.ligraph.eve.market.updaters

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.get
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.rnett.core.launchInAndJoinAll
import com.rnett.ligraph.eve.market.connectToDB
import com.rnett.ligraph.eve.market.data.WatchedRegions
import com.rnett.ligraph.eve.market.data.marketprices
import com.rnett.ligraph.eve.market.publishedItemIds
import com.soywiz.klock.DateTime
import io.ktor.client.HttpClient
import io.ktor.client.features.BadResponseStatusException
import io.ktor.client.request.get
import io.ktor.http.contentLength
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.io.readFully
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.transactions.transaction

private val client = HttpClient()
private val fuzzworksSizeLimit = 100

data class FuzzworksMarketDataSingle(
    val weightedAverage: Double,
    val max: Double, val min: Double,
    val stddev: Double, val median: Double,
    val volume: Double, val orderCount: Long,
    val percentile: Double
)

data class FuzzworksMarketData(val buy: FuzzworksMarketDataSingle, val sell: FuzzworksMarketDataSingle)

suspend fun getFuzzworksData(types: List<Int>, regionId: Int): Map<Int, FuzzworksMarketData> {
    val url = "https://market.fuzzwork.co.uk/aggregates/?region=$regionId&types=${types.joinToString(",")}"
    return try {
        client.get<String>(url)
            .let {
                Gson().fromJson(it)
            }
    } catch (e: BadResponseStatusException) {
        System.err.println("Exception on URL: $url")
        System.err.println("Response: ${e.response}")

        val arr = ByteArray(e.response.contentLength()?.toInt() ?: 0)
        e.response.content.readFully(arr)
        System.err.println("Content: ${String(arr)}")
        throw e
    }
}

suspend fun addDataFor(typeIds: List<Int>, regionId: Int, date: String, time: String){
    if(typeIds.size > fuzzworksSizeLimit)
        typeIds.chunked(fuzzworksSizeLimit).forEach { addDataFor(it, regionId, date, time) }
    else {
        val data = getFuzzworksData(typeIds, regionId).toList()
        transaction {
            marketprices.apply {
                marketprices.batchInsert(data, false) {
                    this[typeid] = it.first
                    this[regionid] = regionId
                    this[marketprices.date] = date
                    this[marketprices.time] = time

                    it.second.sell.let {
                        this[sellmin] = it.min
                        this[sellmax] = it.max
                        this[sellaverage] = it.weightedAverage
                        this[sellordervol] = it.orderCount
                        this[sellsd] = it.stddev
                        this[sellpercentile] = it.percentile
                    }

                    it.second.buy.let {
                        this[buymin] = it.min
                        this[buymax] = it.max
                        this[buyaverage] = it.weightedAverage
                        this[buyordervol] = it.orderCount
                        this[buysd] = it.stddev
                        this[buypercentile] = it.percentile
                    }
                }
            }
        }
    }
}

fun addAllPrices() = runBlocking(context = Dispatchers.Default){
    val now = DateTime.now()
    val date = now.toString("yyyy-MM-dd")
    val time = now.toString("HH:mm")

    val regions = WatchedRegions.watchedRegions.size

    println("Starting update for ${publishedItemIds.size} items in $regions regions at $date $time")

    var regionsLeft = regions

    WatchedRegions.watchedRegions.forEach {
        publishedItemIds.chunked(fuzzworksSizeLimit).launchInAndJoinAll(this) { items ->
            addDataFor(items, it, date, time)
        }
        regionsLeft--
        println("Finished update for region $it.  $regionsLeft left (${100 * regionsLeft / regions}%)")
    }

    println("Finished update.")
}
