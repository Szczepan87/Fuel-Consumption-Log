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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lszczepanski.fuelconsumptionlog.data.local.CarRepository
import com.lszczepanski.fuelconsumptionlog.domain.model.Car
import com.lszczepanski.fuelconsumptionlog.domain.model.RefuelDraft
import com.lszczepanski.fuelconsumptionlog.domain.model.RefuelDraftValidator
import com.lszczepanski.fuelconsumptionlog.domain.model.RefuelEntry
import com.lszczepanski.fuelconsumptionlog.domain.model.RefuelInput
import com.lszczepanski.fuelconsumptionlog.util.formatEpochMillis
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.round

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CarDetailsScreen(
    carId: Long,
    repository: CarRepository,
    onBack: () -> Unit,
) {
    val car by repository.observeCarById(carId).collectAsState(initial = null)
    val refuels by repository.observeRefuelsByCarId(carId).collectAsState(initial = emptyList())
    val averageConsumption = remember(refuels) { calculateAverageConsumption(refuels) }

    var isDialogOpen by remember { mutableStateOf(false) }
    var editingRefuelId by remember { mutableStateOf<Long?>(null) }
    var editingOdometerKm by remember { mutableStateOf<Int?>(null) }
    var draft by remember { mutableStateOf(RefuelDraft()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = if (car == null) {
                        "Szczegoly samochodu"
                    } else {
                        "${car!!.brand} ${car!!.model}"
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
                    editingRefuelId = null
                    editingOdometerKm = null
                    draft = RefuelDraft()
                    errorMessage = null
                    isDialogOpen = true
                }
            ) {
                Text("+")
            }
        },
    ) { padding ->
        if (car == null) {
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
                        brand = car!!.brand,
                        model = car!!.model,
                        registration = car!!.registrationNumber,
                        engineCapacity = car!!.engineCapacityCm3,
                        horsePower = car!!.horsePower,
                        mileageKm = car!!.mileageKm,
                        averageConsumption = averageConsumption,
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

                if (refuels.isEmpty()) {
                    item {
                        Text("Brak tankowan. Dodaj pierwsze tankowanie przyciskiem +.")
                    }
                } else {
                    items(refuels, key = { it.id }) { refuel ->
                        RefuelRow(
                            refuel = refuel,
                            onClick = {
                                editingRefuelId = refuel.id
                                editingOdometerKm = refuel.odometerKm
                                draft = RefuelDraft(
                                    fuelLiters = refuel.fuelLiters?.toString() ?: "",
                                    odometerKm = refuel.odometerKm.toString(),
                                )
                                errorMessage = null
                                isDialogOpen = true
                            },
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (isDialogOpen) {
        val isEditing = editingRefuelId != null

        RefuelDialog(
            draft = draft,
            isEditing = isEditing,
            errorMessage = errorMessage,
            onDraftChanged = { draft = it },
            onDismiss = { isDialogOpen = false },
            onSave = { saveAsDraft ->
                val draftToValidate = if (isEditing && editingOdometerKm != null) {
                    draft.copy(odometerKm = editingOdometerKm!!.toString())
                } else {
                    draft
                }

                val validationResult = RefuelDraftValidator.validate(
                    draft = draftToValidate,
                    saveAsDraft = saveAsDraft,
                )

                validationResult
                    .onSuccess { input ->
                        coroutineScope.launch {
                            val saveResult = if (editingRefuelId == null) {
                                repository.addRefuel(carId = carId, input = input)
                            } else {
                                repository.updateRefuel(refuelId = editingRefuelId!!, input = input)
                            }

                            saveResult
                                .onSuccess {
                                    isDialogOpen = false
                                    draft = RefuelDraft()
                                    editingRefuelId = null
                                    editingOdometerKm = null
                                    errorMessage = null
                                }
                                .onFailure { error ->
                                    errorMessage = error.message ?: "Nie udalo sie zapisac tankowania."
                                }
                        }
                    }
                    .onFailure { error ->
                        errorMessage = error.message ?: "Nieprawidlowe dane tankowania."
                    }
            },
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
    onClick: () -> Unit,
) {
    val status = if (refuel.isDraft) "Draft" else "Zapisane"
    val liters = refuel.fuelLiters?.let { "$it l" } ?: "-"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(formatEpochMillis(refuel.createdAtEpochMillis), fontWeight = FontWeight.SemiBold)
            Text("Paliwo: $liters")
            Text("Licznik: ${refuel.odometerKm} km")
            Text("Status: $status")
        }
    }
}

@Composable
private fun RefuelDialog(
    draft: RefuelDraft,
    isEditing: Boolean,
    errorMessage: String?,
    onDraftChanged: (RefuelDraft) -> Unit,
    onDismiss: () -> Unit,
    onSave: (Boolean) -> Unit,
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
                    enabled = !isEditing,
                    singleLine = true,
                )
                Text(
                    "Data i godzina sa zapisywane automatycznie podczas dodawania.",
                    style = MaterialTheme.typography.bodySmall,
                )
                if (isEditing) {
                    Text(
                        "Podczas edycji nie mozna zmienic stanu licznika.",
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
            Row(horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onSave(true) }) {
                    Text("Zapisz draft")
                }
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = { onSave(false) }) {
                    Text("Zapisz")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        },
    )
}

private fun calculateAverageConsumption(refuels: List<RefuelEntry>): Double? {
    val completed = refuels
        .filter { !it.isDraft && it.fuelLiters != null }
        .sortedBy { it.odometerKm }

    if (completed.size < 2) return null

    val distanceKm = completed.last().odometerKm - completed.first().odometerKm
    if (distanceKm <= 0) return null

    var totalLiters = 0.0
    for (index in 1 until completed.size) {
        totalLiters += completed[index].fuelLiters ?: 0.0
    }

    return (totalLiters * 100.0) / distanceKm.toDouble()
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
                isDraft = false,
            ),
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
            onDraftChanged = {},
            onDismiss = {},
            onSave = {},
        )
    }
}

