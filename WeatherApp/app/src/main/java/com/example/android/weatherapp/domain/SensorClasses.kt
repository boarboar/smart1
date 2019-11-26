package com.example.android.weatherapp.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.weatherapp.database.DbSensorData
import com.example.android.weatherapp.overview.DbStatus
import com.example.android.weatherapp.utils.DateUtils

data class Sensor(val id: Short, val description: String, val updated: Long=0,
                  //val _data: LiveData<List<DbSensorData>> = MutableLiveData<List<DbSensorData>>()
    val data : DbSensorData? = null
) {
    val at : String
        get() = DateUtils.convertDateTime(updated)
    val temp : String
        get() = data?.temp.toString() ?: "---"
  }


data class SensorData(
    val sensor_id: Short,
    val timestamp: Long?,
    val temp: Short,
    val vcc: Short,
    val hum: Short,
    val dhum: Short
)