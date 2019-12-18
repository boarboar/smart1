package com.example.android.weatherapp.domain

import android.os.Parcelable
import com.example.android.weatherapp.utils.DateUtils
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Sensor(val id: Int, val description: String, val updated: Long=0,
                  val data : SensorData? = null
) : Parcelable {

    companion object {
        const val VCC_LOW_1000 = 3500
        const val VCC_LOW_DHT11_1000 = 2900
        const val MEAS_OUTDATED_PERIOD = 1000L * 60L * 46L // 46 min
        const val MEAS_OBSOLETE_PERIOD = 1000L * 60L * 60L * 4L// 4hrs
    }

    val at : String
        get() = if(updated>0) DateUtils.convertDateTime(updated) else "---"

//    val updatedAt : String
//        get() = data?.timestamp?.let {DateUtils.convertDateTime(it)} ?: "---"
//    val tempString : String
//        get() = data?.temp?.let {(it/10.0F).toString()+"º"} ?: "---"
//    val vccString : String
//        get() = data?.vcc?.let {((it/10)/100.0F).toString()+"v"} ?: "---"
//    val humString : String
//        get() = data?.hum?.let {if(it>0) (it/10.0F).toString()+"%" else ""} ?: ""
//    val dhumString : String
//        get() = data?.dhum?.let {if(it.toInt()==SensorData.DHUM_VALS.LEAK.value) "!" else ""} ?: ""
//    val dhumVal : String
//        get() = data?.dhum?.let {it.toString()} ?: ""
//    val isVccLow : Boolean
//        get () = data?.vcc?.let { it< VCC_LOW_1000 } ?: false
//
    val updatedAt : String
        get() = data?.let {it.at} ?: "---"
    val tempString : String
        get() = data?.let {it.tempString} ?: "---"
    val vccString : String
        get() = data?.let {it.vccString} ?: "---"
    val humString : String
        get() = data?.let {it.humString} ?: ""
    val dhumString : String
        get() = data?.let {it.dhumString} ?: ""
    val dhumVal : String
        get() = data?.let {it.dhumVal} ?: ""
    val isVccLow : Boolean
        get () = data?.let { it.isVccLow } ?: false
    val idString : String
        get() = id.toString()

    //fun equalData(other : Sensor) = id==other.id && description==other.description && updated==other.updated
  }

@Parcelize
data class SensorData(
    val sensor_id: Int,
    val timestamp: Long,
    val temp: Short,
    val vcc: Short,
    val hum: Short,
    val dhum: Short
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
    val isVccLow : Boolean
        get () = vcc< Sensor.VCC_LOW_1000

    val asString : String
        get() = "$temp $vcc" // for test
}