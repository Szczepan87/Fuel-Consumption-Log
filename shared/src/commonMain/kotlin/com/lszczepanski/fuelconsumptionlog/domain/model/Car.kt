package com.lszczepanski.fuelconsumptionlog.domain.model

data class Car(
    val id: Long,
    val brand: String,
    val model: String,
    val engineCapacityCm3: Int,
    val horsePower: Int,
    val registrationNumber: String,
    val initialMileageKm: Double,
    val mileageKm: Double,
)
