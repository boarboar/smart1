package com.boar.smartserver.db

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.boar.smartserver.domain.Sensor
import com.boar.smartserver.domain.SensorList
import org.jetbrains.anko.db.*
import java.util.HashMap

class SensorDb(private val dbHelper: DbHelper = DbHelper.instance
    //                 private val dataMapper: DbDataMapper = DbDataMapper()
)   {

    companion object {
        private const val tag = "SNS DB"
        private val parser = rowParser { id: Int, description: String ->
            Sensor(id.toShort(), description)
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
                sensors = select(SensorTable.NAME, SensorTable.ID, SensorTable.DESCRIPTION).parseList(parser)
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


}