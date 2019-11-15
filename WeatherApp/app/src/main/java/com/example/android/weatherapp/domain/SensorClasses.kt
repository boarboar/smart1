package com.example.android.weatherapp.domain

import com.example.android.weatherapp.utils.DateUtils

data class Sensor(val id: Int, val description: String, val updated: Long=0) {
    val at : String
        get() = DateUtils.convertTimeShort(updated * 1000)
}