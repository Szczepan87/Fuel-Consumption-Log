package com.lszczepanski.fuelconsumptionlog.data.local

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.lszczepanski.fuelconsumptionlog.db.FuelLogDatabase

actual class DatabaseDriverFactory(
    private val context: Context,
) {
    actual fun createDriver(): SqlDriver = AndroidSqliteDriver(
        schema = FuelLogDatabase.Schema,
        context = context,
        name = "fuel_log.db",
    )
}

@Composable
actual fun rememberDatabaseDriverFactory(): DatabaseDriverFactory {
    val context = LocalContext.current.applicationContext
    return remember(context) { DatabaseDriverFactory(context) }
}


