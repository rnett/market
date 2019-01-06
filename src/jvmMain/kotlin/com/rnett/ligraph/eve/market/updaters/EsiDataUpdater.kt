package com.rnett.ligraph.eve.market.updaters

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.get
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.rnett.core.launchInAndJoinAll
import com.rnett.ligraph.eve.market.MarketPrices
import com.rnett.ligraph.eve.market.MarketPricesTyped
import com.rnett.ligraph.eve.market.data.WatchedRegions
import com.rnett.ligraph.eve.market.data.marketinfo
import com.rnett.ligraph.eve.market.data.marketprice
import com.rnett.ligraph.eve.market.data.marketprices
import com.rnett.ligraph.eve.market.publishedItemIds
import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

private val client = HttpClient()

data class EsiMarketData(
    val average: Double, val date: String,
    val highest: Double, val lowest: Double,
    @SerializedName("order_count") val orderCount: Long, val volume: Long
)

suspend fun getEsiDataFor(type: Int, regionId: Int, date: String) =
    client.get<String>("https://esi.evetech.net/latest/markets/$regionId/history/?datasource=tranquility&type_id=$type").let {
        JsonParser().parse(it).asJsonArray.find{ it["date"].asString == date }.toString().let {
            Gson().fromJson<EsiMarketData>(it)
        }
    }

inline fun <T> Iterable<T>.averageBy(value: (T) -> Double) = sumByDouble(value) / this.count()
inline fun <T> Iterable<T>.averageByLong(value: (T) -> Long) = map(value).sum().toDouble() / this.count()

suspend fun averageAndDeletePriceData(type: Int, regionId: Int, date: String): MarketPrices {
    val prices = transaction{
        marketprice.find { marketprices.regionid eq regionId and(marketprices.typeid eq type) and(marketprices.date eq date) }.toList()
    }

    val data = MarketPrices(
        MarketPricesTyped(
            prices.averageBy { it.buymin },
            prices.averageBy { it.buymax },
            prices.averageBy { it.buyaverage },
            prices.averageBy { it.buysd },
            prices.averageBy { it.buypercentile },
            prices.averageByLong { it.buyordervol }
        ),
        MarketPricesTyped(
            prices.averageBy { it.sellmin },
            prices.averageBy { it.sellmax },
            prices.averageBy { it.sellaverage },
            prices.averageBy { it.sellsd },
            prices.averageBy { it.sellpercentile },
            prices.averageByLong { it.sellordervol }
        )
    )

    transaction{
        marketprices.deleteWhere { marketprices.regionid eq regionId and(marketprices.typeid eq type) and(marketprices.date eq date) }
        marketprices.deleteWhere { marketprices.date lessEq date }
    }

    return data
}

data class TransactionVolumes(val buyVol: Long, val sellVol: Long)

fun getTransactionVol(buy: Double, sell: Double, average: Double, volume: Long): TransactionVolumes {
    val sellVol = ((average - buy) * volume / (sell - buy)).toLong()
    return TransactionVolumes(volume - sellVol, sellVol)

}

suspend fun addMarketInfo(type: Int, regionId: Int, date: String){
    val data = averageAndDeletePriceData(type, regionId, date)

    val esiData = getEsiDataFor(type, regionId, date)

    val transactionVols = getTransactionVol(data.buy.max, data.sell.min, esiData.average, esiData.volume)

    transaction{
        marketinfo.new(
            type,
            regionId,
            date,
            
            data.sell.min,
            data.sell.max,
            data.sell.average,
            data.sell.orderCount.toLong(),
            transactionVols.sellVol,
            data.sell.stdDev,
            data.sell.percentile,

            data.buy.min,
            data.buy.max,
            data.buy.average,
            data.buy.orderCount.toLong(),
            transactionVols.buyVol,
            data.buy.stdDev,
            data.buy.percentile,
            0
        )
    }
}

fun addAllHistoryData() = runBlocking(context = Dispatchers.Default) {
    val yesterday = DateTime.now() - 1.days
    val date = yesterday.toString("yyyy-MM-dd")

    val regions = WatchedRegions.watchedRegions.size

    println("Starting update for ${publishedItemIds.size} items in $regions regions for $date")

    var regionsLeft = regions

    WatchedRegions.watchedRegions.forEach {
        publishedItemIds.launchInAndJoinAll(this) { item ->
            addMarketInfo(item, it, date)
        }
        regionsLeft--
        println("Finished update for region $it.  $regionsLeft left (${100 * regionsLeft / regions}%)")
    }

    println("Finished update.")
}