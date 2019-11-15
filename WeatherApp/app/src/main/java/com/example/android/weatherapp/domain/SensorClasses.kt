package com.example.android.weatherapp.domain

import com.example.android.weatherapp.utils.DateUtils

data class Sensor(val id: Int, val description: String, val updated: Long=0) {
    val at : String
        get() = DateUtils.convertTimeShort(updated * 1000)
    // add latest data here
}

data class SensorData(
    val sensor_id: Short,
    val timestamp: Long,
    val temp: Short,
    val vcc: Short,
    val hum: Short,
    val dhum: Short
)