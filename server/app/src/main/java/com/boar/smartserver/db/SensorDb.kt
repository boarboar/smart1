package com.boar.smartserver.db

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.boar.smartserver.domain.Sensor
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
            Sensor(id.toShort(), description)
        }
        private val parserLog = rowParser { id: Long, timestamp: Long, msg: String ->
            ServiceLog(msg, id, timestamp)
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
                sensors = select(SensorTable.NAME, SensorTable.ID, SensorTable.DESCRIPTION).parseList(parserSensor)
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

    fun requestLog() : MutableList<ServiceLog> {
        lateinit var logs : MutableList<ServiceLog>
        dbHelper.use {
            // what if error ?
            try {
                logs = select(LogTable.NAME, LogTable.ID, LogTable.TIMESTAMP, LogTable.MSG).parseList(parserLog).toMutableList()
            }
            catch (t: Throwable) {
                val msg = t.message ?: "Unknown DB error"
                Log.w(tag, "DB error: $msg")
                logs = mutableListOf()
            }
        }
        return logs
    }
}