package com.rnett.ligraph.eve.market

import com.rnett.ligraph.eve.sde.data.invtype
import com.rnett.ligraph.eve.sde.data.invtypes
import javafx.application.Application.launch
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object relateditems : Table("relateditems") {
    val typeid = integer("typeid")
    val relatedtypeid = integer("relatedtypeid")

    init {
        uniqueIndex(
            typeid,
            relatedtypeid
        )
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

object industryrelated : Table("industryrelated") {
    val typeid = integer("typeid")
    val relatedtypeid = integer("relatedtypeid")

    init {
        uniqueIndex(
            typeid,
            relatedtypeid
        )
        index(false, typeid)
    }
}

fun industryRelated(item: Int) = transaction {
    val first = industryrelated.select { industryrelated.typeid eq item }.map {
        it[industryrelated.relatedtypeid]
    }.toSet()

    return@transaction if (first.size > 20)
        first
    else
        first + industryrelated.select { industryrelated.typeid inList first }.map {
            it[industryrelated.relatedtypeid]
        }.toSet()
}


//TODO make expected, make makeFor on client (rather than caching?)

//TODO store interfaces in DB?

private fun invtype.generateRelated(steps: Int = 1): List<Int> = transaction {
    listOf(
        industryRelated(typeID),
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

suspend fun addAllRelated(limit: Int? = null, steps: Int = 1, force: Boolean = false) = coroutineScope {
    //val known = transaction { relateditems.selectAll().map { it[relateditems.typeid] }.toSet() }

    val types =
        publishedItems/*.filter { it.typeID !in known }*/
            .let {
                /*
                if (it.isEmpty()) {
                    println("No more items to add!")
                    throw RuntimeException("Done with adding items")
                }
                */
                if (limit != null)
                    it.take(limit)
                else
                    it
            }

    val sizes = relateditems.run {
        transaction {
            slice(typeid, relatedtypeid.count())
                .select{ typeid inList types.map { it.typeID } }
                .groupBy(typeid).associate {
                    it[typeid] to it[relatedtypeid.count()]
                }
        }
    }

    val numTypes = types.size

    println("Types to do: $numTypes")

    var typesLeft = numTypes

    types.map { type ->
        launch(Dispatchers.Default) {
            val toAdd = type.generateRelated(steps).filter { it in publishedItemIds }

            if(toAdd.size > sizes[type.typeID] ?: 0 || force) {
                transaction {
                    relateditems.deleteWhere { relateditems.typeid eq type.typeID }

                    commit()

                    relateditems.batchInsert(toAdd, true) {
                        this[relateditems.typeid] = type.typeID
                        this[relateditems.relatedtypeid] = it
                    }
                }
                typesLeft--
                println("Inserted.  Left: $typesLeft (${100 * typesLeft / numTypes}%)")
            } else {
                typesLeft--
                println("Skipped.  Left: $typesLeft (${100 * typesLeft / numTypes}%)")
            }

        }
    }.joinAll()
}

fun invtype.getRelated(): List<invtype> = transaction {
    relateditems.select { relateditems.typeid eq typeID }
        .map { invtype[it[relateditems.relatedtypeid]] }
}