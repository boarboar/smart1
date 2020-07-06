package com.example.android.weatherapp.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.preference.PreferenceManager
import com.example.android.weatherapp.database.*
import com.example.android.weatherapp.domain.*
import com.example.android.weatherapp.utils.DateUtils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.util.*

class SensorRepository(val appContext: Context) {

    private val database = getDatabase(appContext)
    var retention_days = 30

    companion object {
        const val tag = "SensorRepository"
        const val DEFAULT_DATA_RETENTION = 31
    }

    val errorHandler = CoroutineExceptionHandler { _, error ->
        Log.e(tag, "Coroutine DB error: $error")
    }

    val job = Job()

    lateinit private var _currentSensor: LiveData<Sensor>

    val weather = MutableLiveData<Weather>()
    var forecastList = MutableLiveData<ArrayList<WeatherForecastItem>>()

    val currentSensor: LiveData<Sensor>
        get() = _currentSensor

    lateinit private var _sensorDataList: LiveData<List<SensorData>>
    private var _sensorDataId = 0

    private var _logList: LiveData<List<LogRecord>> =
        try {
            Transformations.map(database.weatherDao.getLogs()) {
                it.asLogRecord()
            }
        } catch (t: Throwable) {
            val msg = t.message ?: "Unknown DB error"
            Log.e(tag, "DB error: $msg")
            MutableLiveData<List<LogRecord>>()
        }

    val logList: LiveData<List<LogRecord>>
        get() = _logList

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

        setRetention()

        // is it legal?
        sensorList.observeForever {
            if (null != it) {
                Log.i(tag, "==== READ ${it.size} sensors")
                sensorLastId = it.maxBy {it.id}?.id ?: 0
            }
        }
        //logEvent(DbLog.SEVERITY_CODE.INFO, tag, "Started")

        //Log.i(tag, "==== READ ${_logList.value?.size} logs")

    }

    fun setRetention() {
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(appContext)
        retention_days = sharedPreferences.getString("data_retention_days", DEFAULT_DATA_RETENTION.toString())?.toInt() ?:  DEFAULT_DATA_RETENTION
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
        withContext(Dispatchers.IO + errorHandler +job) {

            try {
                Log.i(tag, "REFRESH RUN")

                database.runInTransaction {
                    for (d in data) _refreshSensorData(d)

                    // update outdated to refresh UI
                    database.weatherDao.updateOutdated(System.currentTimeMillis() - Sensor.MEAS_OUTDATED_PERIOD)


                    val stat = database.weatherDao.getSensorDataStat()
                    Log.i(
                        tag,
                        "${stat.count} total data records in DB from  ${DateUtils.convertDate(stat.from)}  to ${DateUtils.convertDate(
                            stat.to
                        )}"
                    )

                    // cleanup old records

                    Log.w(tag, "Clear measurements older than $retention_days days")
                    val delcount =
                        database.weatherDao.clearSensorData(System.currentTimeMillis() - 24L * 3600L * 1000L * retention_days)
                    Log.w(tag, "Cleared $delcount records")
                }

//                val logstat = database.weatherDao.getLogStat()
//                Log.i(tag, "${logstat.count} total log records in DB from  ${DateUtils.convertDate(logstat.from)}  to ${DateUtils.convertDate(logstat.to)}")
//
                val log_retention_days = 7
                Log.w(tag, "Clear logs older than $log_retention_days days")
                val dellogcount = database.weatherDao.clearLog(System.currentTimeMillis() - 24L * 3600L * 1000L * log_retention_days)
                Log.w(tag, "Cleared $dellogcount records")

                true
            }  catch (t: Throwable) {
                val msg = t.message ?: "Unknown DB error"
                Log.e(tag, "DB error: $msg")
                false
            }
        }

