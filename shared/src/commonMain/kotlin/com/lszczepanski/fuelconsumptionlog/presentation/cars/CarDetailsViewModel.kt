package com.lszczepanski.fuelconsumptionlog.presentation.cars

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lszczepanski.fuelconsumptionlog.data.local.CarRepository
import com.lszczepanski.fuelconsumptionlog.domain.model.Car
import com.lszczepanski.fuelconsumptionlog.domain.model.RefuelDraft
import com.lszczepanski.fuelconsumptionlog.domain.model.RefuelDraftValidator
import com.lszczepanski.fuelconsumptionlog.domain.model.RefuelEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CarDetailsUiState(
    val car: Car? = null,
    val initialMileageKm: Double? = null,
    val refuels: List<RefuelEntry> = emptyList(),
    val averageConsumption: Double? = null,
    val isDialogOpen: Boolean = false,
    val isEditing: Boolean = false,
    val editingRefuelId: Long? = null,
    val draft: RefuelDraft = RefuelDraft(),
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
)

class CarDetailsViewModel(
    private val carId: Long,
    private val repository: CarRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CarDetailsUiState())
    val uiState: StateFlow<CarDetailsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            refreshData()
        }
    }

    fun onAddRefuelClick() {
        if (!canAddNewRefuel(_uiState.value.refuels)) {
            return
        }

        _uiState.update {
            it.copy(
                isDialogOpen = true,
                isEditing = false,
                editingRefuelId = null,
                draft = RefuelDraft(),
                errorMessage = null,
                isSaving = false,
            )
        }
    }

    fun onEditRefuelClick(refuel: RefuelEntry) {
        val latestRefuelId = latestRefuel(_uiState.value.refuels)?.id

        if (latestRefuelId == null || latestRefuelId != refuel.id) {
            return
        }

        _uiState.update {
            it.copy(
                isDialogOpen = true,
                isEditing = true,
                editingRefuelId = refuel.id,
                draft = RefuelDraft(
                    fuelLiters = refuel.fuelLiters?.toString() ?: "",
                    odometerKm = refuel.odometerKm.toString(),
                ),
                errorMessage = null,
                isSaving = false,
            )
        }
    }

    fun onDismissDialog() {
        _uiState.update {
            it.copy(
                isDialogOpen = false,
                isEditing = false,
                editingRefuelId = null,
                draft = RefuelDraft(),
                errorMessage = null,
                isSaving = false,
            )
        }
    }

    fun onDraftChanged(draft: RefuelDraft) {
        _uiState.update { it.copy(draft = draft, errorMessage = null) }
    }

    fun onSaveRefuel() {
        val state = _uiState.value
        if (state.isSaving) return

        if (state.isEditing) {
            val latestRefuel = latestRefuel(state.refuels)
            val canEditCurrent = latestRefuel != null && latestRefuel.id == state.editingRefuelId

            if (!canEditCurrent) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Edytowac mozna tylko najnowsze tankowanie.",
                        isSaving = false,
                    )
                }
                return
            }
        } else if (!canAddNewRefuel(state.refuels)) {
            return
        }

        val validation = RefuelDraftValidator.validate(state.draft)

        validation
            .onSuccess { input ->
                _uiState.update { it.copy(isSaving = true, errorMessage = null) }
                viewModelScope.launch {
                    val saveResult = if (state.isEditing) {
                        repository.updateRefuel(carId = carId, refuelId = state.editingRefuelId!!, input = input)
                    } else {
                        repository.addRefuel(carId = carId, input = input)
                    }

                    saveResult
                        .onSuccess {
                            val car = repository.getCarById(carId)
                            val refuels = repository.getRefuelsByCarId(carId)
                            _uiState.update {
                                val initialMileageKm = it.initialMileageKm ?: car?.initialMileageKm
                                it.copy(
                                    car = car,
                                    initialMileageKm = initialMileageKm,
                                    refuels = refuels,
                                    averageConsumption = calculateAverageConsumption(
                                        refuels = refuels,
                                        initialMileageKm = initialMileageKm,
                                    ),
                                    isDialogOpen = false,
                                    isEditing = false,
                                    editingRefuelId = null,
                                    draft = RefuelDraft(),
                                    errorMessage = null,
                                    isSaving = false,
                                )
                            }
                        }
                        .onFailure { error ->
                            _uiState.update {
                                it.copy(
                                    errorMessage = error.message ?: "Nie udalo sie zapisac tankowania.",
                                    isSaving = false,
                                )
                            }
                        }
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.message ?: "Nieprawidlowe dane tankowania.",
                        isSaving = false,
                    )
                }
            }
    }

    private suspend fun refreshData() {
        val car = repository.getCarById(carId)
        val refuels = repository.getRefuelsByCarId(carId)
        _uiState.update {
            val initialMileageKm = it.initialMileageKm ?: car?.initialMileageKm
            it.copy(
                car = car,
                initialMileageKm = initialMileageKm,
                refuels = refuels,
                averageConsumption = calculateAverageConsumption(
                    refuels = refuels,
                    initialMileageKm = initialMileageKm,
                ),
            )
        }
    }

    private fun calculateAverageConsumption(
        refuels: List<RefuelEntry>,
        initialMileageKm: Double?,
    ): Double? {
        val completed = refuels
            .filter { it.fuelLiters != null }
            .sortedBy { it.odometerKm }

        if (completed.isEmpty()) return null

        val baseMileage = initialMileageKm ?: 0.0
        val distanceKm = completed.last().odometerKm - baseMileage
        if (distanceKm <= 0) return null

        val totalLiters = completed.sumOf { it.fuelLiters ?: 0.0 }

        return (totalLiters * 100.0) / distanceKm
    }

    private fun latestRefuel(refuels: List<RefuelEntry>): RefuelEntry? {
        return refuels.maxWithOrNull(compareBy<RefuelEntry> { it.createdAtEpochMillis }.thenBy { it.id })
    }

    private fun canAddNewRefuel(refuels: List<RefuelEntry>): Boolean {
        return latestRefuel(refuels)?.fuelLiters != null || refuels.isEmpty()
    }
}
