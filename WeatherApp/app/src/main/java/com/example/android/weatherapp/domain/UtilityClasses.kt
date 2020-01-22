package com.example.android.weatherapp.domain

import com.example.android.weatherapp.utils.DateUtils

data class LogRecord(
    val id: Int,
    val timestamp: Long,
    val severity: Int,
    val tag: String,
    val msg: String
)  {
    enum class SEVERITY_CODE(val value: Int) {
        ERROR(0), WARNING(1), INFO(2)
    }
    val at : String
        get() = if(timestamp>0) DateUtils.convertDateTime(timestamp) else "---"
}