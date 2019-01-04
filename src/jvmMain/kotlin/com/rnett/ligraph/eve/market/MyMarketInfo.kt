package com.rnett.ligraph.eve.market

import com.rnett.ligraph.eve.market.data.marketinfo
import com.rnett.ligraph.eve.market.data.marketinfos
import com.rnett.ligraph.eve.sde.data.invtype
import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

fun getMarketInfo(type: invtype, date: DateTime): marketinfo = transaction {
    marketinfo.find { (marketinfos.typeid eq type.typeID) and (marketinfos.date eq date.toString("")) }.first()
}

fun marketinfo.toMarketTypeInfo() = transaction {
    MarketTypeInfo(
        invtype[typeid],
        date,
        PriceInfo(sellmin, sellmax, sellaverage, sellordervol, selltransactionvol),
        PriceInfo(buymin, buymax, buyaverage, buyordervol, buytransactionvol),
        destroyed
    )
}

fun <T : Any> T.getDataFields(serializer: KSerializer<T>): Map<String, Data<*>> =
    JsonTreeParser.parse(JSON.stringify(serializer, this)).mapValues {
        when {
            try {
                it.value.primitive; false
            } catch (e: JsonElementTypeMismatchException) {
                true
            } -> it.value.toString().data
            it.value.isNull -> "null".data
            it.value.intOrNull != null -> it.value.int.data
            it.value.doubleOrNull != null -> it.value.double.data
            it.value.floatOrNull != null -> it.value.float.toDouble().data
            it.value.longOrNull != null -> it.value.long.toInt().data
            it.value.booleanOrNull != null -> it.value.boolean.data
            it.value.contentOrNull != null -> it.value.content.data
            else -> it.value.toString().data
        }
    }

//TODO normalize prices, volume (just use % increase/decrease?)

//TODO DO I want % change normaliziation or just flat from min/max?

data class MarketInfo(
    val type: invtype,
    val mine: TimeMarketInfo,
    val related: Map<invtype, TimeMarketInfo>,
    val normalized: Boolean = false
) : HasData {
    companion object;

    override fun getMLData(): Map<String, Data<*>> = mutableMapOf<String, Data<*>>().apply {
        type.putAllDataFields("type", invtype, this)
        mine.putAllData("mine", this)
        related.forEach {
            it.value.putAllData("related_${it.key.typeID}_${it.key.typeName}", this)
        }
    }.toMap()

    fun normalize() = if (normalized) this.copy() else MarketInfo(
        type,
        mine.normalize(),
        related.mapValues { it.value.normalize() },
        true
    )

}

data class TimeMarketInfo(
    val type: invtype,
    val today: MarketTypeInfo,
    val past: List<MarketTypeInfo>,
    val destroyedPast: Long,
    val normalized: Boolean = false
) : HasData {
    companion object;

    override fun getMLData(): Map<String, Data<*>> {
        return mutableMapOf<String, Data<*>>().apply {
            today.putAllData("today", this)
            past.mapIndexed { index, it ->
                it.putAllData("past_marketInfo_$index", this)
            }
            put("destroyedPast_total", destroyedPast.data)
        }
    }

    fun normalize(): TimeMarketInfo {
        return if (normalized) this.copy() else {
            val past = past.map { it.normalize() }

            TimeMarketInfo(
                type,
                today.normalize(),
                past,
                past.map { it.destroyed }.sum(),
                true
            )
        }
    }

}

data class MarketTypeInfo(
    val type: invtype, val date: String,
    val sell: PriceInfo,
    val buy: PriceInfo,
    val destroyed: Long,
    val normalized: Boolean = false
) : HasData {
    override fun getMLData(): Map<String, Data<*>> =
        mutableMapOf<String, Data<*>>().apply {
            sell.putAllData("sell", this)
            buy.putAllData("buy", this)
            put("destroyed", destroyed.data)
        }

    fun normalize(): MarketTypeInfo {
        return if (normalized) this.copy() else {
            val yesterday = getMarketInfo(type, DateTime.fromString(date).utc - 1.days).toMarketTypeInfo()

            MarketTypeInfo(
                type,
                date,
                sell.normalizeBy(yesterday.sell),
                buy.normalizeBy(yesterday.buy),
                destroyed normalizeFrom yesterday.destroyed,
                true
            )
        }
    }
}

data class PriceInfo(
    val min: Double, val max: Double, val average: Double,
    val orderVol: Long, val transactionVol: Long,
    val normalized: Boolean = false
) : HasDefaultData(
    mapOf(
        "min" to min.data,
        "max" to max.data,
        "average" to average.data,
        "orderVol" to orderVol.data,
        "transactionVol" to transactionVol.data
    )
) {
    fun normalizeBy(yesterday: PriceInfo): PriceInfo =
        if (normalized) this.copy() else PriceInfo(
            min normalizeFrom yesterday.max,
            max normalizeFrom yesterday.max,
            average normalizeFrom yesterday.average,
            orderVol normalizeFrom yesterday.orderVol,
            transactionVol normalizeFrom yesterday.transactionVol
        )

    operator fun minus(other: PriceInfo) = PriceInfo(
        min - other.min,
        max - other.max,
        average - other.average,
        orderVol - other.orderVol,
        transactionVol - other.transactionVol,
        normalized
    )

}

infix fun Double.normalizeFrom(yesterday: Double) = (this - yesterday) / yesterday
infix fun Int.normalizeFrom(yesterday: Int) = (this - yesterday) / yesterday
infix fun Long.normalizeFrom(yesterday: Long) = (this - yesterday) / yesterday

fun MarketInfo.Companion.makeFor(
    type: invtype,
    startDate: DateTime = DateTime.now(),
    daysPast: Int = 30,
    relatedDaysPast: Int = 30
): MarketInfo {
    return MarketInfo(
        type,
        TimeMarketInfo.makeFor(type, startDate, daysPast),
        type.getRelated().associate {
            it to TimeMarketInfo.makeFor(
                it,
                startDate,
                relatedDaysPast
            )
        }
    )
}


fun TimeMarketInfo.Companion.makeFor(type: invtype, startDate: DateTime, daysPast: Int = 14): TimeMarketInfo {

    val last2 = (1..daysPast + 1).map { getMarketInfo(type, startDate - it.days).toMarketTypeInfo() }

    return TimeMarketInfo(
        type,
        getMarketInfo(type, startDate).toMarketTypeInfo(),
        last2,
        last2.map { it.destroyed }.sum()
    )
}