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

fun List<DbSensor>.asDomainModel(): List<Sensor> {
    return map {
        Sensor (
            id = it.id,
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
    val sensor_id: Int,
    val timestamp: Long,
    val temp: Int,
    val vcc: Int,
    val hum: Int,
    val dhum: Int
)

/*
fun List<DbSensorData>.asDomainModel(): List<SensorData> {
    return map {
        SensorData (
            sensor_id = it.sensor_id.toShort(),
            timestamp = it.timestamp.toLong()
        )
    }
}
*/
