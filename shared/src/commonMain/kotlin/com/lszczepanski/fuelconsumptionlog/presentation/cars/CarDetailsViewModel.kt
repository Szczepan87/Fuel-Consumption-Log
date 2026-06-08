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
    val initialMileageKm: Int? = null,
    val refuels: List<RefuelEntry> = emptyList(),
    val averageConsumption: Double? = null,
    val isDialogOpen: Boolean = false,
    val isEditing: Boolean = false,
    val editingRefuelId: Long? = null,
    val editingOdometerKm: Int? = null,
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
        _uiState.update {
            it.copy(
                isDialogOpen = true,
                isEditing = false,
                editingRefuelId = null,
                editingOdometerKm = null,
                draft = RefuelDraft(),
                errorMessage = null,
                isSaving = false,
            )
        }
    }

    fun onEditRefuelClick(refuel: RefuelEntry) {
        _uiState.update {
            it.copy(
                isDialogOpen = true,
                isEditing = true,
                editingRefuelId = refuel.id,
                editingOdometerKm = refuel.odometerKm,
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
        _uiState.update { it.copy(isDialogOpen = false, errorMessage = null, isSaving = false) }
    }

    fun onDraftChanged(draft: RefuelDraft) {
        _uiState.update { it.copy(draft = draft, errorMessage = null) }
    }

    fun onSaveRefuel(saveAsDraft: Boolean) {
        val state = _uiState.value
        if (state.isSaving) return

        val draftToValidate = if (state.isEditing && state.editingOdometerKm != null) {
            state.draft.copy(odometerKm = state.editingOdometerKm.toString())
        } else {
            state.draft
        }

        val validation = RefuelDraftValidator.validate(
            draft = draftToValidate,
            saveAsDraft = saveAsDraft,
        )

        validation
            .onSuccess { input ->
                _uiState.update { it.copy(isSaving = true, errorMessage = null) }
                viewModelScope.launch {
                    val saveResult = if (state.isEditing) {
                        repository.updateRefuel(refuelId = state.editingRefuelId!!, input = input)
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
                                    editingOdometerKm = null,
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
        initialMileageKm: Int?,
    ): Double? {
        val completed = refuels
            .filter { !it.isDraft && it.fuelLiters != null }
            .sortedBy { it.odometerKm }

        if (completed.isEmpty()) return null

        val baseMileage = initialMileageKm ?: 0
        val distanceKm = completed.last().odometerKm - baseMileage
        if (distanceKm <= 0) return null

        val totalLiters = completed.sumOf { it.fuelLiters ?: 0.0 }

        return (totalLiters * 100.0) / distanceKm.toDouble()
    }
}

