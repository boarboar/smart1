package com.example.android.weatherapp.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.weatherapp.database.DbSensor
import com.example.android.weatherapp.database.DbSensorData
import com.example.android.weatherapp.database.asSensor
import com.example.android.weatherapp.domain.Sensor
import com.example.android.weatherapp.overview.OverviewViewModel
import com.example.android.weatherapp.work.RefreshDataWorker
import com.example.android.weatherapp.work.nextInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.*

class SensorRepository(appContext: Context) {

    private val database = com.example.android.weatherapp.database.getDatabase(appContext)

    companion object {
        const val tag = "SensorRepository"
    }

    var sensorList: LiveData<List<Sensor>> =
        try {
            Transformations.map(database.weatherDao.getSensorsWithData()) {
                it.asSensor()
            }
            //_db_status.value = DbStatus.DONE
        } catch (t: Throwable) {
            val msg = t.message ?: "Unknown DB error"
            Log.e(tag, "DB error: $msg")
            MutableLiveData<List<Sensor>>()
        }


    suspend fun refreshSensorData(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                Log.i(tag, "REFRESH RUN")
                val random = Random()
                for (sensId in 1..3) {
                    database.weatherDao.insert_data(
                        DbSensorData(
                            0, sensId, System.currentTimeMillis(),
                            random.nextInt(-400..400), random.nextInt(2540..4950),
                            random.nextInt(10..1000), random.nextInt(1..3)
                        )
                    )
                    Log.i(tag, "Refresh sensor $sensId")
                }
                val count = database.weatherDao.getSensorDataCount()
                Log.i(tag, "Total data records in DB: $count")
                true
            } catch (e: HttpException) {
                val msg = e.message ?: "Unknown HTTP error"
                Log.e(tag, "HTTP error: $msg")
                false
            } catch (t: Throwable) {
                val msg = t.message ?: "Unknown DB error"
                Log.e(tag, "DB error: $msg")
                false
            }
        }

    suspend fun populateDb() {

        /*
        _sensorList.value = arrayListOf(
            Sensor(1, "Window"),
            Sensor(2, "Balcony"),
            Sensor(3, "Bathroom"))
*/

        withContext(Dispatchers.IO) {
            try {
                database.weatherDao.insert(DbSensor(1, "room", System.currentTimeMillis()))
                database.weatherDao.insert(DbSensor(2, "balcony", System.currentTimeMillis()))
                database.weatherDao.insert(DbSensor(3, "bath", System.currentTimeMillis()))
            } catch (t: Throwable) {
                val msg = t.message ?: "Unknown DB error"
                Log.e(tag, "DB error: $msg")
            }

            try {
                database.weatherDao.insert_data(
                    DbSensorData(
                        0,
                        1,
                        System.currentTimeMillis(),
                        -100,
                        3500,
                        -1,
                        -1
                    )
                )
                database.weatherDao.insert_data(
                    DbSensorData(
                        0,
                        2,
                        System.currentTimeMillis(),
                        100,
                        3400,
                        -1,
                        -1
                    )
                )
                database.weatherDao.insert_data(
                    DbSensorData(
                        0,
                        3,
                        System.currentTimeMillis(),
                        250,
                        3500,
                        850,
                        1
                    )
                )
            } catch (t: Throwable) {
                val msg = t.message ?: "Unknown DB error"
                Log.e(tag, "DB error: $msg")
            }
        }
    }

    suspend fun updateSensorsDb() {
        Log.i(tag, "Update sens test")
        withContext(Dispatchers.IO) {
            try {
                database.weatherDao.update(DbSensor(1, "SENSOR1", System.currentTimeMillis()))
                database.weatherDao.update(DbSensor(2, "SENSOR2", System.currentTimeMillis()))
                database.weatherDao.update(DbSensor(3, "SENSOR3", 0))
                database.weatherDao.update(DbSensor(4, "SENSOR4", 0))
                //database.weatherDao.insert(DbSensor(2, "balcony", System.currentTimeMillis()))
                //database.weatherDao.insert(DbSensor(3, "bath", System.currentTimeMillis()))
                database.weatherDao.insert_data(
                    DbSensorData(
                        0,
                        1,
                        System.currentTimeMillis(),
                        155,
                        3500,
                        -1,
                        -1
                    )
                )
                //database.weatherDao.insert_data(DbSensorData(0,2, System.currentTimeMillis(), 100, 3400, -1, -1))
                //database.weatherDao.insert_data(DbSensorData(0,3, System.currentTimeMillis(), 250, 3500, 850, 1))
                database.weatherDao.insert_data(
                    DbSensorData(
                        0,
                        3,
                        System.currentTimeMillis(),
                        254,
                        3567,
                        950,
                        1
                    )
                )
                //database.weatherDao.insert(DbSensor(4, "WC", System.currentTimeMillis()))
            } catch (t: Throwable) {
                val msg = t.message ?: "Unknown DB error"
                Log.e(tag, "DB error: $msg")
            }
        }
    }

    suspend fun deleteSensorDataDb() {
        Log.i(tag, "Delete sens data test")
        withContext(Dispatchers.IO) {
            try {
                database.weatherDao.deleteSensorData(3)
            } catch (t: Throwable) {
                val msg = t.message ?: "Unknown DB error"
                Log.e(tag, "DB error: $msg")
            }
        }
    }

}

private lateinit var INSTANCE: SensorRepository

fun getSensorRepository(context: Context): SensorRepository {
    synchronized(SensorRepository::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = SensorRepository(context.applicationContext)
        }
    }
    return INSTANCE
}
