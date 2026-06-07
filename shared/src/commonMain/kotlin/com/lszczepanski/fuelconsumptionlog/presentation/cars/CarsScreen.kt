package com.lszczepanski.fuelconsumptionlog.presentation.cars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lszczepanski.fuelconsumptionlog.data.local.CarRepository
import com.lszczepanski.fuelconsumptionlog.domain.model.Car
import com.lszczepanski.fuelconsumptionlog.domain.model.CarDraft
import com.lszczepanski.fuelconsumptionlog.domain.model.CarDraftValidator
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CarsScreen(
    repository: CarRepository,
    onCarSelected: (Long) -> Unit,
) {
    val cars by repository.observeCars().collectAsState(initial = emptyList())
    var isDialogOpen by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf(CarDraft()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Samochody") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    isDialogOpen = true
                    errorMessage = null
                    draft = CarDraft()
                }
            ) {
                Text("+")
            }
        },
    ) { padding ->
        if (cars.isEmpty()) {
            Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
                Text("Brak zapisanych samochodow.")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Kliknij +, aby dodac pierwszy samochod.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(cars, key = { it.id }) { car ->
                    CarRow(
                        car = car,
                        onClick = { onCarSelected(car.id) },
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (isDialogOpen) {
        AddCarDialog(
            draft = draft,
            errorMessage = errorMessage,
            onDismiss = { isDialogOpen = false },
            onDraftChanged = { draft = it },
            onSave = {
                val inputResult = CarDraftValidator.validate(draft)
                inputResult
                    .onSuccess { input ->
                        coroutineScope.launch {
                            val saveResult = repository.addCar(input)
                            saveResult
                                .onSuccess {
                                    isDialogOpen = false
                                    draft = CarDraft()
                                    errorMessage = null
                                }
                                .onFailure { error ->
                                    errorMessage = error.message ?: "Nie udalo sie zapisac samochodu."
                                }
                        }
                    }
                    .onFailure { error ->
                        errorMessage = error.message ?: "Nieprawidlowe dane formularza."
                    }
            },
        )
    }
}

@Composable
private fun CarRow(
    car: Car,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${car.brand} ${car.model}", style = MaterialTheme.typography.titleMedium)
            Text(car.registrationNumber, style = MaterialTheme.typography.bodyMedium)
            Text("Przebieg: ${car.mileageKm} km", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun AddCarDialog(
    draft: CarDraft,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onDraftChanged: (CarDraft) -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj samochod") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = draft.brand,
                        onValueChange = { onDraftChanged(draft.copy(brand = it)) },
                        label = { Text("Marka") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = draft.model,
                        onValueChange = { onDraftChanged(draft.copy(model = it)) },
                        label = { Text("Model") },
                        singleLine = true,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = draft.engineCapacityCm3,
                        onValueChange = { onDraftChanged(draft.copy(engineCapacityCm3 = it)) },
                        label = { Text("Pojemnosc cm3") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = draft.horsePower,
                        onValueChange = { onDraftChanged(draft.copy(horsePower = it)) },
                        label = { Text("KM") },
                        singleLine = true,
                    )
                }
                OutlinedTextField(
                    value = draft.registrationNumber,
                    onValueChange = { onDraftChanged(draft.copy(registrationNumber = it)) },
                    label = { Text("Numer rejestracyjny") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = draft.mileageKm,
                    onValueChange = { onDraftChanged(draft.copy(mileageKm = it)) },
                    label = { Text("Przebieg (km)") },
                    singleLine = true,
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        },
    )
}



