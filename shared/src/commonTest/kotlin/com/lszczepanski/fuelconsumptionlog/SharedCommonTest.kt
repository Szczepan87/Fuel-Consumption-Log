package com.lszczepanski.fuelconsumptionlog

import com.lszczepanski.fuelconsumptionlog.domain.model.CarDraft
import com.lszczepanski.fuelconsumptionlog.domain.model.CarDraftValidator
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
            registrationNumber = "KR 1234A",
            mileageKm = "210000",
        )

        val result = CarDraftValidator.validate(draft)

        assertTrue(result.isSuccess)
        val car = result.getOrThrow()
        assertEquals("KR 1234A", car.registrationNumber)
        assertEquals(210000, car.mileageKm)
    }

    @Test
    fun rejectsNegativeMileage() {
        val draft = CarDraft(
            brand = "Ford",
            model = "Focus",
            engineCapacityCm3 = "1600",
            horsePower = "125",
            registrationNumber = "KR 1234A",
            mileageKm = "-1",
        )

        val result = CarDraftValidator.validate(draft)

        assertTrue(result.isFailure)
    }
}