package com.lszczepanski.fuelconsumptionlog.data.local

import androidx.compose.runtime.Composable
import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

@Composable
expect fun rememberDatabaseDriverFactory(): DatabaseDriverFactory

