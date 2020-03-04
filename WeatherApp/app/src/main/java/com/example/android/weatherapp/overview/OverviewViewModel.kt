package com.example.android.weatherapp.overview

import android.app.Application
import android.content.SharedPreferences
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
        private val SENSOR_REFRESH_TIMEOUT_MIN =  15L
    }

    private val wservice : WeatherServiceApi by lazy  { WeatherServiceApi.obtain() }
    //private val sservice : SensorServiceApi by lazy  { SensorServiceApi.obtain() }
    private lateinit var sservice : SensorServiceApi
    private var wServiceTimer = Timer()
    private var sServiceTimer = Timer()

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main )
    private val sensorRepository = getSensorRepository(application)

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)

    private val _status = MutableLiveData<WeatherApiStatus>()
    val status: LiveData<WeatherApiStatus>
        get() = _status

    val weather: LiveData<Weather>
        get() = sensorRepository.weather

    val forecastItemList: LiveData<ArrayList<WeatherForecastItem>>
        get() = sensorRepository.forecastList

    val sensorList = sensorRepository.sensorList

    private val _navigateToSelectedSensor = MutableLiveData<Sensor>()

    val navigateToSelectedSensor: LiveData<Sensor>
        get() = _navigateToSelectedSensor

    val prefChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { preferences, s ->
        Log.i(tag, "Preference $s changed")
        when (s) {
            "forecast_refresh_min", "location_code" -> {
                Log.i(tag, "Restart forecast scheduler")
                wServiceTimer.cancel()
                scheduleForecast()
            }
            "sensor_refresh_min" -> {
                Log.i(tag, "Restart sensor refresh scheduler")
                sServiceTimer.cancel()
                scheduleSensorRefresh()
            }
            "sensor_service_url" -> {
                Log.i(tag, "Change URL, restart sensor refresh scheduler")
                sServiceTimer.cancel()
                sservice = SensorServiceApi.obtain()
                scheduleSensorRefresh()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    init {
        sservice = SensorServiceApi.obtain()
        scheduleSensorRefresh()
        scheduleForecast()
        sharedPreferences.registerOnSharedPreferenceChangeListener(prefChangeListener)
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

    private fun scheduleForecast() {
        val pollTimeoutWeather: Long = sharedPreferences.getString(
            "forecast_refresh_min",
            FORECAST_REFRESH_TIMEOUT_MIN.toString()
        )?.toLong() ?: FORECAST_REFRESH_TIMEOUT_MIN
        wServiceTimer = Timer()
        wServiceTimer.schedule(400, 1000*60*pollTimeoutWeather) { getWeatherForecast() }
    }

    private fun scheduleSensorRefresh() {
        val pollTimeoutSensor: Long = sharedPreferences.getString(
            "sensor_refresh_min",
            SENSOR_REFRESH_TIMEOUT_MIN.toString()
        )?.toLong() ?: SENSOR_REFRESH_TIMEOUT_MIN
        sServiceTimer = Timer()
        sServiceTimer.schedule(2000, 1000 * 60 * pollTimeoutSensor) { getSensorsData() }
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
