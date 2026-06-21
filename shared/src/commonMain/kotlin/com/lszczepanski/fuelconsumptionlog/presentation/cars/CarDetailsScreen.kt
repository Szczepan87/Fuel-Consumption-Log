package com.lszczepanski.fuelconsumptionlog.presentation.cars

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lszczepanski.fuelconsumptionlog.data.local.CarRepository
import com.lszczepanski.fuelconsumptionlog.domain.model.Car
import com.lszczepanski.fuelconsumptionlog.domain.model.RefuelDraft
import com.lszczepanski.fuelconsumptionlog.domain.model.RefuelEntry
import com.lszczepanski.fuelconsumptionlog.domain.model.RefuelInput
import com.lszczepanski.fuelconsumptionlog.util.formatEpochMillis
import kotlin.math.round

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CarDetailsScreen(
    viewModel: CarDetailsViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val latestRefuelId = remember(uiState.refuels) {
        uiState.refuels
            .maxWithOrNull(compareBy<RefuelEntry> { it.createdAtEpochMillis }.thenBy { it.id })
            ?.id
    }
    val canAddRefuel = remember(uiState.refuels) {
        uiState.refuels.isEmpty() || uiState.refuels.firstOrNull { it.id == latestRefuelId }?.fuelLiters != null
    }
    val refuelConsumptions = remember(uiState.refuels, uiState.initialMileageKm) {
        calculateRefuelConsumptions(
            refuels = uiState.refuels,
            initialMileageKm = uiState.initialMileageKm,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = if (uiState.car == null) {
                        "Szczegoly samochodu"
                    } else {
                        "${uiState.car!!.brand} ${uiState.car!!.model}"
                    }
                    Text(title)
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Wroc")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (canAddRefuel) {
                        viewModel.onAddRefuelClick()
                    }
                },
                modifier = Modifier.alpha(if (canAddRefuel) 1f else 0.5f),
            ) {
                Text("+")
            }
        },
    ) { padding ->
        if (uiState.car == null) {
            Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
                Text("Nie znaleziono samochodu.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                item {
                    CarHeaderCard(
                        brand = uiState.car!!.brand,
                        model = uiState.car!!.model,
                        registration = uiState.car!!.registrationNumber,
                        engineCapacity = uiState.car!!.engineCapacityCm3,
                        horsePower = uiState.car!!.horsePower,
                        mileageKm = uiState.car!!.mileageKm,
                        averageConsumption = uiState.averageConsumption,
                    )
                }
                item {
                    Text(
                        text = "Tankowania",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    )
                }
                if (!canAddRefuel) {
                    item {
                        Text(
                            "Uzupelnij litry w najnowszym tankowaniu, aby dodac kolejne.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                if (uiState.refuels.isEmpty()) {
                    item {
                        Text("Brak tankowan. Dodaj pierwsze tankowanie przyciskiem +.")
                    }
                } else {
                    items(uiState.refuels, key = { it.id }) { refuel ->
                        val isEditable = refuel.id == latestRefuelId
                        RefuelRow(
                            refuel = refuel,
                            consumptionPer100Km = refuelConsumptions[refuel.id],
                            isEditable = isEditable,
                            onClick = { viewModel.onEditRefuelClick(refuel) },
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (uiState.isDialogOpen) {
        RefuelDialog(
            draft = uiState.draft,
            isEditing = uiState.isEditing,
            errorMessage = uiState.errorMessage,
            isSaving = uiState.isSaving,
            onDraftChanged = viewModel::onDraftChanged,
            onDismiss = viewModel::onDismissDialog,
            onSave = viewModel::onSaveRefuel,
        )
    }
}

@Composable
private fun CarHeaderCard(
    brand: String,
    model: String,
    registration: String,
    engineCapacity: Int,
    horsePower: Int,
    mileageKm: Int,
    averageConsumption: Double?,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("$brand $model", style = MaterialTheme.typography.titleLarge)
            Text("Rejestracja: $registration")
            Text("Silnik: ${engineCapacity} cm3, ${horsePower} KM")
            Text("Aktualny przebieg: ${mileageKm} km")
            val avgText = averageConsumption?.let { "${round(it * 10.0) / 10.0} l/100 km" } ?: "Brak danych"
            Text("Srednie spalanie: $avgText", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun RefuelRow(
    refuel: RefuelEntry,
    consumptionPer100Km: Double?,
    isEditable: Boolean,
    onClick: () -> Unit,
) {
    val liters = refuel.fuelLiters?.let { "$it l" } ?: "do uzupelnienia"
    val consumptionText = consumptionPer100Km?.let { "${round(it * 10.0) / 10.0} l/100 km" } ?: "-"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEditable, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                Text(formatEpochMillis(refuel.createdAtEpochMillis), fontWeight = FontWeight.SemiBold)
                Text("Paliwo: $liters")
                Text("Licznik: ${refuel.odometerKm} km")
                if (isEditable) {
                    Text("Mozesz edytowac litry i przebieg", style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Spalanie", fontWeight = FontWeight.SemiBold)
                Text(consumptionText)
            }
        }
    }
}

private fun calculateRefuelConsumptions(
    refuels: List<RefuelEntry>,
    initialMileageKm: Int?,
): Map<Long, Double> {
    val completed = refuels
        .filter { it.fuelLiters != null }
        .sortedBy { it.odometerKm }

    if (completed.isEmpty()) return emptyMap()

    val result = mutableMapOf<Long, Double>()
    var previousOdometer = initialMileageKm ?: 0

    for (refuel in completed) {
        val distanceKm = refuel.odometerKm - previousOdometer
        val liters = refuel.fuelLiters ?: continue
        if (distanceKm > 0) {
            result[refuel.id] = (liters * 100.0) / distanceKm.toDouble()
        }
        previousOdometer = refuel.odometerKm
    }

    return result
}

@Composable
private fun RefuelDialog(
    draft: RefuelDraft,
    isEditing: Boolean,
    errorMessage: String?,
    isSaving: Boolean,
    onDraftChanged: (RefuelDraft) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEditing) "Edytuj tankowanie" else "Dodaj tankowanie")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = draft.fuelLiters,
                    onValueChange = { onDraftChanged(draft.copy(fuelLiters = it)) },
                    label = { Text("Paliwo (l)") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = draft.odometerKm,
                    onValueChange = { onDraftChanged(draft.copy(odometerKm = it)) },
                    label = { Text("Stan licznika (km)") },
                    singleLine = true,
                )
                Text(
                    "Data i godzina sa zapisywane automatycznie podczas dodawania.",
                    style = MaterialTheme.typography.bodySmall,
                )
                if (isEditing) {
                    Text(
                        "Edycja litrow i przebiegu jest dostepna tylko dla najnowszego wpisu.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
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


@Preview
@Composable
private fun CarHeaderCardPreview() {
    MaterialTheme {
        CarHeaderCard(
            brand = "Skoda",
            model = "Octavia",
            registration = "KR 1234A",
            engineCapacity = 1968,
            horsePower = 150,
            mileageKm = 182300,
            averageConsumption = 6.1,
        )
    }
}

@Preview
@Composable
private fun RefuelRowPreview() {
    MaterialTheme {
        RefuelRow(
            refuel = RefuelEntry(
                id = 1,
                carId = 1,
                createdAtEpochMillis = 1_780_000_000_000,
                fuelLiters = 42.7,
                odometerKm = 182300,
            ),
            consumptionPer100Km = 6.3,
            isEditable = true,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun RefuelDialogPreview() {
    MaterialTheme {
        RefuelDialog(
            draft = RefuelDraft(
                fuelLiters = "43.2",
                odometerKm = "182450",
            ),
            isEditing = false,
            errorMessage = null,
            isSaving = false,
            onDraftChanged = {},
            onDismiss = {},
            onSave = {},
        )
    }
}

@Preview
@Composable
private fun CarDetailsScreenPreview() {
    val previewCar = Car(
        id = 1,
        brand = "Skoda",
        model = "Octavia",
        engineCapacityCm3 = 1968,
        horsePower = 150,
        registrationNumber = "KR 1234A",
        initialMileageKm = 180000,
        mileageKm = 182300,
    )
    val previewRefuels = listOf(
        RefuelEntry(
            id = 3,
            carId = 1,
            createdAtEpochMillis = 1_780_010_000_000,
            fuelLiters = 40.1,
            odometerKm = 182300,
        ),
        RefuelEntry(
            id = 2,
            carId = 1,
            createdAtEpochMillis = 1_779_800_000_000,
            fuelLiters = null,
            odometerKm = 181900,
        ),
        RefuelEntry(
            id = 1,
            carId = 1,
            createdAtEpochMillis = 1_779_500_000_000,
            fuelLiters = 41.6,
            odometerKm = 181500,
        ),
    )

    val previewRepository = remember {
        object : CarRepository {
            override suspend fun getCars(): List<Car> = listOf(previewCar)

            override suspend fun getCarById(carId: Long): Car? = previewCar.takeIf { it.id == carId }

            override suspend fun getRefuelsByCarId(carId: Long): List<RefuelEntry> =
                previewRefuels.takeIf { carId == previewCar.id } ?: emptyList()

            override suspend fun addCar(input: com.lszczepanski.fuelconsumptionlog.domain.model.CarInput): Result<Unit> {
                return Result.success(Unit)
            }

            override suspend fun addRefuel(carId: Long, input: RefuelInput): Result<Unit> {
                return Result.success(Unit)
            }

            override suspend fun updateRefuel(carId: Long, refuelId: Long, input: RefuelInput): Result<Unit> {
                return Result.success(Unit)
            }
        }
    }

    val previewViewModel = remember {
        CarDetailsViewModel(
            carId = 1,
            repository = previewRepository,
        )
    }

    MaterialTheme {
        CarDetailsScreen(
            viewModel = previewViewModel,
            onBack = {},
        )
    }
}

