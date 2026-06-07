package com.lszczepanski.fuelconsumptionlog.util

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

actual fun currentTimeMillis(): Long {
    return (NSDate().timeIntervalSince1970 * 1000.0).toLong()
}

actual fun formatEpochMillis(epochMillis: Long): String {
    val formatter = NSDateFormatter().apply {
        dateFormat = "yyyy-MM-dd HH:mm"
    }
    val date = NSDate.dateWithTimeIntervalSince1970(epochMillis.toDouble() / 1000.0)
    return formatter.stringFromDate(date)
}

