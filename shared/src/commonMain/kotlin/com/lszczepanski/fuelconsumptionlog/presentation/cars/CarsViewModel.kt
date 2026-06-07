package com.lszczepanski.fuelconsumptionlog.presentation.cars

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lszczepanski.fuelconsumptionlog.data.local.CarRepository
import com.lszczepanski.fuelconsumptionlog.domain.model.Car
import com.lszczepanski.fuelconsumptionlog.domain.model.CarDraft
import com.lszczepanski.fuelconsumptionlog.domain.model.CarDraftValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CarsUiState(
    val cars: List<Car> = emptyList(),
    val isAddDialogOpen: Boolean = false,
    val draft: CarDraft = CarDraft(),
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
)

class CarsViewModel(
    private val repository: CarRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CarsUiState())
    val uiState: StateFlow<CarsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            refreshCars()
        }
    }

    fun onAddCarClick() {
        _uiState.update {
            it.copy(
                isAddDialogOpen = true,
                draft = CarDraft(),
                errorMessage = null,
                isSaving = false,
            )
        }
    }

    fun onDismissDialog() {
        _uiState.update { it.copy(isAddDialogOpen = false, errorMessage = null, isSaving = false) }
    }

    fun onDraftChanged(draft: CarDraft) {
        _uiState.update { it.copy(draft = draft, errorMessage = null) }
    }

    fun onSaveCar() {
        val state = _uiState.value
        if (state.isSaving) return

        val validation = CarDraftValidator.validate(state.draft)
        validation
            .onSuccess { input ->
                _uiState.update { it.copy(isSaving = true, errorMessage = null) }
                viewModelScope.launch {
                    val saveResult = repository.addCar(input)
                    saveResult
                        .onSuccess {
                            val cars = repository.getCars()
                            _uiState.update {
                                it.copy(
                                    cars = cars,
                                    isAddDialogOpen = false,
                                    draft = CarDraft(),
                                    errorMessage = null,
                                    isSaving = false,
                                )
                            }
                        }
                        .onFailure { error ->
                            _uiState.update {
                                it.copy(
                                    errorMessage = error.message ?: "Nie udalo sie zapisac samochodu.",
                                    isSaving = false,
                                )
                            }
                        }
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.message ?: "Nieprawidlowe dane formularza.",
                        isSaving = false,
                    )
                }
            }
    }

    private suspend fun refreshCars() {
        _uiState.update { it.copy(cars = repository.getCars()) }
    }
}

