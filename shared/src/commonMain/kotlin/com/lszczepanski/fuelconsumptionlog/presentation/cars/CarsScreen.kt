package com.lszczepanski.fuelconsumptionlog.presentation.cars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lszczepanski.fuelconsumptionlog.domain.model.Car
import com.lszczepanski.fuelconsumptionlog.domain.model.CarDraft
import com.lszczepanski.fuelconsumptionlog.util.formatDecimal

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CarsScreen(
    viewModel: CarsViewModel,
    onCarSelected: (Long) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Samochody") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::onAddCarClick
            ) {
                Text("+")
            }
        },
    ) { padding ->
        if (uiState.cars.isEmpty()) {
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
                items(uiState.cars, key = { it.id }) { car ->
                    CarRow(
                        car = car,
                        onClick = { onCarSelected(car.id) },
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (uiState.isAddDialogOpen) {
        AddCarDialog(
            draft = uiState.draft,
            errorMessage = uiState.errorMessage,
            isSaving = uiState.isSaving,
            onDismiss = viewModel::onDismissDialog,
            onDraftChanged = viewModel::onDraftChanged,
            onSave = viewModel::onSaveCar,
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
            Text("Przebieg: ${formatDecimal(car.mileageKm)} km", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun AddCarDialog(
    draft: CarDraft,
    errorMessage: String?,
    isSaving: Boolean,
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
                        onValueChange = {
                            onDraftChanged(
                                draft.copy(
                                    engineCapacityCm3 = it.filter(Char::isDigit).take(5)
                                )
                            )
                        },
                        label = { Text("Pojemnosc cm3") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = draft.horsePower,
                        onValueChange = {
                            onDraftChanged(
                                draft.copy(
                                    horsePower = it.filter(Char::isDigit)
                                )
                            )
                        },
                        label = { Text("KM") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                }
                OutlinedTextField(
                    value = draft.registrationNumber,
                    onValueChange = {
                        onDraftChanged(
                            draft.copy(
                                registrationNumber = it.filter(Char::isLetterOrDigit).uppercase()
                            )
                        )
                    },
                    label = { Text("Numer rejestracyjny") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = draft.mileageKm,
                    onValueChange = {
                        onDraftChanged(
                            draft.copy(
                                mileageKm = filterDecimalInput(it)
                            )
                        )
                    },
                    label = { Text("Przebieg (km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
            TextButton(onClick = onSave, enabled = !isSaving) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Anuluj")
            }
        },
    )
}

private fun filterDecimalInput(value: String): String {
    val filtered = value.filter { it.isDigit() || it == '.' || it == ',' }
    val separatorIndex = filtered.indexOfFirst { it == '.' || it == ',' }
    if (separatorIndex == -1) return filtered

    val integerPart = filtered.substring(0, separatorIndex)
    val separator = filtered[separatorIndex]
    val fractionalPart = filtered.substring(separatorIndex + 1).filter(Char::isDigit)
    return integerPart + separator + fractionalPart
}
