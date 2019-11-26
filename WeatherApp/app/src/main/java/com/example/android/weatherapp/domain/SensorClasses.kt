package com.example.android.weatherapp.domain

import com.example.android.weatherapp.utils.DateUtils

data class Sensor(val id: Short, val description: String, val updated: Long=0,
                  val data : SensorData? = null
) {
    val at : String
        get() = DateUtils.convertDateTime(updated)
    val temp : String
        get() = data?.temp?.toString() ?: "---"

    //fun equalData(other : Sensor) = id==other.id && description==other.description && updated==other.updated
  }


data class SensorData(
    val sensor_id: Short,
    val timestamp: Long?,
    val temp: Short,
    val vcc: Short,
    val hum: Short,
    val dhum: Short
)