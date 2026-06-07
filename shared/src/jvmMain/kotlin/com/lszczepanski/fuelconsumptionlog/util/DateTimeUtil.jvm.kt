package com.lszczepanski.fuelconsumptionlog.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun formatEpochMillis(epochMillis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val instant = Instant.ofEpochMilli(epochMillis)
    return formatter.format(instant.atZone(ZoneId.systemDefault()))
}

