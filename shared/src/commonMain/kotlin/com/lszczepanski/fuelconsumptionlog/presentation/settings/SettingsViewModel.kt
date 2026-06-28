package com.lszczepanski.fuelconsumptionlog.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lszczepanski.fuelconsumptionlog.data.local.SettingsRepository
import com.lszczepanski.fuelconsumptionlog.domain.model.UnitSystem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val errorMessage: String? = null,
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(unitSystem = settingsRepository.getUnitSystem()) }
        }
    }

    fun onUnitSystemSelected(unitSystem: UnitSystem) {
        if (_uiState.value.unitSystem == unitSystem) return

        _uiState.update {
            it.copy(
                unitSystem = unitSystem,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            settingsRepository
                .setUnitSystem(unitSystem)
                .onFailure { error ->
                    val currentValue = settingsRepository.getUnitSystem()
                    _uiState.update {
                        it.copy(
                            unitSystem = currentValue,
                            errorMessage = error.message ?: "Nie udalo sie zapisac ustawienia.",
                        )
                    }
                }
        }
    }
}
