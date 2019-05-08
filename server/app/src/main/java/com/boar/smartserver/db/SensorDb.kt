package com.boar.smartserver.db

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.boar.smartserver.domain.Sensor
import com.boar.smartserver.domain.SensorHistory
import com.boar.smartserver.domain.SensorList
import com.boar.smartserver.domain.ServiceLog
import org.jetbrains.anko.db.*
import java.util.HashMap

class SensorDb(private val dbHelper: DbHelper = DbHelper.instance
    //                 private val dataMapper: DbDataMapper = DbDataMapper()
)   {

    companion object {
        private const val tag = "SNS DB"
        private val parserSensor = rowParser { id: Int, description: String ->
            Sensor(id, description)
        }
        private val parserLog = rowParser { id: Long, timestamp: Long, msg: String ->
            ServiceLog(msg, id, timestamp)
        }
        private val parserSensorHistory = rowParser { id: Long, timestamp: Long,
                                                      sensorId : Int, t : Int, v: Int,
                                                      h: Int, hd: Int ->
            SensorHistory(sensorId, t, v, h, hd, id, timestamp)
        }
        private val parserLogMinMax = rowParser { idmax: Long, idmin: Long ->
            Pair(idmax, idmin)
        }
    }

    /*
    class MyRowParser : RowParser<Pair<Int, String>> {
        override fun parseRow(columns: Array<Any?>): Pair<Int, String> {
            return Pair(columns[0] as Int, columns[1] as String)
        }
    }
    */

    fun requestSensors() : SensorList {
        lateinit var sensors : List<Sensor>
        //lateinit var sensors : SensorList
        dbHelper.use {
            // what if error ?
            try {
                sensors = select(SensorTable.NAME, SensorTable.ID, SensorTable.DESCRIPTION)
                        .parseList(parserSensor)
            }
            catch (t: Throwable) {
                val msg = t.message ?: "Unknown DB error"
                Log.w(tag, "DB error: $msg")
                sensors = listOf()
            }
        }
        val slist = SensorList()
        for (s in sensors) slist.add(s)
        return slist
    }

    fun saveSensor(sensor : Sensor) : Pair<Boolean, String> {
        var result : Boolean = false
        var msg = ""
        dbHelper.use {
            try {
            insertOrThrow(SensorTable.NAME, SensorTable.ID to  sensor.id,
                    SensorTable.DESCRIPTION to sensor.description)
                result = true
            }

            catch (t: SQLiteConstraintException) {
                msg = t.message ?: "Unknown DB error"
                Log.w(tag, "Constraint error: $msg")
            }
            catch (t: Throwable) {
                msg = t.message ?: "Unknown DB error"
                Log.w(tag, "DB error: $msg")
            }
        }
        return Pair(result, msg)
    }

    fun updateSensor(sensor : Sensor) : Pair<Boolean, String> {
        var result : Boolean = false
        var msg = ""
        dbHelper.use {
            try {
                replaceOrThrow(SensorTable.NAME, SensorTable.ID to  sensor.id,
                        SensorTable.DESCRIPTION to sensor.description)
                result = true
            }

            catch (t: SQLiteConstraintException) {
                msg = t.message ?: "Unknown DB error"
                Log.w(tag, "Constraint error: $msg")
            }
            catch (t: Throwable) {
                msg = t.message ?: "Unknown DB error"
                Log.w(tag, "DB error: $msg")
            }
        }
        return Pair(result, msg)
    }

    fun deleteSensor(sensor : Sensor) : Pair<Boolean, String> {
        var result : Boolean = false
        var msg = ""
        dbHelper.use {
            try {
                delete(SensorTable.NAME, "${SensorTable.ID} = {sensorID}","sensorID" to  sensor.id)
                result = true
            }

            catch (t: SQLiteConstraintException) {
                msg = t.message ?: "Unknown DB error"
                Log.w(tag, "Constraint error: $msg")
            }
            catch (t: Throwable) {
                msg = t.message ?: "Unknown DB error"
                Log.w(tag, "DB error: $msg")
            }
        }
        return Pair(result, msg)
    }

    fun saveLog(event : ServiceLog) : Pair<Boolean, String> {
        var result = false
        var errmsg = ""
        dbHelper.use {
            try {
                insertOrThrow(LogTable.NAME, LogTable.TIMESTAMP to  event.timestamp,
                        LogTable.MSG to event.message)
                result = true
            }

            catch (t: SQLiteConstraintException) {
                errmsg = t.message ?: "Unknown DB error"
                Log.w(tag, "Constraint error: $errmsg")
            }
            catch (t: Throwable) {
                errmsg = t.message ?: "Unknown DB error"
                Log.w(tag, "DB error: $errmsg")
            }
        }
        return Pair(result, errmsg)
    }

    fun requestLog(maxLogRec : Int) : MutableList<ServiceLog> {
        lateinit var logs : MutableList<ServiceLog>
        dbHelper.use {
            // what if error ?
            try {
                logs = select(LogTable.NAME, LogTable.ID, LogTable.TIMESTAMP, LogTable.MSG)
                        .orderBy(LogTable.ID, SqlOrderDirection.DESC).limit(maxLogRec)
                        .parseList(parserLog).toMutableList()
            }
            catch (t: Throwable) {
                val msg = t.message ?: "Unknown DB error"
                Log.w(tag, "DB error: $msg")
                logs = mutableListOf()
            }
        }
        return logs
    }

    fun cleanLog(maxLogRec : Int) : Int {
        var cln = 0
        dbHelper.use {
            try {
                val (lmax, lmin) = select(LogTable.NAME, "max(${LogTable.ID})", "min(${LogTable.ID})")
                        .parseSingle(parserLogMinMax)
                Log.w(tag, "Logstat: $lmax, $lmin")

                if(lmax - lmin > maxLogRec) {
                    val truncid = lmax - maxLogRec
                    cln = (truncid - lmin).toInt()
                    delete(LogTable.NAME, "${LogTable.ID} < {truncID}","truncID" to  truncid)
                }
                else {}

            }
            catch (t: Throwable) {
                val msg = t.message ?: "Unknown DB error"
                Log.w(tag, "DB error: $msg")
            }
        }
        return cln
    }

    fun saveSensorHist(hist : SensorHistory) : Pair<Boolean, String> {
        var result = false
        var errmsg = ""
        dbHelper.use {
            try {
                insertOrThrow(SensorHistoryTable.NAME,
                        SensorHistoryTable.TIMESTAMP to  hist.timestamp,
                        SensorHistoryTable.SENSOR_ID to hist.sensorId,
                        SensorHistoryTable.TEMPERATURE to hist.temp10,
                        SensorHistoryTable.VCC to hist.vcc1000,
                        SensorHistoryTable.HUMIDITY to hist.h10,
                        SensorHistoryTable.DHUMIDITY to hist.hd
                        )
                result = true
            }

            catch (t: SQLiteConstraintException) {
                errmsg = t.message ?: "Unknown DB error"
                Log.w(tag, "Constraint error: $errmsg")
            }
            catch (t: Throwable) {
                errmsg = t.message ?: "Unknown DB error"
                Log.w(tag, "DB error: $errmsg")
            }
        }
        return Pair(result, errmsg)
    }

    fun requestSensorHist(sensorId: Int, maxLogRec : Int) : List<SensorHistory> {
        lateinit var hist : List<SensorHistory>
        dbHelper.use {
            // what if error ?
            try {
                hist = select(SensorHistoryTable.NAME, SensorHistoryTable.ID,
                        SensorHistoryTable.TIMESTAMP, SensorHistoryTable.SENSOR_ID,
                        SensorHistoryTable.TEMPERATURE, SensorHistoryTable.VCC,
                        SensorHistoryTable.HUMIDITY, SensorHistoryTable.DHUMIDITY
                        )
                        .whereArgs("${SensorHistoryTable.SENSOR_ID} = {sensorID}","sensorID" to  sensorId)
                        .orderBy(SensorHistoryTable.ID, SqlOrderDirection.DESC).limit(maxLogRec)
                        .parseList(parserSensorHistory)
            }
            catch (t: Throwable) {
                val msg = t.message ?: "Unknown DB error"
                Log.w(tag, "DB error: $msg")
                hist = listOf()
            }
        }
        return hist
    }

    fun cleanSensorHist(maxLogRec : Int) : Int {
        var cln = 0
        dbHelper.use {
            try {
                val (lmax, lmin) = select(SensorHistoryTable.NAME
                        , "max(${SensorHistoryTable.ID})", "min(${SensorHistoryTable.ID})")
                        .parseSingle(parserLogMinMax)
                Log.w(tag, "Histtat: $lmax, $lmin")

                if(lmax - lmin > maxLogRec) {
                    val truncid = lmax - maxLogRec
                    cln = (truncid - lmin).toInt()
                    delete(SensorHistoryTable.NAME,
                            "${SensorHistoryTable.ID} < {truncID}","truncID" to  truncid)
                }
                else {}

            }
            catch (t: Throwable) {
                val msg = t.message ?: "Unknown DB error"
                Log.w(tag, "DB error: $msg")
            }
        }
        return cln
    }

    fun getSensorHistSize() : Int {
        var cnt = 0
        dbHelper.use {
            try {
                cnt = select(SensorHistoryTable.NAME, "count()")
                        .parseSingle(IntParser)
            }
            catch (t: Throwable) {
                val msg = t.message ?: "Unknown DB error"
                Log.w(tag, "DB error: $msg")
            }
        }
        return cnt
    }

    fun setLatestSensorHist( sensors: SensorList) {
        lateinit var hist : List<SensorHistory>
        dbHelper.use {
            // what if error ?
            try {
                hist = select(SensorHistoryTable.NAME, SensorHistoryTable.ID,
                        SensorHistoryTable.TIMESTAMP, SensorHistoryTable.SENSOR_ID,
                        SensorHistoryTable.TEMPERATURE, SensorHistoryTable.VCC,
                        SensorHistoryTable.HUMIDITY, SensorHistoryTable.DHUMIDITY)
                        .orderBy(SensorHistoryTable.ID, SqlOrderDirection.DESC).limit(sensors.size*4)
                        .parseList(parserSensorHistory)
            }
            catch (t: Throwable) {
                val msg = t.message ?: "Unknown DB error"
                Log.w(tag, "DB error: $msg")
                hist = listOf()
            }
        }

        /*
        val latest  = mutableMapOf<Int, SensorHistory>()

        hist.forEach {
            if(!(it.sensorId in latest)) latest[it.sensorId] = it
        }

        return latest.values.toList()
        */
        for(sensor in sensors) {
            val l = hist.find { it.sensorId == sensor.id.toInt() }
            l?.let {
                sensor.pushHistTemp(it)
                sensor.outdated = true
            }
        }
    }
}