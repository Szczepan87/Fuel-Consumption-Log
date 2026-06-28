package com.lszczepanski.fuelconsumptionlog

import com.lszczepanski.fuelconsumptionlog.domain.model.CarDraft
import com.lszczepanski.fuelconsumptionlog.domain.model.CarDraftValidator
import com.lszczepanski.fuelconsumptionlog.domain.model.RefuelDraft
import com.lszczepanski.fuelconsumptionlog.domain.model.RefuelDraftValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SharedCommonTest {

    @Test
    fun validatesCorrectDraft() {
        val draft = CarDraft(
            brand = "Ford",
            model = "Focus",
            engineCapacityCm3 = "1600",
            horsePower = "125",
            registrationNumber = "KR1234A",
            mileageKm = "210000,5",
        )

        val result = CarDraftValidator.validate(draft)

        assertTrue(result.isSuccess)
        val car = result.getOrThrow()
        assertEquals("KR1234A", car.registrationNumber)
        assertEquals(210000.5, car.mileageKm)
    }

    @Test
    fun rejectsNegativeMileage() {
        val draft = CarDraft(
            brand = "Ford",
            model = "Focus",
            engineCapacityCm3 = "1600",
            horsePower = "125",
            registrationNumber = "KR1234A",
            mileageKm = "-1",
        )

        val result = CarDraftValidator.validate(draft)

        assertTrue(result.isFailure)
    }

    @Test
    fun rejectsTooLargeEngineCapacity() {
        val draft = CarDraft(
            brand = "Ford",
            model = "Focus",
            engineCapacityCm3 = "100000",
            horsePower = "125",
            registrationNumber = "KR1234A",
            mileageKm = "210000",
        )

        val result = CarDraftValidator.validate(draft)

        assertTrue(result.isFailure)
    }

    @Test
    fun rejectsRegistrationWithSpecialCharacters() {
        val draft = CarDraft(
            brand = "Ford",
            model = "Focus",
            engineCapacityCm3 = "1600",
            horsePower = "125",
            registrationNumber = "KR-1234A",
            mileageKm = "210000",
        )

        val result = CarDraftValidator.validate(draft)

        assertTrue(result.isFailure)
    }

    @Test
    fun allowsRefuelWithoutFuelLiters() {
        val draft = RefuelDraft(
            fuelLiters = "",
            odometerKm = "221000,5",
        )

        val result = RefuelDraftValidator.validate(draft)

        assertTrue(result.isSuccess)
        assertEquals(null, result.getOrThrow().fuelLiters)
        assertEquals(221000.5, result.getOrThrow().odometerKm)
    }

    @Test
    fun rejectsNonPositiveFuelLiters() {
        val draft = RefuelDraft(
            fuelLiters = "0",
            odometerKm = "221000",
        )

        val result = RefuelDraftValidator.validate(draft)

        assertTrue(result.isFailure)
    }

    @Test
    fun acceptsFuelLitersWithDecimalSeparator() {
        val draft = RefuelDraft(
            fuelLiters = "42.5",
            odometerKm = "221000",
        )

        val result = RefuelDraftValidator.validate(draft)

        assertTrue(result.isSuccess)
        assertEquals(42.5, result.getOrThrow().fuelLiters)
    }

    @Test
    fun acceptsCommaAsDecimalSeparatorInFuelLiters() {
        val draft = RefuelDraft(
            fuelLiters = "42,5",
            odometerKm = "221000",
        )

        val result = RefuelDraftValidator.validate(draft)

        assertTrue(result.isSuccess)
        assertEquals(42.5, result.getOrThrow().fuelLiters)
    }
}