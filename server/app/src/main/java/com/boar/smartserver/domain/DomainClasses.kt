package com.boar.smartserver.domain

import com.google.gson.annotations.SerializedName
import java.util.ArrayList

class SensorList : ArrayList<Sensor>() {
    fun update(newmeas: SensorMeasurement) : Int {
        val sensorIdx = indexOfFirst {it.id == newmeas.id}
        if(sensorIdx==-1) return -1
        set(sensorIdx, this[sensorIdx].copy(meas=newmeas))
        return sensorIdx
    }
    /*
    simulate
    SensorMeasurement(id=1, temp10=215, vcc100=312)
    SensorMeasurement(id=2, temp10=225, vcc100=299)
    */
}


/*
data class Sensor(val id: Short, val description: String,
                  val updated: Long,  val temperature: Float, val vcc: Float
                  //, val measList
                  )
*/

data class Sensor(val id: Short, val description: String,
                  val meas: SensorMeasurement? = null
        //, val measList
) {
    val temperature : Float
        get() = if(meas!=null) meas.temp10.toFloat()/10f else 0f
    val vcc : Float
        get() = if(meas!=null) meas.vcc100.toFloat()/100f else 0f
    val updated : Long
        get() = meas?.updated ?: 0L


    val temperatureAsString : String
        get() = if(meas!=null) "${meas.temp10.toFloat()/10f}" else "--.-"
    val vccAsString : String
        get() = if(meas!=null) "${meas.vcc100.toFloat()/100f}" else "-.--"

}

//b?.length ?: -1

// '{"I":"1","M":64,"P":0,"R":8,"T":210,"V":310}'

data class SensorMeasurement(
        @SerializedName("I") val id: Short,
        @SerializedName("M") val model: Short = 0,
        @SerializedName("P") val isParasite: Short = 0,
        @SerializedName("R") val resolution: Short = 0,
        @SerializedName("T") val temp10: Short,
        @SerializedName("V") val vcc100: Short,
        val updated: Long=System.currentTimeMillis()
)
