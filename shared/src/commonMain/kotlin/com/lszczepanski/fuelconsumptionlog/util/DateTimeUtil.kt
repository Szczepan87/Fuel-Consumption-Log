package com.lszczepanski.fuelconsumptionlog.util

expect fun currentTimeMillis(): Long
expect fun formatEpochMillis(epochMillis: Long): String
expect fun formatMonthYearEpochMillis(epochMillis: Long): String

fun formatMonthYearHeader(epochMillis: Long): String {
    return formatMonthYearEpochMillis(epochMillis).replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase() else char.toString()
    }
}
