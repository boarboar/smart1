package com.example.android.weatherapp.sensorview

import android.app.Application
import androidx.lifecycle.*
import com.example.android.weatherapp.MainActivity
import com.example.android.weatherapp.domain.Sensor
import com.example.android.weatherapp.domain.SensorData
import com.example.android.weatherapp.repository.getSensorRepository

class SensorChartViewModel(sensorId : Int, app: Application) : AndroidViewModel(app) {
    lateinit private var _sensorData : LiveData<List<SensorData>>
    val sensorData: LiveData<List<SensorData>>
        get() = _sensorData

    init {
        _sensorData = getSensorRepository(app).getSensorData(sensorId)
    }
}

class SensorChartViewModelFactory(
    private val sensorId: Int,
    private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SensorChartViewModel::class.java)) {
            return SensorChartViewModel(sensorId, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
