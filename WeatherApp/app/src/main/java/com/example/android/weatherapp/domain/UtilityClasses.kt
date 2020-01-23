package com.example.android.weatherapp.domain

import com.example.android.weatherapp.utils.DateUtils


data class LogRecord(
    val id: Int,
    val timestamp: Long,
    val severity: SEVERITY_CODE,
    val tag: String,
    val msg: String
)  {
    enum class SEVERITY_CODE(val value: Int) {
        NONE(0), ERROR(1), WARNING(2), INFO(3);
        companion object {
            fun valueOf(value: Int) = values().find { it.value == value } ?: NONE
        }
    }
    val at : String
        get() = if(timestamp>0) DateUtils.convertDateTime(timestamp) else "---"
    val severityString : String
        get() =  severity.name
}