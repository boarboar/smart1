package com.example.android.weatherapp.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.preference.PreferenceManager
import com.example.android.weatherapp.database.*
import com.example.android.weatherapp.domain.Sensor
import com.example.android.weatherapp.domain.SensorData
import com.example.android.weatherapp.domain.SensorTransferData
import com.example.android.weatherapp.overview.OverviewViewModel
import com.example.android.weatherapp.work.RefreshDataWorker
import com.example.android.weatherapp.work.nextInt
import com.example.android.weatherapp.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.*

class SensorRepository(val appContext: Context) {

    private val database = com.example.android.weatherapp.database.getDatabase(appContext)

    companion object {
        const val tag = "SensorRepository"
        const val DEFAULT_DATA_RETENTION = 31
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
            Transformations.map(database.weatherDao.getSensorsWithLatestData()) {
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
            _currentSensor = Transformations.map(database.weatherDao.getOneSensorWithLatestData(id)) {
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


    fun getSensorData(id : Int) : LiveData<List<SensorData>> =
        if (::_sensorDataList.isInitialized && _sensorDataId == id)
            _sensorDataList
        else try {
            Log.i(tag, "Loading sensor data ${id}")
            _sensorDataList =Transformations.map(database.weatherDao.getSensorData(id)) {
                it.asSensorData() }
            _sensorDataId = id
            _sensorDataList
        } catch (t: Throwable) {
            val msg = t.message ?: "Unknown DB error"
            Log.e(tag, "DB error: $msg")
            _sensorDataList = MutableLiveData<List<SensorData>>()
            _sensorDataList
        }


    suspend fun refreshSensorsData(data: List<SensorTransferData>): Boolean =
        withContext(Dispatchers.IO) {
            try {
                Log.i(tag, "REFRESH RUN")
                for(d in data)  refreshSensorData(d)
                // update outdated to refresh UI
                database.weatherDao.updateOutdated(System.currentTimeMillis()-Sensor.MEAS_OUTDATED_PERIOD)
                //val count = database.weatherDao.getSensorDataCount()
                val stat = database.weatherDao.getSensorDataStat()
                Log.i(tag, "${stat.count} total data records in DB from  ${DateUtils.convertDate(stat.from)}  to ${DateUtils.convertDate(stat.to)}")

                // cleanup old records
                val sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(appContext)
                val retention_days = sharedPreferences.getString("data_retention_days", DEFAULT_DATA_RETENTION.toString()).toInt()
                Log.w(tag, "Clear measurements older than $retention_days days")
                val delcount = database.weatherDao.clearSensorData(System.currentTimeMillis() - 24L * 3600L * 1000L * retention_days)
                Log.w(tag, "Cleared $delcount records")

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

    suspend fun refreshSensorData(sdata : SensorTransferData): Boolean =
        withContext(Dispatchers.IO) {
            try {
                if(sdata.isValid) {
                    val latest = database.weatherDao.getSensorLatestData(sdata.sensor_id)
                    if(latest != null && latest.event_stamp == sdata.event_stamp) {
                        Log.i(tag, "Refresh sensor - old data ${sdata}")
                    }
                    else {
                        Log.i(tag, "Refresh sensor ${sdata}")
                        val data = DbSensorData(sdata)
                        database.weatherDao.insert_data(data)
                        val latest_update: DbSensorLatestData =
                            latest?.let {
                                //Log.i(tag, "Sensor ${data.sensor_id} prev latest data is ${latest}")
                                DbSensorLatestData(it, sdata)
                            } ?: DbSensorLatestData(sdata)
                        database.weatherDao.insert_latest_data_and_update_sensor(latest_update) // transaction
                    }
                } else
                    Log.w(tag, "Refresh sensor - invalid data ${sdata}")
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
