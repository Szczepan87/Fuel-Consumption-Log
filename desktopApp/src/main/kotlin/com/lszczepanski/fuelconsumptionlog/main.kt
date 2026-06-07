package com.lszczepanski.fuelconsumptionlog

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.lszczepanski.fuelconsumptionlog.di.initKoin

fun main() = application {
    initKoin()

    Window(
        onCloseRequest = ::exitApplication,
        title = "FuelConsumptionLog",
    ) {
        App()
    }
}