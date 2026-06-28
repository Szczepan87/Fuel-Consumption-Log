package com.lszczepanski.fuelconsumptionlog.domain.model

object CarDraftValidator {
    private val registrationPattern = Regex("^[A-Z0-9]+$")
    private val decimalPattern = Regex("^\\d+(?:[.,]\\d+)?$")

    fun validate(draft: CarDraft): Result<CarInput> {
        val brand = draft.brand.trim()
        val model = draft.model.trim()
        val registration = draft.registrationNumber.trim().uppercase()
        val engineCapacityRaw = draft.engineCapacityCm3.trim()
        val horsePowerRaw = draft.horsePower.trim()
        val mileageRaw = draft.mileageKm.trim()

        if (brand.isBlank()) return Result.failure(IllegalArgumentException("Marka jest wymagana."))
        if (model.isBlank()) return Result.failure(IllegalArgumentException("Model jest wymagany."))
        if (registration.isBlank()) return Result.failure(IllegalArgumentException("Numer rejestracyjny jest wymagany."))
        if (!registrationPattern.matches(registration)) {
            return Result.failure(IllegalArgumentException("Numer rejestracyjny moze zawierac tylko litery i cyfry."))
        }

        if (engineCapacityRaw.isEmpty() || !engineCapacityRaw.all(Char::isDigit)) {
            return Result.failure(IllegalArgumentException("Pojemnosc silnika musi byc liczba calkowita."))
        }
        val engineCapacity = engineCapacityRaw.toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Pojemnosc silnika musi byc liczba calkowita."))
        if (engineCapacity <= 0) {
            return Result.failure(IllegalArgumentException("Pojemnosc silnika musi byc wieksza od zera."))
        }
        if (engineCapacity > 99999) {
            return Result.failure(IllegalArgumentException("Pojemnosc silnika moze wynosic maksymalnie 99999 cm3."))
        }

        if (horsePowerRaw.isEmpty() || !horsePowerRaw.all(Char::isDigit)) {
            return Result.failure(IllegalArgumentException("Moc silnika musi byc liczba calkowita."))
        }
        val horsePower = horsePowerRaw.toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Moc silnika musi byc liczba calkowita."))
        if (horsePower <= 0) {
            return Result.failure(IllegalArgumentException("Moc silnika musi byc wieksza od zera."))
        }

        if (!decimalPattern.matches(mileageRaw)) {
            return Result.failure(IllegalArgumentException("Przebieg musi byc liczba."))
        }
        val mileage = mileageRaw.replace(',', '.').toDoubleOrNull()
            ?: return Result.failure(IllegalArgumentException("Przebieg musi byc liczba."))
        if (mileage < 0) {
            return Result.failure(IllegalArgumentException("Przebieg nie moze byc ujemny."))
        }

        return Result.success(
            CarInput(
                brand = brand,
                model = model,
                engineCapacityCm3 = engineCapacity,
                horsePower = horsePower,
                registrationNumber = registration,
                mileageKm = mileage,
            )
        )
    }
}
