package com.lszczepanski.fuelconsumptionlog

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.lszczepanski.fuelconsumptionlog.di.initKoin
import com.lszczepanski.fuelconsumptionlog.presentation.cars.CarDetailsScreen
import com.lszczepanski.fuelconsumptionlog.presentation.cars.CarDetailsViewModel
import com.lszczepanski.fuelconsumptionlog.presentation.cars.CarsScreen
import com.lszczepanski.fuelconsumptionlog.presentation.cars.CarsViewModel
import com.lszczepanski.fuelconsumptionlog.presentation.settings.SettingsScreen
import com.lszczepanski.fuelconsumptionlog.presentation.settings.SettingsViewModel
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform

@Composable
@Preview
fun App() {
    initKoin()

    val koin = remember { KoinPlatform.getKoin() }
    val carsViewModel = remember { koin.get<CarsViewModel>() }
    val settingsViewModel = remember { koin.get<SettingsViewModel>() }
    val settingsUiState by settingsViewModel.uiState.collectAsState()
    var selectedCarId by remember { mutableStateOf<Long?>(null) }
    var isSettingsOpen by remember { mutableStateOf(false) }

    MaterialTheme {
        if (isSettingsOpen) {
            SettingsScreen(
                unitSystem = settingsUiState.unitSystem,
                errorMessage = settingsUiState.errorMessage,
                onUnitSystemSelected = settingsViewModel::onUnitSystemSelected,
                onBack = { isSettingsOpen = false },
            )
        } else if (selectedCarId == null) {
            CarsScreen(
                viewModel = carsViewModel,
                onCarSelected = { selectedCarId = it },
                onOpenSettings = { isSettingsOpen = true },
            )
        } else {
            val detailsViewModel = remember(selectedCarId) {
                koin.get<CarDetailsViewModel> { parametersOf(selectedCarId!!) }
            }

            CarDetailsScreen(
                viewModel = detailsViewModel,
                onBack = { selectedCarId = null },
                unitSystem = settingsUiState.unitSystem,
            )
        }
    }
}