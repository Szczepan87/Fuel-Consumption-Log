package com.lszczepanski.fuelconsumptionlog.domain.model

object RefuelDraftValidator {
    private val decimalPattern = Regex("^\\d+(?:[.,]\\d+)?$")

    fun validate(draft: RefuelDraft): Result<RefuelInput> {
        val odometerRaw = draft.odometerKm.trim()
        if (!decimalPattern.matches(odometerRaw)) {
            return Result.failure(IllegalArgumentException("Stan licznika jest wymagany i musi byc liczba."))
        }
        val odometer = odometerRaw.replace(',', '.').toDoubleOrNull()
            ?: return Result.failure(IllegalArgumentException("Stan licznika jest wymagany i musi byc liczba."))
        if (odometer < 0) {
            return Result.failure(IllegalArgumentException("Stan licznika nie moze byc ujemny."))
        }

        val litersRaw = draft.fuelLiters.trim()
        val liters = if (litersRaw.isBlank()) {
            null
        } else {
            if (!decimalPattern.matches(litersRaw)) {
                return Result.failure(IllegalArgumentException("Ilosc paliwa musi byc liczba."))
            }
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
