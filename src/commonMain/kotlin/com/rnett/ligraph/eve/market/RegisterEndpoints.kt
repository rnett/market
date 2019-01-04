package com.rnett.ligraph.eve.market


fun registerEndpoints() {
    com.rnett.ligraph.eve.market.data.registerEndpoints()
    com.rnett.ligraph.eve.sde.data.registerEndpoints()
}

