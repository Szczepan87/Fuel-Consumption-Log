package com.lszczepanski.fuelconsumptionlog.data.local

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.lszczepanski.fuelconsumptionlog.db.FuelLogDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver = NativeSqliteDriver(
        schema = FuelLogDatabase.Schema,
        name = "fuel_log.db",
    )
}

@Composable
actual fun rememberDatabaseDriverFactory(): DatabaseDriverFactory {
    return remember { DatabaseDriverFactory() }
}


