package com.lszczepanski.fuelconsumptionlog.domain.model

object CarDraftValidator {
    fun validate(draft: CarDraft): Result<CarInput> {
        val brand = draft.brand.trim()
        val model = draft.model.trim()
        val registration = draft.registrationNumber.trim().uppercase()

        if (brand.isBlank()) return Result.failure(IllegalArgumentException("Marka jest wymagana."))
        if (model.isBlank()) return Result.failure(IllegalArgumentException("Model jest wymagany."))
        if (registration.isBlank()) return Result.failure(IllegalArgumentException("Numer rejestracyjny jest wymagany."))

        val engineCapacity = draft.engineCapacityCm3.trim().toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Pojemnosc silnika musi byc liczba calkowita."))
        if (engineCapacity <= 0) {
            return Result.failure(IllegalArgumentException("Pojemnosc silnika musi byc wieksza od zera."))
        }

        val horsePower = draft.horsePower.trim().toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Moc silnika musi byc liczba calkowita."))
        if (horsePower <= 0) {
            return Result.failure(IllegalArgumentException("Moc silnika musi byc wieksza od zera."))
        }

        val mileage = draft.mileageKm.trim().toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Przebieg musi byc liczba calkowita."))
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

