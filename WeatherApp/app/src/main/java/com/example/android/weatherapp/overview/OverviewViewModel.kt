package com.example.android.weatherapp.overview

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.example.android.weatherapp.database.DbLog
import com.example.android.weatherapp.domain.*
import com.example.android.weatherapp.network.SensorServiceApi
import com.example.android.weatherapp.network.WeatherApiStatus
import com.example.android.weatherapp.network.WeatherServiceApi
import com.example.android.weatherapp.repository.SensorRepository
import com.example.android.weatherapp.repository.getSensorRepository
import com.example.android.weatherapp.work.RefreshDataWorker
import kotlinx.coroutines.*
import java.net.SocketTimeoutException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class OverviewViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val tag = "OverviewViewModel"
        private val DEF_CITYCODE = "193312,Ru"
        private val FORECAST_REFRESH_TIMEOUT_MIN =  30L
    }

    private val wservice : WeatherServiceApi by lazy  { WeatherServiceApi.obtain() }
    private val sservice : SensorServiceApi by lazy  { SensorServiceApi.obtain() }

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main )
    private val sensorRepository = getSensorRepository(application)

    // all data should be moved to Repository (todo)

    private val _status = MutableLiveData<WeatherApiStatus>()
    val status: LiveData<WeatherApiStatus>
        get() = _status

    //private val _weather = MutableLiveData<Weather>()
    val weather: LiveData<Weather>
        //get() = _weather
        get() = sensorRepository.weather

    //private val _forecastList = MutableLiveData<ArrayList<WeatherForecastItem>>()
    val forecastItemList: LiveData<ArrayList<WeatherForecastItem>>
        //get() = _forecastList
        get() = sensorRepository.forecastList

    val sensorList = sensorRepository.sensorList

    private val _navigateToSelectedSensor = MutableLiveData<Sensor>()

    val navigateToSelectedSensor: LiveData<Sensor>
        get() = _navigateToSelectedSensor


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    init {
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(application)
        val pollTimeout : Long = sharedPreferences.getString("forecast_refresh_min", FORECAST_REFRESH_TIMEOUT_MIN.toString())?.toLong() ?: FORECAST_REFRESH_TIMEOUT_MIN
        Timer().schedule(400, 1000*60*pollTimeout) { getWeatherForecast() }
        Timer().schedule(2000, 1000*5) { getSensorsData() }
        }

    fun displaySensorDetails(sensor: Sensor) {
        _navigateToSelectedSensor.value = sensor
    }

    fun displaySensorDetailsComplete() {
        _navigateToSelectedSensor.value = null
    }

    fun addSensor(sensor: Sensor) {
        coroutineScope.launch {
            sensorRepository.addSensor(sensor)
        }
    }

    private fun getSensorsData() {
        var res = false
        coroutineScope.launch {
            sensorRepository.logEvent(
                LogRecord.SEVERITY_CODE.INFO,
                tag,
                "Refreshing sensor data..."
            )
            var getSensorsDeferred = sservice.getSensors()
            try {
                val sdata = getSensorsDeferred.await()
                res = sensorRepository.refreshSensorsData(sdata)
                sensorRepository.logEvent(LogRecord.SEVERITY_CODE.INFO, tag, "Refresh OK")
            } catch (se: SocketTimeoutException) {
                Log.e(tag, "Socket timeout")
                sensorRepository.logEvent(
                    LogRecord.SEVERITY_CODE.ERROR,
                    tag,
                    "Controller - Socket timeout"
                )
            } catch (e: Exception) {
                val msg = e.message ?: "Unknown network error"
                Log.e(tag, "NET error: $msg")
                sensorRepository.logEvent(
                    LogRecord.SEVERITY_CODE.ERROR,
                    tag,
                    "Controller - Unknown error"
                )
            }
        }
    }

    private fun getWeatherForecast() {
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication())
        val citycode = sharedPreferences.getString("location_code", DEF_CITYCODE)
        Log.w(tag, "Forecast for: $citycode")
        coroutineScope.launch {
            sensorRepository.logEvent(LogRecord.SEVERITY_CODE.INFO, tag, "Getting forecast for $citycode")
            var getWeatherDeferred = wservice.getWeather(citycode)
            var getWeatherForecastDeferred = wservice.getWeatherForecast(citycode, cnt=12)
            try {
                _status.value = WeatherApiStatus.LOADING
                sensorRepository.weather.value = getWeatherDeferred.await()
                sensorRepository.forecastList.value = getWeatherForecastDeferred.await().forecastList
                //delay(100) // to show spinner
                sensorRepository.logEvent(LogRecord.SEVERITY_CODE.INFO, tag, "Forecast refresh OK")
                _status.value = WeatherApiStatus.DONE
            } catch (e: Exception) {
                val msg = e.message ?: "Unknown network error"
                Log.e(tag, "NET error: $msg")
                sensorRepository.logEvent(LogRecord.SEVERITY_CODE.ERROR, tag, "Forecast NET error: $msg")
                _status.value = WeatherApiStatus.ERROR
            }
        }
    }

    fun getNewSensor() : Sensor {
        val id = sensorRepository.sensorLastId + 1
        return Sensor(id, "SENSOR$id")
    }

//    fun updateForecast() {
//        getWeatherForecast()
//    }
//    fun onPopulate() {
//        coroutineScope.launch {
//            sensorRepository.populateDb()
//            //getSensors()
//        }
//    }
//
//    fun onUpdate() {
//        coroutineScope.launch {
//            sensorRepository.updateSensorsDb()
//        }
//    }
//
//    fun onDeleteSensorData() {
//        coroutineScope.launch {
//            sensorRepository.deleteSensorDataDb()
//        }
//    }

}
