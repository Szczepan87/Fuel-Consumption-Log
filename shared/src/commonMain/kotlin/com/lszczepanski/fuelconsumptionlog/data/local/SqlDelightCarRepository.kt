package com.lszczepanski.fuelconsumptionlog.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.lszczepanski.fuelconsumptionlog.db.FuelLogDatabase
import com.lszczepanski.fuelconsumptionlog.domain.model.Car
import com.lszczepanski.fuelconsumptionlog.domain.model.CarInput
import com.lszczepanski.fuelconsumptionlog.domain.model.RefuelEntry
import com.lszczepanski.fuelconsumptionlog.domain.model.RefuelInput
import com.lszczepanski.fuelconsumptionlog.util.currentTimeMillis
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
            .map { rows -> rows.map(::mapCar) }
    }

    override fun observeCarById(carId: Long): Flow<Car?> {
        return queries.selectCarById(id = carId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { row -> row?.let(::mapCar) }
    }

    override fun observeRefuelsByCarId(carId: Long): Flow<List<RefuelEntry>> {
        return queries.selectRefuelsByCarId(car_id = carId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map(::mapRefuel) }
    }

    override suspend fun addCar(input: CarInput): Result<Unit> {
        return runCatching {
            val count = queries.countByRegistration(registration_number = input.registrationNumber).executeAsOne()
            if (count > 0L) {
                throw IllegalArgumentException("Samochod o tym numerze rejestracyjnym juz istnieje.")
            }

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

    override suspend fun addRefuel(carId: Long, input: RefuelInput): Result<Unit> {
        return runCatching {
            queries.insertRefuel(
                car_id = carId,
                created_at_epoch_millis = currentTimeMillis(),
                fuel_liters = input.fuelLiters,
                odometer_km = input.odometerKm.toLong(),
                is_draft = if (input.isDraft) 1L else 0L,
            )

            if (!input.isDraft) {
                updateCarMileageIfNeeded(carId = carId, odometerKm = input.odometerKm)
            }
        }
    }

    override suspend fun updateRefuel(refuelId: Long, input: RefuelInput): Result<Unit> {
        return runCatching {
            queries.updateRefuel(
                fuel_liters = input.fuelLiters,
                is_draft = if (input.isDraft) 1L else 0L,
                id = refuelId,
            )
        }
    }

    private fun updateCarMileageIfNeeded(carId: Long, odometerKm: Int) {
        val car = queries.selectCarById(id = carId).executeAsOneOrNull() ?: return
        if (odometerKm > car.mileage_km.toInt()) {
            queries.updateCarMileage(mileage_km = odometerKm.toLong(), id = carId)
        }
    }

    private fun mapCar(row: com.lszczepanski.fuelconsumptionlog.db.Cars): Car {
        return Car(
            id = row.id,
            brand = row.brand,
            model = row.model,
            engineCapacityCm3 = row.engine_capacity_cm3.toInt(),
            horsePower = row.horse_power.toInt(),
            registrationNumber = row.registration_number,
            mileageKm = row.mileage_km.toInt(),
        )
    }

    private fun mapRefuel(row: com.lszczepanski.fuelconsumptionlog.db.Refuel_entries): RefuelEntry {
        return RefuelEntry(
            id = row.id,
            carId = row.car_id,
            createdAtEpochMillis = row.created_at_epoch_millis,
            fuelLiters = row.fuel_liters,
            odometerKm = row.odometer_km.toInt(),
            isDraft = row.is_draft != 0L,
        )
    }
}


