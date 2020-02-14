package com.example.android.weatherapp.domain

import android.os.Parcelable
import com.example.android.weatherapp.utils.DateUtils
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Sensor(val id: Int, val description: String, val updated: Long=0,
                  val data : SensorData? = null
) : Parcelable {

    companion object {
        const val VCC_LOW_1000 = 3500
        const val VCC_LOW_DHT11_1000 = 2900
        const val HUM_HIGH_10 = 900
        const val MEAS_OUTDATED_PERIOD = 1000L * 60L * 46L // 46 min
        const val MEAS_OBSOLETE_PERIOD = 1000L * 60L * 60L * 4L// 4hrs
    }

    val at : String
        get() = if(updated>0) DateUtils.convertDateTime(updated) else "---"

    val isOutdated : Boolean
        get() = updated < System.currentTimeMillis()-MEAS_OUTDATED_PERIOD

    val isObsolete : Boolean
        get() = updated < System.currentTimeMillis()-MEAS_OBSOLETE_PERIOD

    val updatedAt : String
        get() = if(isObsolete) "---" else data?.let {it.at} ?: "---"
    val tempString : String
        get() = if(isObsolete) "---" else data?.let {it.tempString} ?: "---"
    val tempStringWithDelta : String
        get() = if(isObsolete) "---" else data?.let {
            when {
                it.d_temp>0 -> "${it.tempString} \u21D7"
                it.d_temp<0 -> "${it.tempString} \u21D8"
                else -> "${it.tempString}"
            }} ?: "---"
    val vccString : String
        get() = if(isObsolete) "---" else data?.let {it.vccString} ?: "---"
    val humString : String
        get() = if(isObsolete) "---" else data?.let {it.humString} ?: ""
    val dhumString : String
        get() = if(isObsolete) "---" else data?.let {it.dhumString} ?: ""
    val dhumVal : String
        get() = if(isObsolete) "---" else data?.let {it.dhumVal} ?: ""
    val isVccLow : Boolean
        get () = if(isObsolete) false else data?.let { it.isVccLow } ?: false
    val isHumHigh : Boolean
        get () = if(isObsolete) false else data?.let { it.isHumHigh } ?: false
    val isLeakage : Boolean
        get () = if(isObsolete) false else data?.let { it.isLeakage } ?: false
    val idString : String
        get() = id.toString()

    //fun equalData(other : Sensor) = id==other.id && description==other.description && updated==other.updated

    fun validate() = id>0 && !description.isEmpty()
  }

@Parcelize
data class SensorData(
    val sensor_id: Int,
    val timestamp: Long,
    val temp: Short,
    val vcc: Short,
    val hum: Short,
    val dhum: Short,
    val d_temp : Short = 0
) : Parcelable {
    enum class DHUM_VALS(val value: Int) {
        NOTSET(0), LEAK(1), NORM(2)
    }
    val at : String
        get() = if(timestamp>0) DateUtils.convertDateTime(timestamp) else "---"
    val tempString : String
        get() = (temp/10.0F).toString()+"º"
    val vccString : String
        get() = ((vcc/10)/100.0F).toString()+"v"
    val humString : String
        get() = if(hum>0) (hum/10.0F).toString()+"%" else ""
    val dhumString : String
        get() = if(dhum.toInt()==DHUM_VALS.LEAK.value) "!" else ""
    val dhumVal : String
        get() = dhum.toString()
    val d_tempVal : String
        get() = when { d_temp>0 -> "U"; d_temp<0 -> "D"; else -> "_" }
    val isVccLow : Boolean
        get () = vcc < Sensor.VCC_LOW_1000
    val isHumHigh : Boolean
        get () = hum > Sensor.HUM_HIGH_10
    val isLeakage : Boolean
        get () = dhum.toInt()==DHUM_VALS.LEAK.value
    val asString : String
        get() = "$temp $vcc" // for test
}

data class SensorTransferData(
    @SerializedName("I") val sensor_id: Int,
    @SerializedName("X") val event_stamp: Long,
    @SerializedName("T") val temp: Int,
    @SerializedName("V") val vcc: Int,
    @SerializedName("H") val hum: Int,
    @SerializedName("HD") val dhum: Int
) {
    val isValid : Boolean
        get() = temp > -500 && temp < 500
}