package com.lszczepanski.fuelconsumptionlog.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.lszczepanski.fuelconsumptionlog.db.FuelLogDatabase
import com.lszczepanski.fuelconsumptionlog.domain.model.Car
import com.lszczepanski.fuelconsumptionlog.domain.model.CarInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SqlDelightCarRepository(
    databaseDriverFactory: DatabaseDriverFactory,
) : CarRepository {

    private val database = FuelLogDatabase(databaseDriverFactory.createDriver())
    private val queries = database.fuelLogDatabaseQueries

    override fun observeCars(): Flow<List<Car>> {
        return queries.selectAllCars()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                rows.map { row ->
                    Car(
                        id = row.id,
                        brand = row.brand,
                        model = row.model,
                        engineCapacityCm3 = row.engine_capacity_cm3.toInt(),
                        horsePower = row.horse_power.toInt(),
                        registrationNumber = row.registration_number,
                        mileageKm = row.mileage_km.toInt(),
                    )
                }
            }
    }

    override suspend fun addCar(input: CarInput): Result<Unit> {
        return runCatching {
            queries.insertCar(
                brand = input.brand,
                model = input.model,
                engine_capacity_cm3 = input.engineCapacityCm3.toLong(),
                horse_power = input.horsePower.toLong(),
                registration_number = input.registrationNumber,
                mileage_km = input.mileageKm.toLong(),
            )
        }
    }
}


