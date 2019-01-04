package com.rnett.ligraph.eve.market

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

interface HasData {
    fun getMLData(): Map<String, Data<*>>
}

fun HasData.putAllData(prefix: String, map: MutableMap<String, Data<*>>) {
    this.getMLData().forEach {
        map.put("${prefix}_${it.key}", it.value)
    }
}

fun <T : Any> T.putAllDataFields(prefix: String, serializer: KSerializer<T>, map: MutableMap<String, Data<*>>) {
    this.getDataFields(serializer).forEach {
        map.put("${prefix}_${it.key}", it.value)
    }
}

abstract class HasDefaultData(val data: Map<String, Data<*>>) :
    HasData {
    override fun getMLData(): Map<String, Data<*>> = data
}

@Serializable
sealed class Data<T>(val data: T)

@Serializable
data class StringData(val stringData: String) : Data<String>(stringData)

@Serializable
data class IntData(val intData: Int) : Data<Int>(intData)

@Serializable
data class DoubleData(val doubleData: Double) : Data<Double>(doubleData)

@Serializable
data class BooleanData(val booleanData: Boolean) : Data<Boolean>(booleanData)

@Serializable
data class LongData(val longData: Long) : Data<Long>(longData)

val String.data get() = StringData(this)
val Int.data get() = IntData(this)
val Double.data get() = DoubleData(this)
val Boolean.data get() = BooleanData(this)
val Long.data get() = LongData(this)
