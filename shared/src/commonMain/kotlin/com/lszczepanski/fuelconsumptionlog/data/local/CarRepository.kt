package com.lszczepanski.fuelconsumptionlog.data.local

import com.lszczepanski.fuelconsumptionlog.domain.model.Car
import com.lszczepanski.fuelconsumptionlog.domain.model.CarInput
import kotlinx.coroutines.flow.Flow

interface CarRepository {
    fun observeCars(): Flow<List<Car>>
    suspend fun addCar(input: CarInput): Result<Unit>
}

