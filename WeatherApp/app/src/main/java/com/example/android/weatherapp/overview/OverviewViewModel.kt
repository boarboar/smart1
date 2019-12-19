package com.example.android.weatherapp.overview

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.example.android.weatherapp.R
import com.example.android.weatherapp.database.*
import com.example.android.weatherapp.domain.*
import com.example.android.weatherapp.network.WeatherServiceApi
import com.example.android.weatherapp.repository.SensorRepository
import com.example.android.weatherapp.repository.getSensorRepository
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

enum class WeatherApiStatus { LOADING, ERROR, DONE }
//enum class DbStatus { LOADING, ERROR, DONE, START }

class OverviewViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val tag = "OverviewViewModel"
        private val CITYCODE = "193312,Ru"
        private val FORECAST_REFRESH_TIMEOUT_MIN =  15L
    }

    private val wservice : WeatherServiceApi by lazy  { WeatherServiceApi.obtain() }

    //private val _test = MutableLiveData<String>()
    //val test: LiveData<String>
    //    get() = _test

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main )
    //private val database = getDatabase(application)
    private val sensorRepository = getSensorRepository(application)

    // all data should be moved to Repository (todo)

    private val _status = MutableLiveData<WeatherApiStatus>()
    val status: LiveData<WeatherApiStatus>
        get() = _status

//    private val _db_status = MutableLiveData<DbStatus>()
//    val db_status: LiveData<DbStatus>
//        get() = _db_status

    private val _weather = MutableLiveData<Weather>()
    val weather: LiveData<Weather>
        get() = _weather

    private val _forecastList = MutableLiveData<ArrayList<WeatherForecastItem>>()
    val forecastItemList: LiveData<ArrayList<WeatherForecastItem>>
        get() = _forecastList


    //lateinit private var _sensorList: LiveData<List<Sensor>>
    //val sensorList: LiveData<List<Sensor>>
    //    get() = _sensorList

    val sensorList = sensorRepository.sensorList

    private val _navigateToSelectedSensor = MutableLiveData<Sensor>()

    val navigateToSelectedSensor: LiveData<Sensor>
        get() = _navigateToSelectedSensor



    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    init {
        Timer().schedule(400, 1000*60*FORECAST_REFRESH_TIMEOUT_MIN) { getWeatherForecast() }
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

    private fun getWeatherForecast() {
        coroutineScope.launch {
            var getWeatherDeferred = wservice.getWeather(CITYCODE)
            var getWeatherForecastDeferred = wservice.getWeatherForecast(CITYCODE, cnt=6)
            try {
                _status.value = WeatherApiStatus.LOADING
                _weather.value = getWeatherDeferred.await()
                val forecastResult = getWeatherForecastDeferred.await()
                _forecastList.value = forecastResult.forecastList
                delay(100) // to show spinner
                _status.value = WeatherApiStatus.DONE
            } catch (e: Exception) {
                _status.value = WeatherApiStatus.ERROR
                val msg = e.message ?: "Unknown network error"
                Log.e(tag, "NET error: $msg")
            }
        }
    }

    fun updateForecast() {
        getWeatherForecast()
    }

    fun getNewSensor() : Sensor {
        val id = sensorRepository.sensorLastId + 1
        return Sensor(id, "SENSOR$id")
    }

    fun onPopulate() {
        coroutineScope.launch {
            sensorRepository.populateDb()
            //getSensors()
        }
    }

    fun onUpdate() {
        coroutineScope.launch {
            sensorRepository.updateSensorsDb()
        }
    }

    fun onDeleteSensorData() {
        coroutineScope.launch {
            sensorRepository.deleteSensorDataDb()
        }
    }
}
