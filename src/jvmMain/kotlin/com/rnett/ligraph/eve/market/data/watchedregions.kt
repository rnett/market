package com.rnett.ligraph.eve.market.data

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object WatchedRegions : IntIdTable("watchedregions", "regionid") {
    val regionid = integer("regionid").primaryKey()

    fun watchRegion(regionId: Int){
        transaction{
            if(WatchedRegions.select { regionid eq regionId }.count() == 0)
                WatchedRegions.insert{
                    it[regionid] = regionId
                }
        }
    }

    fun unWatchRegion(regionId: Int){
        transaction {
            WatchedRegions.deleteWhere { regionid eq regionId }
        }
    }

    val watchedRegions get() = transaction { selectAll().map { it[regionid] } }
}