//    suspend fun refreshSensorData(sdata : SensorTransferData): Boolean =
//        withContext(Dispatchers.IO) {
//            try {
//                if(sdata.isValid) {
//                    val latest = database.weatherDao.getSensorLatestData(sdata.sensor_id)
//                    if(latest != null && latest.event_stamp == sdata.event_stamp) {
//                        Log.i(tag, "Refresh sensor - old data ${sdata}")
//                        _logEvent(LogRecord.SEVERITY_CODE.INFO, tag, "Refresh sensor - old data ${sdata.sensor_id}")
//                    }
//                    else {
//                        Log.i(tag, "Refresh sensor ${sdata}")
//                        val data = DbSensorData(sdata)
//                        database.weatherDao.insert_data(data)
//                        val latest_update: DbSensorLatestData =
//                            latest?.let {
//                                //Log.i(tag, "Sensor ${data.sensor_id} prev latest data is ${latest}")
//                                DbSensorLatestData(it, sdata)
//                            } ?: DbSensorLatestData(sdata)
//                        //database.weatherDao.insert_latest_data_and_update_sensor(latest_update) // transaction
//
//                        database.weatherDao.insert_latest_data(latest_update)
//                        database.weatherDao.updateSensor(sdata.sensor_id)
//
//                        _logEvent(LogRecord.SEVERITY_CODE.INFO, tag, "Refresh sensor ${sdata.sensor_id}")
//                    }
//                } else {
//                    Log.w(tag, "Refresh sensor - invalid data ${sdata}")
//                    _logEvent(LogRecord.SEVERITY_CODE.ERROR, tag, "Refresh sensor - invalid data ${sdata}")
//                }
//                true
//            }  catch (t: Throwable) {
//                val msg = t.message ?: "Unknown DB error"
//                Log.e(tag, "DB error: $msg")
//                false
//            }
//        }

    private fun _refreshSensorData(sdata : SensorTransferData)
    {
        if(sdata.isValid) {
            val latest = database.weatherDao.getSensorLatestData(sdata.sensor_id)
            if(latest != null && latest.event_stamp == sdata.event_stamp) {
                Log.i(tag, "Refresh sensor - old data ${sdata}")
                _logEvent(LogRecord.SEVERITY_CODE.INFO, tag, "Refresh sensor - old data ${sdata.sensor_id}")
            }
            else {
                Log.i(tag, "Refresh sensor ${sdata}")
                val data = DbSensorData(sdata)
                database.weatherDao.insert_data(data)
                val latest_update: DbSensorLatestData =
                    latest?.let {
                        DbSensorLatestData(it, sdata)
                    } ?: DbSensorLatestData(sdata)
                //database.weatherDao.insert_latest_data_and_update_sensor(latest_update) // transaction

                database.weatherDao.insert_latest_data(latest_update)
                database.weatherDao.updateSensor(sdata.sensor_id)

                _logEvent(LogRecord.SEVERITY_CODE.INFO, tag, "Refresh sensor ${sdata.sensor_id}")
            }
        } else {
            Log.w(tag, "Refresh sensor - invalid data ${sdata}")
            _logEvent(LogRecord.SEVERITY_CODE.ERROR, tag, "Refresh sensor - invalid data ${sdata}")
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

    private fun _logEvent(severity: LogRecord.SEVERITY_CODE, tag: String, msg: String) {
        //database.weatherDao.insert_log(DbLog(0, System.currentTimeMillis(), severity.value, tag, msg ))
    }

    suspend fun logEvent(severity: LogRecord.SEVERITY_CODE, tag: String, msg: String) {
        withContext(Dispatchers.IO) {
            try {
                //database.weatherDao.insert_log(DbLog(0, System.currentTimeMillis(), severity.value, tag, msg ))
            } catch (t: Throwable) {
                val errmsg = t.message ?: "Unknown DB error"
                Log.e(tag, "DB error: $errmsg")
            }
        }
    }

//    suspend fun populateDb() {
//
//        withContext(Dispatchers.IO) {
//            try {
//                database.weatherDao.insert(DbSensor(1, "room", System.currentTimeMillis()))
//                database.weatherDao.insert(DbSensor(2, "balcony", System.currentTimeMillis()))
//                database.weatherDao.insert(DbSensor(3, "bath", System.currentTimeMillis()))
//            } catch (t: Throwable) {
//                val msg = t.message ?: "Unknown DB error"
//                Log.e(tag, "DB error: $msg")
//            }
//
//            try {
//                database.weatherDao.insert_data(
//                    DbSensorData(
//                        0,
//                        1,
//                        System.currentTimeMillis(),
//                        -100,
//                        3500,
//                        -1,
//                        -1
//                    )
//                )
//                database.weatherDao.insert_data(
//                    DbSensorData(
//                        0,
//                        2,
//                        System.currentTimeMillis(),
//                        100,
//                        3400,
//                        -1,
//                        -1
//                    )
//                )
//                database.weatherDao.insert_data(
//                    DbSensorData(
//                        0,
//                        3,
//                        System.currentTimeMillis(),
//                        250,
//                        3500,
//                        850,
//                        1
//                    )
//                )
//            } catch (t: Throwable) {
//                val msg = t.message ?: "Unknown DB error"
//                Log.e(tag, "DB error: $msg")
//            }
//        }
//    }
//
//    suspend fun updateSensorsDb() {
//        Log.i(tag, "Update sens test")
//        withContext(Dispatchers.IO) {
//            try {
//                database.weatherDao.update(DbSensor(1, "SENSOR1", System.currentTimeMillis()))
//                database.weatherDao.update(DbSensor(2, "SENSOR2", System.currentTimeMillis()))
//                database.weatherDao.update(DbSensor(3, "SENSOR3", 0))
//                database.weatherDao.update(DbSensor(4, "SENSOR4", 0))
//                //database.weatherDao.insert(DbSensor(2, "balcony", System.currentTimeMillis()))
//                //database.weatherDao.insert(DbSensor(3, "bath", System.currentTimeMillis()))
//                database.weatherDao.insert_data(
//                    DbSensorData(
//                        0,
//                        1,
//                        System.currentTimeMillis(),
//                        155,
//                        3500,
//                        -1,
//                        -1
//                    )
//                )
//                //database.weatherDao.insert_data(DbSensorData(0,2, System.currentTimeMillis(), 100, 3400, -1, -1))
//                //database.weatherDao.insert_data(DbSensorData(0,3, System.currentTimeMillis(), 250, 3500, 850, 1))
//                database.weatherDao.insert_data(
//                    DbSensorData(
//                        0,
//                        3,
//                        System.currentTimeMillis(),
//                        254,
//                        3567,
//                        950,
//                        1
//                    )
//                )
//                //database.weatherDao.insert(DbSensor(4, "WC", System.currentTimeMillis()))
//            } catch (t: Throwable) {
//                val msg = t.message ?: "Unknown DB error"
//                Log.e(tag, "DB error: $msg")
//            }
//        }
//    }
//
//    suspend fun deleteSensorDataDb() {
//        Log.i(tag, "Delete sens data test")
//        withContext(Dispatchers.IO) {
//            try {
//                database.weatherDao.deleteSensorData(3)
//            } catch (t: Throwable) {
//                val msg = t.message ?: "Unknown DB error"
//                Log.e(tag, "DB error: $msg")
//            }
//        }
//    }

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
