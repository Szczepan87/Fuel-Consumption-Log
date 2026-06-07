package com.lszczepanski.fuelconsumptionlog

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.lszczepanski.fuelconsumptionlog.data.local.SqlDelightCarRepository
import com.lszczepanski.fuelconsumptionlog.data.local.rememberDatabaseDriverFactory
import com.lszczepanski.fuelconsumptionlog.presentation.cars.CarDetailsScreen
import com.lszczepanski.fuelconsumptionlog.presentation.cars.CarsScreen

@Composable
@Preview
fun App() {
    val databaseDriverFactory = rememberDatabaseDriverFactory()
    val repository = remember(databaseDriverFactory) { SqlDelightCarRepository(databaseDriverFactory) }
    var selectedCarId by remember { mutableStateOf<Long?>(null) }

    MaterialTheme {
        if (selectedCarId == null) {
            CarsScreen(
                repository = repository,
                onCarSelected = { selectedCarId = it },
            )
        } else {
            CarDetailsScreen(
                carId = selectedCarId!!,
                repository = repository,
                onBack = { selectedCarId = null },
            )
        }
    }
}