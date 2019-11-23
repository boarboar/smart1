package com.example.android.weatherapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.android.weatherapp.domain.Sensor
import com.example.android.weatherapp.domain.SensorData

@Entity
data class DbSensor(
    @PrimaryKey
    val id: Int,
    val description: String,
    val updated: Long
    )

fun List<DbSensor>.asSensor(): List<Sensor> {
    return map {
        Sensor (
            id = it.id.toShort(),
            description = it.description,
            updated = it.updated
        )
    }
    // couple with sensordata here
}

@Entity
data class DbSensorData(
    @PrimaryKey(autoGenerate = true)
    val _id: Int,
    val sensor_id: Int, // should be indexed
    val timestamp: Long,
    val temp: Int,
    val vcc: Int,
    val hum: Int,
    val dhum: Int
)


fun List<DbSensorData>.asSensorData(): List<SensorData> {
    return map {
        SensorData (
            sensor_id = it.sensor_id.toShort(),
            timestamp = it.timestamp.toLong(),
            temp = it.temp.toShort(),
            vcc = it.vcc.toShort(),
            hum = it.hum.toShort(),
            dhum = it.dhum.toShort()
        )
    }
}

fun List<DbSensor>.asSensorWithData(datas : List<DbSensorData>?): List<Sensor> {
    return map {
        val sid = it.id
        val sens_data = datas?.find { it.sensor_id == sid }
        Sensor (
            id = it.id.toShort(),
            description = it.description,
            updated = it.updated,
            data = sens_data?.let {
                SensorData(
                    sensor_id = sens_data.sensor_id.toShort(),
                    timestamp = sens_data.timestamp.toLong(),
                    temp = sens_data?.temp.toShort(),
                    vcc = sens_data.vcc.toShort(),
                    hum = sens_data.hum.toShort(),
                    dhum = sens_data.dhum.toShort()
                )
            }
        )
    }
    // couple with sensordata here
}