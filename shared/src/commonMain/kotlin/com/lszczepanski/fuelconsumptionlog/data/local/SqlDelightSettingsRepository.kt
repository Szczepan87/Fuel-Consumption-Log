package com.lszczepanski.fuelconsumptionlog.data.local

import com.lszczepanski.fuelconsumptionlog.db.FuelLogDatabase
import com.lszczepanski.fuelconsumptionlog.domain.model.UnitSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SqlDelightSettingsRepository(
    databaseDriverFactory: DatabaseDriverFactory,
) : SettingsRepository {

    private val database = FuelLogDatabase(databaseDriverFactory.createDriver())
    private val queries = database.fuelLogDatabaseQueries

    override suspend fun getUnitSystem(): UnitSystem {
        return withContext(Dispatchers.Default) {
            queries.insertDefaultSettings()
            val savedValue = queries.selectUnitSystem().executeAsOneOrNull()
            UnitSystem.entries.firstOrNull { it.name == savedValue } ?: UnitSystem.METRIC
        }
    }

    override suspend fun setUnitSystem(unitSystem: UnitSystem): Result<Unit> {
        return runCatching {
            withContext(Dispatchers.Default) {
                queries.insertDefaultSettings()
                queries.updateUnitSystem(unit_system = unitSystem.name)
            }
        }
    }
}
