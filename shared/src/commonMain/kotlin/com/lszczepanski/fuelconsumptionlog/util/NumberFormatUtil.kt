package com.lszczepanski.fuelconsumptionlog.util

import kotlin.math.pow
import kotlin.math.round

fun formatDecimal(value: Double, maxFractionDigits: Int = 2): String {
    val factor = 10.0.pow(maxFractionDigits)
    val rounded = round(value * factor) / factor
    val text = rounded.toString()
    return if (text.contains('.')) text.trimEnd('0').trimEnd('.') else text
}
