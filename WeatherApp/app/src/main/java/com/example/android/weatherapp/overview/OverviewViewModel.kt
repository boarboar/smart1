package com.example.android.weatherapp.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.weatherapp.R
import kotlinx.coroutines.*

enum class WeatherApiStatus { LOADING, ERROR, DONE }

class OverviewViewModel : ViewModel() {

    private val _test = MutableLiveData<String>()
    val test: LiveData<String>
        get() = _test

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main )

    private val _status = MutableLiveData<WeatherApiStatus>()
    val status: LiveData<WeatherApiStatus>
        get() = _status

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    init {
        _test.value = "NONE"
        getWeatherForecast()
    }

    private fun getWeatherForecast() {
        coroutineScope.launch {
            //var getPropertiesDeferred = MarsApi.retrofitService.getProperties(filter.value)
            try {
                _status.value = WeatherApiStatus.LOADING
                _test.value = "LOADING"
                //var listResult = getPropertiesDeferred.await()
                delay(5_000)
                _status.value = WeatherApiStatus.DONE
                _test.value = "DONE"
                //_properties.value = listResult
            } catch (e: Exception) {
                _status.value = WeatherApiStatus.ERROR
                _test.value = "ERROR"
            }
        }
    }

}
