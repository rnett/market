package com.rnett.ligraph.eve.market.data


import kotlinx.serialization.*

@Serializable(with = marketinfo.Companion::class)
expect class marketinfo {
    var idCol: Long
        private set
    var typeid: Int
        private set
    var date: String
        private set
    var sellmin: Double
        private set
    var sellmax: Double
        private set
    var sellaverage: Double
        private set
    var sellordervol: Long
        private set
    var selltransactionvol: Long
        private set
    var buymin: Double
        private set
    var buymax: Double
        private set
    var buyaverage: Double
        private set
    var buyordervol: Long
        private set
    var buytransactionvol: Long
        private set
    var destroyed: Long
        private set

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int

    @Serializer(marketinfo::class)
    companion object : KSerializer<marketinfo> {
        fun getItem(id: Long): marketinfo
        fun allItems(): List<marketinfo>


        override val descriptor: SerialDescriptor

        override fun serialize(output: Encoder, obj: marketinfo)

        override fun deserialize(input: Decoder): marketinfo

        fun serializer(): KSerializer<marketinfo>
    }
}

operator fun marketinfo.Companion.get(id: Long) = getItem(id)
operator fun marketinfo.Companion.invoke() = allItems()


