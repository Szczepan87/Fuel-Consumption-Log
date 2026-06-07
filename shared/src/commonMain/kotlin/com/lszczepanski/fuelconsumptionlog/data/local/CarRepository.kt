package com.lszczepanski.fuelconsumptionlog.data.local

import com.lszczepanski.fuelconsumptionlog.domain.model.Car
import com.lszczepanski.fuelconsumptionlog.domain.model.CarInput
import com.lszczepanski.fuelconsumptionlog.domain.model.RefuelEntry
import com.lszczepanski.fuelconsumptionlog.domain.model.RefuelInput
import kotlinx.coroutines.flow.Flow

interface CarRepository {
    fun observeCars(): Flow<List<Car>>
    fun observeCarById(carId: Long): Flow<Car?>
    fun observeRefuelsByCarId(carId: Long): Flow<List<RefuelEntry>>

    suspend fun addCar(input: CarInput): Result<Unit>
    suspend fun addRefuel(carId: Long, input: RefuelInput): Result<Unit>
    suspend fun updateRefuel(refuelId: Long, input: RefuelInput): Result<Unit>
}

