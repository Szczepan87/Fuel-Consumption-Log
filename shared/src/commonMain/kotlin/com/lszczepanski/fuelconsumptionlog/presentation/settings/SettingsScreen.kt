package com.lszczepanski.fuelconsumptionlog.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lszczepanski.fuelconsumptionlog.domain.model.UnitSystem

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsScreen(
    unitSystem: UnitSystem,
    errorMessage: String?,
    onUnitSystemSelected: (UnitSystem) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ustawienia") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Wroc")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Jednostki", style = MaterialTheme.typography.titleMedium)
            UnitSystemOptionRow(
                title = "Metryczne (km, l/100 km)",
                selected = unitSystem == UnitSystem.METRIC,
                onClick = { onUnitSystemSelected(UnitSystem.METRIC) },
            )
            UnitSystemOptionRow(
                title = "Imperialne (mi, mpg)",
                selected = unitSystem == UnitSystem.IMPERIAL,
                onClick = { onUnitSystemSelected(UnitSystem.IMPERIAL) },
            )
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun UnitSystemOptionRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = title, modifier = Modifier.padding(start = 8.dp))
    }
}
