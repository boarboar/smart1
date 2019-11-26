package com.example.android.weatherapp.database

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
{
    fun toSensorData() = SensorData(sensor_id.toShort(), timestamp, temp.toShort(), vcc.toShort(), hum.toShort(), dhum.toShort())
}


// combine

@Entity
data class DbSensorWithData(
    @PrimaryKey
    val id: Int,
    val description: String,
    val updated: Long,
    @Embedded
    val sdata: DbSensorData?
)
{
    fun toSensor() = Sensor(id.toShort(),
        description = description,
        updated = updated,
        data = sdata?.toSensorData() //?: SensorData(0,0,0,0,0,0)
     )
}

fun List<DbSensorWithData>.asSensor(): List<Sensor> {
    return map { it.toSensor() }
}
