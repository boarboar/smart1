package com.example.android.weatherapp.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.Transformations.map
import androidx.room.Embedded
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


// combine

@Entity
data class DbSensorWithData(
    @PrimaryKey
    val id: Int,
    val description: String,
    val updated: Long,
    @Embedded
    val sdata: DbSensorData
)

fun List<DbSensorWithData>.asSensor(): List<Sensor> {
    return map {
        Sensor (
            id = it.id.toShort(),
            description = it.description,
            updated = it.updated,
            data = it.sdata
        )
    }
}
