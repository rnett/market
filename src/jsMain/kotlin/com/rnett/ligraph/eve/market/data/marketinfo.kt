package com.rnett.ligraph.eve.market.data


import com.rnett.kframe.data.callEndpoint
import com.rnett.ligraph.eve.market.requestClient
import kotlinx.serialization.*
import kotlinx.serialization.internal.HexConverter
import kotlinx.serialization.internal.SerialClassDescImpl

@Serializable(with = marketinfo.Companion::class)
actual data class marketinfo(
    actual var idCol: Long,
    actual var typeid: Int,
    actual var date: String,
    actual var sellmin: Double,
    actual var sellmax: Double,
    actual var sellaverage: Double,
    actual var sellordervol: Long,
    actual var selltransactionvol: Long,
    actual var buymin: Double,
    actual var buymax: Double,
    actual var buyaverage: Double,
    actual var buyordervol: Long,
    actual var buytransactionvol: Long,
    actual var destroyed: Long
) {


    actual override fun equals(other: Any?): Boolean {
        if (other == null || other !is marketinfo)
            return false
        return idCol == other.idCol
    }


    actual override fun hashCode() = idCol.hashCode()


    @Serializer(marketinfo::class)
    actual companion object : KSerializer<marketinfo> {
        actual fun getItem(id: Long): marketinfo = callEndpoint(this::getItem, requestClient, id)
        actual fun allItems(): List<marketinfo> = callEndpoint(this::allItems, requestClient)


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
            var temp_idCol: Long? = null
            var temp_typeid: Int? = null
            var temp_date: String? = null
            var temp_sellmin: Double? = null
            var temp_sellmax: Double? = null
            var temp_sellaverage: Double? = null
            var temp_sellordervol: Long? = null
            var temp_selltransactionvol: Long? = null
            var temp_buymin: Double? = null
            var temp_buymax: Double? = null
            var temp_buyaverage: Double? = null
            var temp_buyordervol: Long? = null
            var temp_buytransactionvol: Long? = null
            var temp_destroyed: Long? = null
            loop@ while (true) {
                when (val i = inp.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> temp_idCol = stringFromUtf8Bytes(
                        HexConverter.parseHexBinary(
                            inp.decodeStringElement(
                                descriptor,
                                i
                            )
                        )
                    ).toLong()
                    1 -> temp_typeid =
                        stringFromUtf8Bytes(HexConverter.parseHexBinary(inp.decodeStringElement(descriptor, i))).toInt()
                    2 -> temp_date = stringFromUtf8Bytes(
                        HexConverter.parseHexBinary(
                            inp.decodeStringElement(
                                descriptor,
                                i
                            )
                        )
                    ).toString()
                    3 -> temp_sellmin = stringFromUtf8Bytes(
                        HexConverter.parseHexBinary(
                            inp.decodeStringElement(
                                descriptor,
                                i
                            )
                        )
                    ).toDouble()
                    4 -> temp_sellmax = stringFromUtf8Bytes(
                        HexConverter.parseHexBinary(
                            inp.decodeStringElement(
                                descriptor,
                                i
                            )
                        )
                    ).toDouble()
                    5 -> temp_sellaverage = stringFromUtf8Bytes(
                        HexConverter.parseHexBinary(
                            inp.decodeStringElement(
                                descriptor,
                                i
                            )
                        )
                    ).toDouble()
                    6 -> temp_sellordervol = stringFromUtf8Bytes(
                        HexConverter.parseHexBinary(
                            inp.decodeStringElement(
                                descriptor,
                                i
                            )
                        )
                    ).toLong()
                    7 -> temp_selltransactionvol = stringFromUtf8Bytes(
                        HexConverter.parseHexBinary(
                            inp.decodeStringElement(
                                descriptor,
                                i
                            )
                        )
                    ).toLong()
                    8 -> temp_buymin = stringFromUtf8Bytes(
                        HexConverter.parseHexBinary(
                            inp.decodeStringElement(
                                descriptor,
                                i
                            )
                        )
                    ).toDouble()
                    9 -> temp_buymax = stringFromUtf8Bytes(
                        HexConverter.parseHexBinary(
                            inp.decodeStringElement(
                                descriptor,
                                i
                            )
                        )
                    ).toDouble()
                    10 -> temp_buyaverage = stringFromUtf8Bytes(
                        HexConverter.parseHexBinary(
                            inp.decodeStringElement(
                                descriptor,
                                i
                            )
                        )
                    ).toDouble()
                    11 -> temp_buyordervol = stringFromUtf8Bytes(
                        HexConverter.parseHexBinary(
                            inp.decodeStringElement(
                                descriptor,
                                i
                            )
                        )
                    ).toLong()
                    12 -> temp_buytransactionvol = stringFromUtf8Bytes(
                        HexConverter.parseHexBinary(
                            inp.decodeStringElement(
                                descriptor,
                                i
                            )
                        )
                    ).toLong()
                    13 -> temp_destroyed = stringFromUtf8Bytes(
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

            return marketinfo(
                temp_idCol ?: throw SerializationException("Missing value for idCol"),
                temp_typeid ?: throw SerializationException("Missing value for typeid"),
                temp_date ?: throw SerializationException("Missing value for date"),
                temp_sellmin ?: throw SerializationException("Missing value for sellmin"),
                temp_sellmax ?: throw SerializationException("Missing value for sellmax"),
                temp_sellaverage ?: throw SerializationException("Missing value for sellaverage"),
                temp_sellordervol ?: throw SerializationException("Missing value for sellordervol"),
                temp_selltransactionvol ?: throw SerializationException("Missing value for selltransactionvol"),
                temp_buymin ?: throw SerializationException("Missing value for buymin"),
                temp_buymax ?: throw SerializationException("Missing value for buymax"),
                temp_buyaverage ?: throw SerializationException("Missing value for buyaverage"),
                temp_buyordervol ?: throw SerializationException("Missing value for buyordervol"),
                temp_buytransactionvol ?: throw SerializationException("Missing value for buytransactionvol"),
                temp_destroyed ?: throw SerializationException("Missing value for destroyed")
            )
        }

        actual fun serializer(): KSerializer<marketinfo> = this
    }
}

