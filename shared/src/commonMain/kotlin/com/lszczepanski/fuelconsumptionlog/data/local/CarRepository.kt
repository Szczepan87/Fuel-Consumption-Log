package com.lszczepanski.fuelconsumptionlog.data.local

import com.lszczepanski.fuelconsumptionlog.domain.model.Car
import com.lszczepanski.fuelconsumptionlog.domain.model.CarInput
import com.lszczepanski.fuelconsumptionlog.domain.model.RefuelEntry
import com.lszczepanski.fuelconsumptionlog.domain.model.RefuelInput

interface CarRepository {
    suspend fun getCars(): List<Car>
    suspend fun getCarById(carId: Long): Car?
    suspend fun getRefuelsByCarId(carId: Long): List<RefuelEntry>

    suspend fun addCar(input: CarInput): Result<Unit>
    suspend fun addRefuel(carId: Long, input: RefuelInput): Result<Unit>
    suspend fun updateRefuel(carId: Long, refuelId: Long, input: RefuelInput): Result<Unit>
}

