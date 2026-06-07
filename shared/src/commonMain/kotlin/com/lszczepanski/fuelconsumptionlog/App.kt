package com.lszczepanski.fuelconsumptionlog

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.lszczepanski.fuelconsumptionlog.data.local.SqlDelightCarRepository
import com.lszczepanski.fuelconsumptionlog.data.local.rememberDatabaseDriverFactory
import com.lszczepanski.fuelconsumptionlog.presentation.cars.CarsScreen

@Composable
@Preview
fun App() {
    val databaseDriverFactory = rememberDatabaseDriverFactory()
    val repository = remember(databaseDriverFactory) { SqlDelightCarRepository(databaseDriverFactory) }

    MaterialTheme {
        CarsScreen(repository = repository)
    }
}