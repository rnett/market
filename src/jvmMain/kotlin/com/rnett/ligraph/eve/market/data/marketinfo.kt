package com.rnett.ligraph.eve.market.data


import kotlinx.serialization.*
import kotlinx.serialization.internal.HexConverter
import kotlinx.serialization.internal.SerialClassDescImpl
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.transactions.transaction

object marketinfos : LongIdTable("marketinfo", "id") {
    // Database Columns

    val idCol = long("id").autoIncrement().primaryKey()

    val typeid = integer("typeid")
    val regionid = integer("regionid")
    val date = varchar("date", 20)

    val sellmin = double("sellmin")
    val sellmax = double("sellmax")
    val sellaverage = double("sellaverage")
    val sellordervol = long("sellordervol")
    val selltransactionvol = long("selltransactionvol")
    val sellsd = double("sellsd")
    val sellpercentile = double("sellpercentile")

    val buymin = double("buymin")
    val buymax = double("buymax")
    val buyaverage = double("buyaverage")
    val buyordervol = long("buyordervol")
    val buytransactionvol = long("buytransactionvol")
    val buysd = double("buysd")
    val buypercentile = double("buypercentile")

    val destroyed = long("destroyed")

    init {
        uniqueIndex(
            typeid,
            regionid,
            date
        )
    }
}


class marketinfo(val myId: EntityID<Long>) : LongEntity(myId) {

    companion object : LongEntityClass<marketinfo>(marketinfos){
        fun new(
            typeid: Int,
            regionid: Int,
            date: String,
            
            sellmin: Double,
            sellmax: Double,
            sellaverage: Double,
            sellordervol: Long,
            selltransactionvol: Long,
            sellsd: Double,
            sellpercentile: Double,
            
            buymin: Double,
            buymax: Double,
            buyaverage: Double,
            buyordervol: Long,
            buytransactionvol: Long,
            buysd: Double,
            buypercentile: Double,
            
            destroyed: Long
        ) = new {

            this.typeid = typeid
            this.regionid = regionid
            this.date = date
            
            this.sellmin = sellmin
            this.sellmax = sellmax
            this.sellaverage = sellaverage
            this.sellordervol = sellordervol
            this.selltransactionvol = selltransactionvol
            this.sellsd = sellsd
            this.sellpercentile = sellpercentile
            
            this.buymin = buymin
            this.buymax = buymax
            this.buyaverage = buyaverage
            this.buyordervol = buyordervol
            this.buytransactionvol = buytransactionvol
            this.buysd = buysd
            this.buypercentile = buypercentile
            
            this.destroyed = destroyed
        }
    }
    // Database Columns

    var idCol by marketinfos.idCol
        private set

    var typeid by marketinfos.typeid
        private set
    var regionid by marketinfos.regionid
        private set
    var date by marketinfos.date
        private set

    var sellmin by marketinfos.sellmin
        private set
    var sellmax by marketinfos.sellmax
        private set
    var sellaverage by marketinfos.sellaverage
        private set
    var sellordervol by marketinfos.sellordervol
        private set
    var selltransactionvol by marketinfos.selltransactionvol
        private set
    var sellsd by marketprices.sellsd
        private set
    var sellpercentile by marketprices.sellpercentile
        private set

    var buymin by marketinfos.buymin
        private set
    var buymax by marketinfos.buymax
        private set
    var buyaverage by marketinfos.buyaverage
        private set
    var buyordervol by marketinfos.buyordervol
        private set
    var buytransactionvol by marketinfos.buytransactionvol
        private set
    var buysd by marketprices.buysd
        private set
    var buypercentile by marketprices.buypercentile
        private set

    var destroyed by marketinfos.destroyed
        private set


    // Helper Methods

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is marketinfo)
            return false
        return idCol == other.idCol
    }


    override fun hashCode() = idCol.hashCode()

}

