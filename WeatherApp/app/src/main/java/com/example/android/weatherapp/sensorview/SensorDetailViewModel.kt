package com.example.android.weatherapp.sensorview

import android.app.Application
import androidx.lifecycle.*
import com.example.android.weatherapp.MainActivity
import com.example.android.weatherapp.domain.Sensor
import com.example.android.weatherapp.repository.getSensorRepository

class SensorDetailViewModel(sensorId : Int, app: Application) : AndroidViewModel(app) {

    lateinit private var _sensor : LiveData<Sensor>

    val sensor: LiveData<Sensor>
        get() = _sensor

    init {
        _sensor = getSensorRepository(app).getOneSensor(sensorId)
    }
}

class SensorDetailViewModelFactory(
    private val sensorId: Int,
    private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SensorDetailViewModel::class.java)) {
            return SensorDetailViewModel(sensorId, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
