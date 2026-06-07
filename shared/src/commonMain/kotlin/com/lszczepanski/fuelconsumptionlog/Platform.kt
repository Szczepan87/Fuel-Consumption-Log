package com.lszczepanski.fuelconsumptionlog

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform