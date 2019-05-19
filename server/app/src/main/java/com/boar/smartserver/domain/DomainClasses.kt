package com.boar.smartserver.domain

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.item_sensor_log.view.*
import java.util.*

fun Random.nextInt(range: IntRange): Int {
    return range.start + nextInt(range.last - range.start)
}

class SensorList : ArrayList<Sensor>() {

    companion object {
        private val tag = "Sensor list"
        //private val MEAS_OBOLETE_PERIOD = 1000L * 60L * 46L // 46 min

        fun simulate() : String {
            val random = Random()
            val maybeerror = random.nextBoolean()
            val id = random.nextInt(1..4)
            val temp10 = if(maybeerror) random.nextInt(-1200..1200)
                else random.nextInt(-400..400)
            val vcc1000 = random.nextInt(2540..4950)
            val h10 = random.nextInt(10..1000)
            val hd = random.nextInt(1..3)

            return if(id==3)
                "{\"I\":$id,\"T\":$temp10,\"V\":$vcc1000,\"H\":$h10,\"HD\":$hd,\"Y\":37}"
                else
                "{\"I\":$id,\"M\":64,\"P\":0,\"R\":8,\"T\":$temp10,\"V\":$vcc1000,\"Y\":37}"
        }
    }

    private val gson = Gson()

    fun measFromJson(text: String) : SensorMeasurement? {
        try {
            val newmeas = gson.fromJson(text, SensorMeasurement::class.java)
            //val valid = newmeas.temp10.toInt() != -1270
            val valid = newmeas.temp10 > -500 && newmeas.temp10 < 500  // -50..50
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
            set(sensorIdx, this[sensorIdx].copy(lastValidMeasTime = System.currentTimeMillis(), outdated=false))
            this[sensorIdx].pushHist(newmeas)
        }
        else {
            //Log.w(tag, "Bad measurement: $newmeas")
            this[sensorIdx].invalidateLastMeasurement(newmeas.updated)
        }
        return sensorIdx
    }

    fun checkForOutdated() : ArrayList<Int> {
        var idxs = arrayListOf<Int>()
        forEachIndexed { idx, it ->
            /*
            if(!it.outdated && (it.lastValidMeasTime < System.currentTimeMillis()-MEAS_OBOLETE_PERIOD)) {
                it.outdated = true
                idxs.add(idx)
            }
            */
            if(it.checkForOutdated()) idxs.add(idx)
        }
        return idxs
    }
}


data class Sensor(val id: Int, val description: String,
                  val lastValidMeasTime: Long = 0,
                  val hist: ArrayList<SensorMeasurement> = arrayListOf(),
                  var outdated : Boolean = false,
                  var obsolete : Boolean = false

) {
    companion object {
        const val VCC_LOW_1000 = 3500
        const val VCC_LOW_DHT11_1000 = 2900
        const val MEAS_OUTDATED_PERIOD = 1000L * 60L * 46L // 46 min
    }

    fun validate() : Boolean = id>0 && description.isNotEmpty()

    fun checkForOutdated() : Boolean {
        if(!outdated && (lastValidMeasTime < System.currentTimeMillis()-MEAS_OUTDATED_PERIOD)) {
            outdated = true
            return true
        } else return false
    }

    val measValidated : Boolean
        get() = if(hist.size>0) hist[0].validated else true
    val measUpdatedTime : Long
        get() = if(hist.size>0) hist[0].updated else 0L

    val resolution : Int
        get() = if(hist.size>0) hist[0].resolution else -1
    val model : Int
        get() = if(hist.size>0) hist[0].model else -1
    val parasite : Int
        get() = if(hist.size>0) hist[0].parasite else -1

    val temp_grad : Int
        get() = if(hist.size>1) {
            val aver = hist.map{it.temp10}.average().toInt()
            hist[0].temp10 - aver
        } else 0

    val temperatureAsString : String
        get() = if(hist.size>0) "${hist[0].temp10.toFloat()/10f}" else "--.-"

    val humidityAsString : String
        get() = if(hist.size>0 && hist[0].hum10>0 && hist[0].vcc1000 > VCC_LOW_DHT11_1000) "${hist[0].hum10.toFloat()/10f} %" else ""

    val humidityDig : Int
        get() = if(hist.size>0) hist[0].hd else 0


    val vccAsString : String
        get() = if(hist.size>0) "${(hist[0].vcc1000/10).toFloat()/100f}" else "-.--"

    val isVccLow : Boolean
        get() = hist.size>0 && hist[0].vcc1000 < VCC_LOW_1000

    val msg : String
        get() = if(hist.size>0) hist[0].msg else "none"

    fun pushHist(meas : SensorMeasurement)  {
        hist.add(0, meas)
        if(hist.size>4)
            hist.removeAt(hist.size-1)
    }

    fun pushHistTemp(h : SensorHistory)  {
        pushHist(
                SensorMeasurement(h.sensorId, 0, h.vcc1000,0,0,0,
                        h.temp10,
                        h.h10, h.hd,
                        h.timestamp, true)
        )
    }

    fun invalidateLastMeasurement(timestamp : Long) {
        if(hist.size>0) {
            val updmeas = hist[0].copy(validated = false, updated = timestamp)
            hist.set(0, updmeas)
        }
    }
}


// '{"I":"1","M":64,"P":0,"R":8,"T":210,"V":310}'

data class SensorMeasurement(
        @SerializedName("I") val id: Int,
        @SerializedName("Y") val ver: Int,
        @SerializedName("V") val vcc1000: Int,

        @SerializedName("M") val model: Int = -1,
        @SerializedName("P") val parasite: Int = -1,
        @SerializedName("R") val resolution: Int = -1,
        @SerializedName("T") val temp10: Int = -1271,
        @SerializedName("H") val hum10: Int = -1,
        @SerializedName("HD") val hd: Int = -1,

        val updated: Long=System.currentTimeMillis(),
        val validated: Boolean = false,
        val msg: String = ""
) {
    enum class DHUM_VALS(val value: Int) {
        NOTSET(0), LEAK(1), NORM(2)
    }
}

data class SensorHistory(val sensorId: Int, val temp10: Int, val vcc1000: Int,
                         val h10: Int, val hd: Int,
                         val id: Long = 0, val timestamp: Long = System.currentTimeMillis()) {


    val temperature: Float
        get() = temp10.toFloat() / 10f
    val vcc : Float
        get() = (vcc1000/10).toFloat()/100f
    val humidity: Float
        get() = h10.toFloat() / 10f

    val humidityAsString : String
        get() = if(h10>0) "${h10.toFloat()/10f} %" else "-"

    val hdAsString : String
        get() = if(hd>0) "$hd" else "-"
}

data class ServiceLog(val message: String, val id: Long = 0, val timestamp: Long = System.currentTimeMillis())


