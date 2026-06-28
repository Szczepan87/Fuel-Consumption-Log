package com.lszczepanski.fuelconsumptionlog.data.local

import com.lszczepanski.fuelconsumptionlog.domain.model.UnitSystem

interface SettingsRepository {
    suspend fun getUnitSystem(): UnitSystem
    suspend fun setUnitSystem(unitSystem: UnitSystem): Result<Unit>
}
