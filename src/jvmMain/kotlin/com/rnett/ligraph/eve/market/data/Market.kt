package com.rnett.ligraph.eve.market.data

import com.rnett.ligraph.eve.sde.data.industryactivitymaterials
import com.rnett.ligraph.eve.sde.data.industryactivityproducts
import com.rnett.ligraph.eve.sde.data.invtype
import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

actual fun getMarketInfo(type: invtype, date: DateTime): MarketTypeInfo = throw NotImplementedError()

fun madeFrom(typeid: Int) = transaction {
    industryactivitymaterials.select {
        // made from
        industryactivitymaterials.activityID inList industryactivityproducts.select { industryactivityproducts.productTypeID eq typeid }.map {
            it[industryactivityproducts.activityID]
        }.distinct()
    }.map { it[industryactivitymaterials.materialTypeID] } +
            industryactivityproducts.select { industryactivityproducts.productTypeID eq typeid }.map {
                // made from bps
                it[industryactivityproducts.activityID]
            }
}.toSet()

fun madeFrom(typeids: List<Int>) = transaction {
    industryactivitymaterials.select {
        // made from
        industryactivitymaterials.activityID inList industryactivityproducts.select { industryactivityproducts.productTypeID inList typeids }.map {
            it[industryactivityproducts.activityID]
        }.distinct()
    }.map { it[industryactivitymaterials.materialTypeID] } +
            industryactivityproducts.select { industryactivityproducts.productTypeID inList typeids }.map {
                // made from bps
                it[industryactivityproducts.activityID]
            }
}.toSet()

fun madeInto(typeid: Int) = transaction {
    industryactivityproducts.select {
        // made into
        industryactivityproducts.activityID inList industryactivitymaterials.select { industryactivitymaterials.materialTypeID eq typeid }.map {
            it[industryactivitymaterials.activityID]
        }.distinct()
    }.map { it[industryactivityproducts.productTypeID] } +
            industryactivitymaterials.select { industryactivitymaterials.materialTypeID eq typeid }.map {
                // made into bps
                it[industryactivitymaterials.activityID]
            }
}.toSet()

fun madeInto(typeids: List<Int>) = transaction {
    industryactivityproducts.select {
        // made into
        industryactivityproducts.activityID inList industryactivitymaterials.select { industryactivitymaterials.materialTypeID inList typeids }.map {
            it[industryactivitymaterials.activityID]
        }.distinct()
    }.map { it[industryactivityproducts.productTypeID] } +
            industryactivitymaterials.select { industryactivitymaterials.materialTypeID inList typeids }.map {
                // made into bps
                it[industryactivitymaterials.activityID]
            }
}.toSet()

fun industryRelatedFirst(typeid: Int) = (madeFrom(typeid) + madeInto(typeid)).toSet()

fun Iterable<Int>.industryRelatedFirst() = (madeInto(this.toList()) + madeFrom(this.toList())).toSet()

fun industryRelated(typeid: Int, steps: Int = 2): Set<Int> {
    var related = setOf(typeid)

    for (i in 0..steps) {
        related += related.industryRelatedFirst()
    }

    return related
}

//TODO make expected, make makeFor on client (rather than caching?)

//TODO store related in DB?

fun invtype.getRelated(steps: Int = 2): List<invtype> = transaction {
    listOf(
        industryRelated(typeID, steps),
        group.invtypes_rk.map { it.typeID },
        marketGroup.invtypes_rk.map { it.typeID }
    ).flatten().toSet().filter { it != this@getRelated.typeID }.map { invtype[it] }
}
/*
TODO
    same category?
    used with commonly on zkill

    TODO setup zkill used with tracker (just set up fit tracker too?)



Want to keep reasonably small if possible

 */

fun MarketInfo.Companion.makeFor(
    type: invtype,
    startDate: DateTime = DateTime.now(),
    daysPast: Int = 14,
    relatedDaysPast: Int = 14
): MarketInfo {
    return MarketInfo(
        type,
        TimeMarketInfo.makeFor(type, startDate, daysPast),
        type.getRelated().associate { it to TimeMarketInfo.makeFor(it, startDate, relatedDaysPast) }
    )
}

fun TimeMarketInfo.Companion.makeFor(type: invtype, startDate: DateTime, daysPast: Int = 14): TimeMarketInfo {

    val last2 = (1..daysPast + 1).map { getMarketInfo(type, startDate - it.days) }

    return TimeMarketInfo(
        type,
        getMarketInfo(type, startDate),
        last2,
        last2.sumBy { it.destroyed }
    )
}