package com.example.android.weatherapp.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.weatherapp.database.*
import com.example.android.weatherapp.domain.Sensor
import com.example.android.weatherapp.domain.SensorData
import com.example.android.weatherapp.overview.OverviewViewModel
import com.example.android.weatherapp.work.RefreshDataWorker
import com.example.android.weatherapp.work.nextInt
import com.example.android.weatherapp.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.*

class SensorRepository(appContext: Context) {

    private val database = com.example.android.weatherapp.database.getDatabase(appContext)

    companion object {
        const val tag = "SensorRepository"
    }

    lateinit private var _currentSensor: LiveData<Sensor>

    val currentSensor: LiveData<Sensor>
        get() = _currentSensor

    lateinit private var _sensorDataList: LiveData<List<SensorData>>
    private var _sensorDataId = 0

//    val currentSensorDataList: LiveData<List<SensorData>>
//        get() = _currentSensorDataList

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

    var sensorLastId = 0

    init {
        // is it legal?
        sensorList.observeForever {
            if (null != it) {
                Log.w(tag, "==== READ ${it.size} sensors")
                sensorLastId = it.maxBy {it.id}?.id ?: 0
            }
        }
    }

    fun getOneSensor(id : Int) : LiveData<Sensor> =
        if (::_currentSensor.isInitialized && _currentSensor?.value?.id == id) _currentSensor
        else try {
            //_currentSensorDataListValid = false
            Log.i(tag, "Loading sensor $id")
            _currentSensor = Transformations.map(database.weatherDao.getOneSensorWithData(id)) {
                it?.toSensor()
            }
            _currentSensor
        } catch (t: Throwable) {
            val msg = t.message ?: "Unknown DB error"
            Log.e(tag, "DB error: $msg")
            MutableLiveData<Sensor>()
        }

    suspend fun deleteSensor(id : Int) : Boolean =
        withContext(Dispatchers.IO) {
            try {
                Log.i(tag, "Delete sensor $id")
                database.weatherDao.deleteSensor(id)
                true
            } catch (t: Throwable) {
                val msg = t.message ?: "Unknown DB error"
                Log.e(tag, "DB error: $msg")
                false
            }
        }

    suspend fun updateSensor(sensor : Sensor) : Boolean =
        withContext(Dispatchers.IO) {
            try {
                Log.i(tag, "Update sensor ${sensor.id} ${sensor.description}")
                database.weatherDao.update(DbSensor(sensor.id, sensor.description, 0))
                true
            } catch (t: Throwable) {
                val msg = t.message ?: "Unknown DB error"
                Log.e(tag, "DB error: $msg")
                false
            }
        }


//    fun getCurrentSensorData() : LiveData<List<SensorData>> =
//        if (!::_currentSensor.isInitialized || _currentSensor.value == null)  {
//            Log.w(tag, "Attempt to load sensor data for not inited sensor")
//            _currentSensorDataList = MutableLiveData<List<SensorData>>()
//            _currentSensorDataList
//        }
//        else if (::_currentSensorDataList.isInitialized && _currentSensorDataListValid)
//            _currentSensorDataList
//        else try {
//            val sensor : Sensor= _currentSensor.value as Sensor
//            Log.i(tag, "Loading sensor data ${sensor.id}")
//            _currentSensorDataList =Transformations.map(database.weatherDao.getSensorData(sensor.id.toInt())) {
//                it.asSensorData() }
//            _currentSensorDataList
//        } catch (t: Throwable) {
//            val msg = t.message ?: "Unknown DB error"
//            Log.e(tag, "DB error: $msg")
//            _currentSensorDataList = MutableLiveData<List<SensorData>>()
//            _currentSensorDataList
//        }

    fun getSensorData(id : Int) : LiveData<List<SensorData>> =
        if (::_sensorDataList.isInitialized && _sensorDataId==id)
            _sensorDataList
        else try {
            Log.i(tag, "Loading sensor data ${id}")
            _sensorDataList =Transformations.map(database.weatherDao.getSensorData(id)) {
                it.asSensorData() }
            _sensorDataList
        } catch (t: Throwable) {
            val msg = t.message ?: "Unknown DB error"
            Log.e(tag, "DB error: $msg")
            _sensorDataList = MutableLiveData<List<SensorData>>()
            _sensorDataList
        }

    /*
    suspend fun getLastSensorId(): Int =
        withContext(Dispatchers.IO) {
            try {
                val res = database.weatherDao.getLastSensorId()
                Log.e(tag, "DB Result =========  $res")
                res
            } catch (t: Throwable) {
                val msg = t.message ?: "Unknown DB error"
                Log.e(tag, "DB error: $msg")
                0
            }
        }
    */

    suspend fun refreshSensorsData(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                Log.i(tag, "REFRESH RUN")
                val random = Random()
                for (sensId in 1..3) {
                    refreshSensorData(
                        DbSensorData(
                            0, sensId, System.currentTimeMillis(),
                            random.nextInt(-400..400), random.nextInt(2540..4950),
                            random.nextInt(10..1000), random.nextInt(1..3)
                        )
                    )
                    Log.i(tag, "Refresh sensor $sensId")
                }
                //val count = database.weatherDao.getSensorDataCount()
                val stat = database.weatherDao.getSensorDataStat()

                Log.i(tag, "${stat.count} total data records in DB from  ${DateUtils.convertDate(stat.from)}  to ${DateUtils.convertDate(stat.to)}")

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

    suspend fun refreshSensorData(data : DbSensorData): Boolean =
        withContext(Dispatchers.IO) {
            try {
                database.weatherDao.insert_data(data)
                val latest = database.weatherDao.getSensorLatestData(data.sensor_id)
                val latest_update : DbSensorLatestData =
                    latest?.let {
                        Log.i(tag, "Sensor ${data.sensor_id} prev latest data is ${latest}")
                        DbSensorLatestData(it, data)
                    } ?: DbSensorLatestData(data)

                database.weatherDao.insert_latest_data(latest_update) // insert or update

                Log.i(tag, "Refresh sensor ${data.sensor_id}")
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


    suspend fun addSensor(sensor : Sensor) {
        withContext(Dispatchers.IO) {
            try {
                database.weatherDao.insert(DbSensor(sensor.id, sensor.description, 0))
            } catch (t: Throwable) {
                val msg = t.message ?: "Unknown DB error"
                Log.e(tag, "DB error: $msg")
            }
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
