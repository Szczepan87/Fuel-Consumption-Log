package com.lszczepanski.fuelconsumptionlog.domain.model

data class CarDraft(
    val brand: String = "",
    val model: String = "",
    val engineCapacityCm3: String = "",
    val horsePower: String = "",
    val registrationNumber: String = "",
    val mileageKm: String = "",
)

data class CarInput(
    val brand: String,
    val model: String,
    val engineCapacityCm3: Int,
    val horsePower: Int,
    val registrationNumber: String,
    val mileageKm: Double,
)
