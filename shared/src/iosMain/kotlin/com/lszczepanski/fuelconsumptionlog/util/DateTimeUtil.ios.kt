package com.lszczepanski.fuelconsumptionlog.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.posix.time

private const val APPLE_REFERENCE_DATE_OFFSET_SECONDS = 978_307_200.0

@OptIn(ExperimentalForeignApi::class)
actual fun currentTimeMillis(): Long {
    return time(null) * 1000L
}

actual fun formatEpochMillis(epochMillis: Long): String {
    val formatter = NSDateFormatter().apply {
        dateFormat = "yyyy-MM-dd HH:mm"
    }
    val date = NSDate(
        timeIntervalSinceReferenceDate = (epochMillis.toDouble() / 1000.0) - APPLE_REFERENCE_DATE_OFFSET_SECONDS
    )
    return formatter.stringFromDate(date)
}

actual fun formatMonthYearEpochMillis(epochMillis: Long): String {
    val formatter = NSDateFormatter().apply {
        dateFormat = "LLLL yyyy"
    }
    val date = NSDate(
        timeIntervalSinceReferenceDate = (epochMillis.toDouble() / 1000.0) - APPLE_REFERENCE_DATE_OFFSET_SECONDS
    )
    return formatter.stringFromDate(date)
}
