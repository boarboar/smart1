package com.example.android.weatherapp.overview

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.android.weatherapp.R
import com.example.android.weatherapp.database.DbSensor
import com.example.android.weatherapp.database.asDomainModel
import com.example.android.weatherapp.database.getDatabase
import com.example.android.weatherapp.domain.Sensor
import com.example.android.weatherapp.domain.Weather
import com.example.android.weatherapp.domain.WeatherForecast
import com.example.android.weatherapp.domain.WeatherForecastItem
import com.example.android.weatherapp.network.WeatherServiceApi
import kotlinx.coroutines.*

enum class WeatherApiStatus { LOADING, ERROR, DONE }
enum class DbStatus { LOADING, ERROR, DONE }

class OverviewViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val tag = "OverviewViewModel"
        private val CITYCODE = "193312,Ru"
    }

    private val wservice : WeatherServiceApi by lazy  { WeatherServiceApi.obtain() }

    //private val _test = MutableLiveData<String>()
    //val test: LiveData<String>
    //    get() = _test

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main )
    private val database = getDatabase(application)

    // all data should be moved to Repository (todo)

    private val _status = MutableLiveData<WeatherApiStatus>()
    val status: LiveData<WeatherApiStatus>
        get() = _status

    private val _db_status = MutableLiveData<DbStatus>()
    val db_status: LiveData<DbStatus>
        get() = _db_status

    private val _weather = MutableLiveData<Weather>()
    val weather: LiveData<Weather>
        get() = _weather

    //private val _forecast = MutableLiveData<WeatherForecast>()
    private val _forecastList = MutableLiveData<ArrayList<WeatherForecastItem>>()
    val forecastItemList: LiveData<ArrayList<WeatherForecastItem>>
        get() = _forecastList


    //private var _sensorList = MutableLiveData<List<Sensor>>()
    lateinit private var _sensorList: LiveData<List<Sensor>>
    val sensorList: LiveData<List<Sensor>>
        get() = _sensorList



    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    init {
        //_test.value = "NONE"
        getWeatherForecast()

        getSensors()

        }

    private fun getSensors() {
/*
        _sensorList.value = arrayListOf(
            Sensor(1, "Window"),
            Sensor(2, "Balcony"),
            Sensor(3, "Bathroom"))
*/


            try {
                _db_status.value = DbStatus.LOADING
                _sensorList = Transformations.map(database.weatherDao.getSensors()) {
                    it.asDomainModel()
                }
                _db_status.value = DbStatus.DONE
            } catch (t: Throwable) {
                _db_status.value = DbStatus.ERROR
                val msg = t.message ?: "Unknown DB error"
                Log.e(tag, "DB error: $msg")
            }

    }

    private fun getWeatherForecast() {
        coroutineScope.launch {
            var getWeatherDeferred = wservice.getWeather(CITYCODE)
            var getWeatherForecastDeferred = wservice.getWeatherForecast(CITYCODE, cnt=6)
            try {
                _status.value = WeatherApiStatus.LOADING
                //_test.value = "LOADING"
                _weather.value = getWeatherDeferred.await()
                val forecastResult = getWeatherForecastDeferred.await()
                _forecastList.value = forecastResult.forecastList
                delay(1_000) // to show spinner
                _status.value = WeatherApiStatus.DONE
                //_test.value = "DONE"
                //_properties.value = listResult
            } catch (e: Exception) {
                _status.value = WeatherApiStatus.ERROR
                val msg = e.message ?: "Unknown network error"
                Log.e(tag, "NET error: $msg")
                //_test.value = "ERROR"
            }
        }
    }

    fun updateForecast() {
        getWeatherForecast()
    }

    private suspend fun populateDb() {
        withContext(Dispatchers.IO) {
            try {
                database.weatherDao.insert(DbSensor(1, "room"))
                database.weatherDao.insert(DbSensor(2, "balcony"))
                database.weatherDao.insert(DbSensor(3, "bath"))
            } catch (t: Throwable) {
                val msg = t.message ?: "Unknown DB error"
                Log.e(tag, "DB error: $msg")
            }
        }
    }

    fun onPopulate() {
        coroutineScope.launch {
            populateDb()
            getSensors()
        }
    }
}
