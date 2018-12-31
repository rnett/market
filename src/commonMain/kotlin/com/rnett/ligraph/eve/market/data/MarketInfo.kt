package com.rnett.ligraph.eve.market.data

import com.rnett.ligraph.eve.sde.data.invtype
import com.soywiz.klock.DateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

expect fun getMarketInfo(type: invtype, date: DateTime): MarketTypeInfo

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

@Serializable
data class MarketInfo(
    val type: invtype,
    val mine: TimeMarketInfo,
    val related: Map<invtype, TimeMarketInfo>
) : HasData {
    companion object;

    override fun getData(): Map<String, Data<*>> = mutableMapOf<String, Data<*>>().apply {
        type.putAllDataFields("type", invtype, this)
        mine.putAllData("mine", this)
        related.forEach {
            it.value.putAllData("related_${it.key.typeID}_${it.key.typeName}", this)
        }
    }.toMap()
}

@Serializable
data class TimeMarketInfo(
    val type: invtype,
    val today: MarketTypeInfo,
    val past: List<MarketTypeInfo>,
    val destroyedPast: Int
) : HasData {
    companion object;

    override fun getData(): Map<String, Data<*>> {
        return mutableMapOf<String, Data<*>>().apply {
            today.putAllData("today", this)
            past.mapIndexed { index, it ->
                it.putAllData("past_marketInfo_$index", this)
            }
            put("destroyedLastTwoWeeks", destroyedPast.data)
        }
    }
}

@Serializable
data class MarketTypeInfo(
    val type: invtype, val time: DateTime,
    val sellPrice: PriceInfo,
    val sellVol: VolumeInfo,
    val buyPrice: PriceInfo,
    val buyVol: VolumeInfo,
    val destroyed: Int
) : HasData {
    override fun getData(): Map<String, Data<*>> =
        mutableMapOf<String, Data<*>>().apply {
            sellPrice.putAllData("sellPrice", this)
            sellVol.putAllData("sellVol", this)
            buyPrice.putAllData("buyPrice", this)
            buyVol.putAllData("buyVol", this)
            put("destroyed", destroyed.data)
        }
}

@Serializable
data class PriceInfo(
    val min: Double, val max: Double, val average: Double,
    val top25: Double, val top75: Double
) : HasDefaultData(
    mapOf(
        "min" to min.data,
        "max" to max.data,
        "average" to average.data,
        "top25" to top25.data,
        "top75" to top75.data
    )
)

@Serializable
data class VolumeInfo(val orderVol: Int, val transactionVol: Int) : HasDefaultData(
    mapOf(
        "orderVol" to orderVol.data,
        "transactionVol" to transactionVol.data
    )
)

