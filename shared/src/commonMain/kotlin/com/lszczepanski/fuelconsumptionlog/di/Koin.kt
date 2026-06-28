package com.lszczepanski.fuelconsumptionlog.di

import com.lszczepanski.fuelconsumptionlog.data.local.CarRepository
import com.lszczepanski.fuelconsumptionlog.data.local.SettingsRepository
import com.lszczepanski.fuelconsumptionlog.data.local.SqlDelightCarRepository
import com.lszczepanski.fuelconsumptionlog.data.local.SqlDelightSettingsRepository
import com.lszczepanski.fuelconsumptionlog.presentation.cars.CarDetailsViewModel
import com.lszczepanski.fuelconsumptionlog.presentation.cars.CarsViewModel
import com.lszczepanski.fuelconsumptionlog.presentation.settings.SettingsViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools

private val commonModule = module {
    single<CarRepository> { SqlDelightCarRepository(get()) }
    single<SettingsRepository> { SqlDelightSettingsRepository(get()) }
    factory { CarsViewModel(get()) }
    factory { (carId: Long) -> CarDetailsViewModel(carId = carId, repository = get()) }
    factory { SettingsViewModel(get()) }
}

expect val platformModule: Module

fun initKoin(config: (KoinApplication.() -> Unit)? = null) {
    if (KoinPlatformTools.defaultContext().getOrNull() != null) return

    startKoin {
        config?.invoke(this)
        modules(commonModule, platformModule)
    }
}
