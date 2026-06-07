package com.lszczepanski.fuelconsumptionlog

import androidx.compose.ui.window.ComposeUIViewController
import com.lszczepanski.fuelconsumptionlog.di.initKoin

fun MainViewController() = ComposeUIViewController {
	initKoin()
	App()
}
