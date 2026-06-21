package com.lszczepanski.fuelconsumptionlog.domain.model

object RefuelDraftValidator {
    fun validate(draft: RefuelDraft): Result<RefuelInput> {
        val odometer = draft.odometerKm.trim().toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Stan licznika jest wymagany i musi byc liczba calkowita."))
        if (odometer < 0) {
            return Result.failure(IllegalArgumentException("Stan licznika nie moze byc ujemny."))
        }

        val litersRaw = draft.fuelLiters.trim()
        val liters = if (litersRaw.isBlank()) {
            null
        } else {
            litersRaw.replace(',', '.').toDoubleOrNull()
                ?: return Result.failure(IllegalArgumentException("Ilosc paliwa musi byc liczba."))
        }

        if (liters != null && liters <= 0.0) {
            return Result.failure(IllegalArgumentException("Ilosc paliwa musi byc wieksza od zera."))
        }

        return Result.success(
            RefuelInput(
                fuelLiters = liters,
                odometerKm = odometer,
            )
        )
    }
}

