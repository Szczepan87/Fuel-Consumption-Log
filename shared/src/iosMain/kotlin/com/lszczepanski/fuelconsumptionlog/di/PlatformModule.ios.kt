package com.lszczepanski.fuelconsumptionlog.di

import com.lszczepanski.fuelconsumptionlog.data.local.DatabaseDriverFactory
import org.koin.dsl.module

actual val platformModule = module {
    single { DatabaseDriverFactory() }
}

