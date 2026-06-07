package com.lszczepanski.fuelconsumptionlog.domain.model

object RefuelDraftValidator {
    fun validate(draft: RefuelDraft, saveAsDraft: Boolean): Result<RefuelInput> {
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

        if (saveAsDraft) {
            return Result.success(
                RefuelInput(
                    fuelLiters = liters,
                    odometerKm = odometer,
                    isDraft = true,
                )
            )
        }

        if (liters == null) {
            return Result.failure(IllegalArgumentException("Ilosc paliwa jest wymagana przy zapisie finalnym."))
        }
        if (liters <= 0.0) {
            return Result.failure(IllegalArgumentException("Ilosc paliwa musi byc wieksza od zera."))
        }

        return Result.success(
            RefuelInput(
                fuelLiters = liters,
                odometerKm = odometer,
                isDraft = false,
            )
        )
    }
}