@Preview
@Composable
private fun CarDetailsScreenPreview() {
    val carFlow = remember {
        MutableStateFlow(
            Car(
                id = 1,
                brand = "Skoda",
                model = "Octavia",
                engineCapacityCm3 = 1968,
                horsePower = 150,
                registrationNumber = "KR 1234A",
                mileageKm = 182300,
            )
        )
    }
    val refuelsFlow = remember {
        MutableStateFlow(
            listOf(
                RefuelEntry(
                    id = 3,
                    carId = 1,
                    createdAtEpochMillis = 1_780_010_000_000,
                    fuelLiters = 40.1,
                    odometerKm = 182300,
                    isDraft = false,
                ),
                RefuelEntry(
                    id = 2,
                    carId = 1,
                    createdAtEpochMillis = 1_779_800_000_000,
                    fuelLiters = null,
                    odometerKm = 181900,
                    isDraft = true,
                ),
                RefuelEntry(
                    id = 1,
                    carId = 1,
                    createdAtEpochMillis = 1_779_500_000_000,
                    fuelLiters = 41.6,
                    odometerKm = 181500,
                    isDraft = false,
                ),
            )
        )
    }

    val previewRepository = remember {
        object : CarRepository {
            override fun observeCars(): Flow<List<Car>> = MutableStateFlow(emptyList())

            override fun observeCarById(carId: Long): Flow<Car?> = carFlow

            override fun observeRefuelsByCarId(carId: Long): Flow<List<RefuelEntry>> = refuelsFlow

            override suspend fun addCar(input: com.lszczepanski.fuelconsumptionlog.domain.model.CarInput): Result<Unit> {
                return Result.success(Unit)
            }

            override suspend fun addRefuel(carId: Long, input: RefuelInput): Result<Unit> {
                return Result.success(Unit)
            }

            override suspend fun updateRefuel(refuelId: Long, input: RefuelInput): Result<Unit> {
                return Result.success(Unit)
            }
        }
    }

    MaterialTheme {
        CarDetailsScreen(
            carId = 1,
            repository = previewRepository,
            onBack = {},
        )
    }
}

