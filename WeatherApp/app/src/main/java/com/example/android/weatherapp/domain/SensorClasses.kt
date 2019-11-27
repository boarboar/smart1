package com.example.android.weatherapp.domain

import com.example.android.weatherapp.utils.DateUtils

data class Sensor(val id: Short, val description: String, val updated: Long=0,
                  val data : SensorData? = null
) {
    val at : String
        get() = if(updated>0) DateUtils.convertDateTime(updated) else "---"

    val updatedAt : String
        get() = data?.timestamp?.let {DateUtils.convertDateTime(it)} ?: "---"
    //val temp : String
    //    get() = data?.temp?.toString() ?: "---"

    val tempString : String
        get() = data?.temp?.let {(it/10.0F).toString()+"º"} ?: "---"
    val vccString : String
        get() = data?.vcc?.let {((it/10)/100.0F).toString()+"v"} ?: "---"
    val humString : String
        get() = data?.hum?.let {if(it>0) (it/10.0F).toString()+"%" else ""} ?: ""
    val dhumString : String
        get() = data?.dhum?.let {if(it.toInt()==SensorData.DHUM_VALS.LEAK.value) "!" else ""} ?: ""

    //fun equalData(other : Sensor) = id==other.id && description==other.description && updated==other.updated
  }


data class SensorData(
    val sensor_id: Short,
    val timestamp: Long,
    val temp: Short,
    val vcc: Short,
    val hum: Short,
    val dhum: Short
) {
    enum class DHUM_VALS(val value: Int) {
        NOTSET(0), LEAK(1), NORM(2)
    }
}