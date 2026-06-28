package com.lszczepanski.fuelconsumptionlog.data.local

import com.lszczepanski.fuelconsumptionlog.db.FuelLogDatabase
import com.lszczepanski.fuelconsumptionlog.domain.model.Car
import com.lszczepanski.fuelconsumptionlog.domain.model.CarInput
import com.lszczepanski.fuelconsumptionlog.domain.model.RefuelEntry
import com.lszczepanski.fuelconsumptionlog.domain.model.RefuelInput
import com.lszczepanski.fuelconsumptionlog.util.currentTimeMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SqlDelightCarRepository(
    databaseDriverFactory: DatabaseDriverFactory,
) : CarRepository {

    private val database = FuelLogDatabase(databaseDriverFactory.createDriver())
    private val queries = database.fuelLogDatabaseQueries

    override suspend fun getCars(): List<Car> {
        return withContext(Dispatchers.Default) {
            queries.selectAllCars().executeAsList().map(::mapCar)
        }
    }

    override suspend fun getCarById(carId: Long): Car? {
        return withContext(Dispatchers.Default) {
            queries.selectCarById(id = carId).executeAsOneOrNull()?.let(::mapCar)
        }
    }

    override suspend fun getRefuelsByCarId(carId: Long): List<RefuelEntry> {
        return withContext(Dispatchers.Default) {
            queries.selectRefuelsByCarId(car_id = carId).executeAsList().map(::mapRefuel)
        }
    }

    override suspend fun addCar(input: CarInput): Result<Unit> {
        return runCatching {
            withContext(Dispatchers.Default) {
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
                    initial_mileage_km = input.mileageKm,
                    mileage_km = input.mileageKm,
                )
            }
        }
    }

    override suspend fun addRefuel(carId: Long, input: RefuelInput): Result<Unit> {
        return runCatching {
            withContext(Dispatchers.Default) {
                queries.insertRefuel(
                    car_id = carId,
                    created_at_epoch_millis = currentTimeMillis(),
                    fuel_liters = input.fuelLiters,
                    odometer_km = input.odometerKm,
                )

                updateCarMileageIfNeeded(carId = carId, odometerKm = input.odometerKm)
            }
        }
    }

    override suspend fun updateRefuel(carId: Long, refuelId: Long, input: RefuelInput): Result<Unit> {
        return runCatching {
            withContext(Dispatchers.Default) {
                queries.updateRefuel(
                    fuel_liters = input.fuelLiters,
                    odometer_km = input.odometerKm,
                    id = refuelId,
                )

                updateCarMileageIfNeeded(carId = carId, odometerKm = input.odometerKm)
            }
        }
    }

    private fun updateCarMileageIfNeeded(carId: Long, odometerKm: Double) {
        val car = queries.selectCarById(id = carId).executeAsOneOrNull() ?: return
        if (odometerKm > car.mileage_km) {
            queries.updateCarMileage(mileage_km = odometerKm, id = carId)
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
            initialMileageKm = row.initial_mileage_km,
            mileageKm = row.mileage_km,
        )
    }

    private fun mapRefuel(row: com.lszczepanski.fuelconsumptionlog.db.Refuel_entries): RefuelEntry {
        return RefuelEntry(
            id = row.id,
            carId = row.car_id,
            createdAtEpochMillis = row.created_at_epoch_millis,
            fuelLiters = row.fuel_liters,
            odometerKm = row.odometer_km,
        )
    }
}

