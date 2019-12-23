package com.example.android.weatherapp.sensorview

import android.app.Application
import androidx.lifecycle.*
import com.example.android.weatherapp.MainActivity
import com.example.android.weatherapp.domain.Sensor
import com.example.android.weatherapp.domain.SensorData
import com.example.android.weatherapp.draw.DrawView
import com.example.android.weatherapp.repository.getSensorRepository

class SensorChartViewModel(sensorId : Int, app: Application) : AndroidViewModel(app) {
    lateinit private var _sensorData : LiveData<List<SensorData>>
    val sensorData: LiveData<List<SensorData>>
        get() = _sensorData

    private val _chartDispType = MutableLiveData<DrawView.DispType>()
    val chartDispType : MutableLiveData<DrawView.DispType>
        get() = _chartDispType

    private val _chartDispPeriod = MutableLiveData<DrawView.DispPeriod>()
    val chartDispPeriod : MutableLiveData<DrawView.DispPeriod>
        get() = _chartDispPeriod

    init {
        _sensorData = getSensorRepository(app).getSensorData(sensorId)
    }

    fun onSetTypeTerm() {
        _chartDispType.value = DrawView.DispType.TEMPERATURE
    }
    fun onSetTypeVcc() {
        _chartDispType.value = DrawView.DispType.VCC
    }
    fun onSetTypeHum() {
        _chartDispType.value = DrawView.DispType.HUMIDITY
    }
    fun onSetPeriodDay() {
        _chartDispPeriod.value = DrawView.DispPeriod.DAY
    }
    fun onSetPeriodWeek() {
        _chartDispPeriod.value = DrawView.DispPeriod.WEEK
    }
    fun onSetPeriodMonth() {
        _chartDispPeriod.value = DrawView.DispPeriod.MONTH
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
