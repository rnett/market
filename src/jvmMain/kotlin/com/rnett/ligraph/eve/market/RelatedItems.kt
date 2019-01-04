package com.rnett.ligraph.eve.market

import com.rnett.ligraph.eve.sde.data.industryactivitymaterials
import com.rnett.ligraph.eve.sde.data.industryactivityproducts
import com.rnett.ligraph.eve.sde.data.invtype
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object relateditems : Table("relateditems") {
    val typeid = integer("typeid")
    val relatedtypeid = integer("relatedtypeid")

    init {
        uniqueIndex(typeid, relatedtypeid)
    }

    fun addRelation(from: Int, to: Int) {
        transaction {
            insert {
                it[typeid] = from
                it[relatedtypeid] = to
            }
        }
    }
}

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
    val related = mutableSetOf<Int>(typeid)

    for (i in 0..steps) {
        related.addAll(related.industryRelatedFirst())
    }

    return related
}

//TODO make expected, make makeFor on client (rather than caching?)

//TODO store related in DB?

private fun invtype.generateRelated(steps: Int = 2): List<Int> = transaction {
    listOf(
        industryRelated(typeID, steps),
        group.invtypes_rk.map { it.typeID },
        marketGroup?.invtypes_rk?.map { it.typeID } ?: emptyList()
    ).flatten().toSet().filter { it != this@generateRelated.typeID }
}

/*
TODO
    same category?
    used with commonly on zkill

    TODO setup zkill used with tracker (just set up fit tracker too?)



Want to keep reasonably small if possible

 */

suspend fun addAllRelated(limit: Int? = null, steps: Int = 2) = coroutineScope {
    val known = transaction { relateditems.selectAll().map { it[relateditems.typeid] }.toSet() }

    transaction { invtype.all().toList().filter { it.typeID !in known } }
        .let {

            if (it.isEmpty()) {
                println("No more items to add!")
                throw RuntimeException("Done with adding items")
            }

            if (limit != null)
                it.take(limit)
            else
                it
        }
        .map {
            launch(Dispatchers.IO) {
                it.generateRelated(steps).map { related ->
                    launch(Dispatchers.IO) {
                        transaction {
                            if (invtype.findById(related) != null)
                                relateditems.addRelation(it.typeID, related)
                        }
                    }
                }.joinAll()
            }
        }.joinAll()
}

fun main(args: Array<String>) {
    connectToDB(args.getOrNull(1))
    runBlocking {
        addAllRelated(limit = args.getOrNull(0)?.toIntOrNull())
    }

}

fun invtype.getRelated(): List<invtype> = transaction {
    relateditems.select { relateditems.typeid eq typeID }
        .map { invtype[it[relateditems.relatedtypeid]] }
}