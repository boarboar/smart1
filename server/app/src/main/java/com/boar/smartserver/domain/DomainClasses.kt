package com.boar.smartserver.domain

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.util.*

fun Random.nextInt(range: IntRange): Int {
    return range.start + nextInt(range.last - range.start)
}

class SensorList : ArrayList<Sensor>() {

    companion object {
        private val tag = "Sensor list"

        fun simulate() : String {
            val random = Random()
            val id = random.nextInt(1..3).toShort()
            val temp10 = random.nextInt(-25..35).toShort()
            val vcc1000 = random.nextInt(2540..4950).toShort()

            return "{\"I\":$id,\"M\":64,\"P\":0,\"R\":8,\"T\":$temp10,\"V\":$vcc1000,\"Y\":37}"
        }
    }

    private val gson = Gson()

    fun measFromJson(text: String) : SensorMeasurement? {
        try {
            val newmeas = gson.fromJson(text, SensorMeasurement::class.java)
            val valid = newmeas.temp10.toInt() != -1270
            return newmeas.copy(updated=System.currentTimeMillis(), validated=valid, msg = text)
        } catch (t: Throwable) {
            Log.w(tag, "Json error: ${t.message}")
            return null
        }
    }

    fun edit(pos: Int, newsens: Sensor) : Int {
        val sensorIdx = indexOfFirst {it.id == newsens.id}
        if(sensorIdx==-1 || pos!=sensorIdx) {
            Log.w(tag, "Bad ID: $newsens.id")
            return -1
        }
        set(sensorIdx, newsens)
        return sensorIdx
    }

    fun update(newmeas: SensorMeasurement) : Int {
        val sensorIdx = indexOfFirst {it.id == newmeas.id}
        if(sensorIdx==-1) return -1
        if(newmeas.validated) {
            val meas_prev_v = this[sensorIdx].meas
            set(sensorIdx, this[sensorIdx].copy(meas = newmeas, meas_prev=meas_prev_v,
                    lastValidMeasTime = System.currentTimeMillis()))
        }
        else {
            //Log.w(tag, "Bad measurement: $newmeas")
            this[sensorIdx].meas?.let  {
                val updmeas = it.copy(validated = false, updated = newmeas.updated)
                set(sensorIdx, this[sensorIdx].copy(meas = updmeas))
            }
        }
        return sensorIdx
    }

    /*
    fun update(text: String) : Int {

        lateinit var newmeas : SensorMeasurement
        try {
            newmeas = gson.fromJson(text, SensorMeasurement::class.java)
        } catch (t: Throwable) {
            Log.w(tag, "Json error: ${t.message}")
            return -1
        }

        val valid = newmeas.temp10.toInt() != -1270

        return update(newmeas.copy(updated=System.currentTimeMillis(), validated=valid))

    }
*/

    /*
    fun simulate() : Int {
        val random = Random()
        val id = random.nextInt(1..3).toShort()
        val temp10 = random.nextInt(-25..35).toShort()
        val vcc1000 = random.nextInt(2540..4950).toShort()

        return update("{\"I\":$id,\"M\":64,\"P\":0,\"R\":8,\"T\":$temp10,\"V\":$vcc1000}")
    }
    */

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
                  val lastValidMeasTime: Long = 0,
                  val meas: SensorMeasurement? = null,
                  val meas_prev: SensorMeasurement? = null
        //, val measList
) {
    val temperature : Float
        get() = if(meas!=null) meas.temp10.toFloat()/10f else 0f
    val vcc : Float
        get() = if(meas!=null) (meas.vcc1000/10).toFloat()/100f else 0f
    val resolution : Short
        get() = if(meas!=null) meas.resolution else 0
    val model : Short
        get() = if(meas!=null) meas.model else 0
    val parasite : Short
        get() = if(meas!=null) meas.parasite else -1
    val updated : Long
        get() = meas?.updated ?: 0L
    val validated : Boolean
        get() = meas?.validated ?: true
    val temp_grad : Int
        get() = if(meas!=null && meas_prev!=null) (meas.temp10 - meas_prev.temp10) else 0

    val temperatureAsString : String
        get() = if(meas!=null) "${meas.temp10.toFloat()/10f}" else "--.-"
    val vccAsString : String
        get() = if(meas!=null) "${(meas.vcc1000/10).toFloat()/100f}" else "-.--"

    val msg : String
        get() = meas?.msg ?: "none"


    fun validate() : Boolean = id>0 && description.isNotEmpty()

}

//b?.length ?: -1

// '{"I":"1","M":64,"P":0,"R":8,"T":210,"V":310}'

data class SensorMeasurement(
        @SerializedName("I") val id: Short,
        @SerializedName("M") val model: Short = 0,
        @SerializedName("P") val parasite: Short = 0,
        @SerializedName("R") val resolution: Short = 0,
        @SerializedName("T") val temp10: Short,
        @SerializedName("V") val vcc1000: Short,
        val updated: Long=System.currentTimeMillis(),
        val validated: Boolean = false,
        val msg: String = ""
)
