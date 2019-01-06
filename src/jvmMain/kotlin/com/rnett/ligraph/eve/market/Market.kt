package com.rnett.ligraph.eve.market

import com.rnett.ligraph.eve.sde.data.invtype
import com.rnett.ligraph.eve.sde.data.invtypes
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

//TODO implement
//fun getMarketInfo(type: invtype, date: DateTime): MarketTypeInfo = throw NotImplementedError()


val publishedItems by lazy{
    transaction { invtype.find { invtypes.published eq true }.toList() }
}

val publishedItemIds by lazy{
    transaction {
        publishedItems.map { it.typeID }.toSet()
    }
}
