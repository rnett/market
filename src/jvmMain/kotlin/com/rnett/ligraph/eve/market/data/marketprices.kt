package com.rnett.ligraph.eve.market.data


import com.rnett.ligraph.eve.sde.data.industryactivities.time
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable

object marketprices : LongIdTable("marketprices", "id") {
    // Database Columns

    val idCol = long("id").autoIncrement().primaryKey()
    
    val typeid = integer("typeid")
    val regionid = integer("regionid")
    val date = varchar("date", 20)
    val time = varchar("time", 30)
    
    val sellmin = double("sellmin")
    val sellmax = double("sellmax")
    val sellaverage = double("sellaverage")
    val sellordervol = long("sellordervol")
    val sellsd = double("sellsd")
    val sellpercentile = double("sellpercentile")
    
    val buymin = double("buymin")
    val buymax = double("buymax")
    val buyaverage = double("buyaverage")
    val buyordervol = long("buyordervol")
    val buysd = double("buysd")
    val buypercentile = double("buypercentile")

    init {
        uniqueIndex(
            typeid,
            regionid,
            date,
            time
        )
    }
}


class marketprice(val myId: EntityID<Long>) : LongEntity(myId) {

    companion object : LongEntityClass<marketprice>(marketprices)
    // Database Columns

    var idCol by marketprices.idCol
        private set
    var typeid by marketprices.typeid
        private set
    var regionid by marketprices.regionid
        private set
    var date by marketprices.date
        private set
    var time by marketprices.time
        private set

    var sellmin by marketprices.sellmin
        private set
    var sellmax by marketprices.sellmax
        private set
    var sellaverage by marketprices.sellaverage
        private set
    var sellordervol by marketprices.sellordervol
        private set
    var sellsd by marketprices.sellsd
        private set
    var sellpercentile by marketprices.sellpercentile
        private set

    var buymin by marketprices.buymin
        private set
    var buymax by marketprices.buymax
        private set
    var buyaverage by marketprices.buyaverage
        private set
    var buyordervol by marketprices.buyordervol
        private set
    var buysd by marketprices.buysd
        private set
    var buypercentile by marketprices.buypercentile
        private set


    // Helper Methods

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is marketinfo)
            return false
        return idCol == other.idCol
    }


    override fun hashCode() = idCol.hashCode()

}

