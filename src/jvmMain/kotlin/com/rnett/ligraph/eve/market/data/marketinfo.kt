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
    val date = varchar("date", 20)
    val sellmin = double("sellmin")
    val sellmax = double("sellmax")
    val sellaverage = double("sellaverage")
    val sellordervol = long("sellordervol")
    val selltransactionvol = long("selltransactionvol")
    val buymin = double("buymin")
    val buymax = double("buymax")
    val buyaverage = double("buyaverage")
    val buyordervol = long("buyordervol")
    val buytransactionvol = long("buytransactionvol")
    val destroyed = long("destroyed")

    init {
        uniqueIndex(
            typeid,
            date
        )
    }
}


@Serializable(with = marketinfo.Companion::class)
actual class marketinfo(val myId: EntityID<Long>) : LongEntity(myId) {

    @Serializer(marketinfo::class)
    actual companion object : LongEntityClass<marketinfo>(marketinfos), KSerializer<marketinfo> {
        actual fun getItem(id: Long) = transaction { super.get(id) }
        actual fun allItems() = transaction { super.all().toList() }
        actual override val descriptor: SerialDescriptor = object : SerialClassDescImpl("marketinfo") {
            init {
                addElement("idCol")
                addElement("typeid")
                addElement("date")
                addElement("sellmin")
                addElement("sellmax")
                addElement("sellaverage")
                addElement("sellordervol")
                addElement("selltransactionvol")
                addElement("buymin")
                addElement("buymax")
                addElement("buyaverage")
                addElement("buyordervol")
                addElement("buytransactionvol")
                addElement("destroyed")
            }
        }

        actual override fun serialize(output: Encoder, obj: marketinfo) {
            val compositeOutput: CompositeEncoder = output.beginStructure(descriptor)
            compositeOutput.encodeStringElement(
                descriptor,
                0,
                HexConverter.printHexBinary(obj.idCol.toString().toUtf8Bytes())
            )
            compositeOutput.encodeStringElement(
                descriptor,
                1,
                HexConverter.printHexBinary(obj.typeid.toString().toUtf8Bytes())
            )
            compositeOutput.encodeStringElement(
                descriptor,
                2,
                HexConverter.printHexBinary(obj.date.toString().toUtf8Bytes())
            )
            compositeOutput.encodeStringElement(
                descriptor,
                3,
                HexConverter.printHexBinary(obj.sellmin.toString().toUtf8Bytes())
            )
            compositeOutput.encodeStringElement(
                descriptor,
                4,
                HexConverter.printHexBinary(obj.sellmax.toString().toUtf8Bytes())
            )
            compositeOutput.encodeStringElement(
                descriptor,
                5,
                HexConverter.printHexBinary(obj.sellaverage.toString().toUtf8Bytes())
            )
            compositeOutput.encodeStringElement(
                descriptor,
                6,
                HexConverter.printHexBinary(obj.sellordervol.toString().toUtf8Bytes())
            )
            compositeOutput.encodeStringElement(
                descriptor,
                7,
                HexConverter.printHexBinary(obj.selltransactionvol.toString().toUtf8Bytes())
            )
            compositeOutput.encodeStringElement(
                descriptor,
                8,
                HexConverter.printHexBinary(obj.buymin.toString().toUtf8Bytes())
            )
            compositeOutput.encodeStringElement(
                descriptor,
                9,
                HexConverter.printHexBinary(obj.buymax.toString().toUtf8Bytes())
            )
            compositeOutput.encodeStringElement(
                descriptor,
                10,
                HexConverter.printHexBinary(obj.buyaverage.toString().toUtf8Bytes())
            )
            compositeOutput.encodeStringElement(
                descriptor,
                11,
                HexConverter.printHexBinary(obj.buyordervol.toString().toUtf8Bytes())
            )
            compositeOutput.encodeStringElement(
                descriptor,
                12,
                HexConverter.printHexBinary(obj.buytransactionvol.toString().toUtf8Bytes())
            )
            compositeOutput.encodeStringElement(
                descriptor,
                13,
                HexConverter.printHexBinary(obj.destroyed.toString().toUtf8Bytes())
            )
            compositeOutput.endStructure(descriptor)
        }

        actual override fun deserialize(input: Decoder): marketinfo {
            val inp: CompositeDecoder = input.beginStructure(descriptor)
            var id: Long? = null
            loop@ while (true) {
                when (val i = inp.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> id = stringFromUtf8Bytes(
                        HexConverter.parseHexBinary(
                            inp.decodeStringElement(
                                descriptor,
                                i
                            )
                        )
                    ).toLong()
                    else -> if (i < descriptor.elementsCount) continue@loop else throw SerializationException("Unknown index $i")
                }
            }

            inp.endStructure(descriptor)
            if (id == null)
                throw SerializationException("Id 'idCol' @ index 0 not found")
            else
                return marketinfo[id]
        }

        actual fun serializer(): KSerializer<marketinfo> = this
        fun new(
            idCol: Long,
            typeid: Int,
            date: String,
            sellmin: Double,
            sellmax: Double,
            sellaverage: Double,
            sellordervol: Long,
            selltransactionvol: Long,
            buymin: Double,
            buymax: Double,
            buyaverage: Double,
            buyordervol: Long,
            buytransactionvol: Long,
            destroyed: Long
        ) = new {
            this.idCol = idCol
            this.typeid = typeid
            this.date = date
            this.sellmin = sellmin
            this.sellmax = sellmax
            this.sellaverage = sellaverage
            this.sellordervol = sellordervol
            this.selltransactionvol = selltransactionvol
            this.buymin = buymin
            this.buymax = buymax
            this.buyaverage = buyaverage
            this.buyordervol = buyordervol
            this.buytransactionvol = buytransactionvol
            this.destroyed = destroyed
        }
    }

    // Database Columns

    actual var idCol by marketinfos.idCol
        private set

    actual var typeid by marketinfos.typeid
        private set

    actual var date by marketinfos.date
        private set

    actual var sellmin by marketinfos.sellmin
        private set

    actual var sellmax by marketinfos.sellmax
        private set

    actual var sellaverage by marketinfos.sellaverage
        private set

    actual var sellordervol by marketinfos.sellordervol
        private set

    actual var selltransactionvol by marketinfos.selltransactionvol
        private set

    actual var buymin by marketinfos.buymin
        private set

    actual var buymax by marketinfos.buymax
        private set

    actual var buyaverage by marketinfos.buyaverage
        private set

    actual var buyordervol by marketinfos.buyordervol
        private set

    actual var buytransactionvol by marketinfos.buytransactionvol
        private set

    actual var destroyed by marketinfos.destroyed
        private set


    // Helper Methods

    actual override fun equals(other: Any?): Boolean {
        if (other == null || other !is marketinfo)
            return false
        return idCol == other.idCol
    }


    actual override fun hashCode() = idCol.hashCode()

}

