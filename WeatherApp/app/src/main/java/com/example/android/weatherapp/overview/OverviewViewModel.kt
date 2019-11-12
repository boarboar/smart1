package com.example.android.weatherapp.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.weatherapp.R
import com.example.android.weatherapp.domain.Weather
import com.example.android.weatherapp.domain.WeatherForecast
import com.example.android.weatherapp.domain.WeatherForecastItem
import com.example.android.weatherapp.network.WeatherServiceApi
import kotlinx.coroutines.*

enum class WeatherApiStatus { LOADING, ERROR, DONE }

class OverviewViewModel : ViewModel() {

    companion object {
        private val CITYCODE = "193312,Ru"
    }

    private val wservice : WeatherServiceApi by lazy  { WeatherServiceApi.obtain() }
    private val _test = MutableLiveData<String>()
    //val test: LiveData<String>
    //    get() = _test

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main )

    private val _status = MutableLiveData<WeatherApiStatus>()
    val status: LiveData<WeatherApiStatus>
        get() = _status

    private val _weather = MutableLiveData<Weather>()
    val weather: LiveData<Weather>
        get() = _weather

    //private val _forecast = MutableLiveData<WeatherForecast>()
    private val _forecastList = MutableLiveData<ArrayList<WeatherForecastItem>>()

    val forecastItemList: LiveData<ArrayList<WeatherForecastItem>>
        get() = _forecastList


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    init {
        //_test.value = "NONE"
        getWeatherForecast()
    }

    private fun getWeatherForecast() {
        coroutineScope.launch {
            var getWeatherDeferred = wservice.getWeather(CITYCODE)
            var getWeatherForecastDeferred = wservice.getWeatherForecast(CITYCODE)
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
                //_test.value = "ERROR"
            }
        }
    }

}
