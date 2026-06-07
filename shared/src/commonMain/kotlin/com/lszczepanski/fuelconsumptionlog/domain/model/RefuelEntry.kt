package com.lszczepanski.fuelconsumptionlog.domain.model

data class RefuelEntry(
    val id: Long,
    val carId: Long,
    val createdAtEpochMillis: Long,
    val fuelLiters: Double?,
    val odometerKm: Int,
    val isDraft: Boolean,
)

data class RefuelDraft(
    val fuelLiters: String = "",
    val odometerKm: String = "",
)

data class RefuelInput(
    val fuelLiters: Double?,
    val odometerKm: Int,
    val isDraft: Boolean,
)

