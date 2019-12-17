package com.example.android.weatherapp.database

import androidx.room.ColumnInfo
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
    @ColumnInfo(index = true)
    val sensor_id: Int, // should be indexed
    @ColumnInfo(index = true)
    val timestamp: Long,
    val temp: Int,
    val vcc: Int,
    val hum: Int,
    val dhum: Int
)
{
    fun toSensorData() = SensorData(sensor_id.toShort(), timestamp, temp.toShort(), vcc.toShort(), hum.toShort(), dhum.toShort())
}
// todo - add validated field


// combine

@Entity
data class DbSensorWithData(
    @Embedded
    val s: DbSensor?,
    @Embedded
    val sdata: DbSensorData?
)
{
    fun toSensor() = Sensor(s?.id?.toShort() ?: 0,
        description = s?.description ?: "",
        updated = s?.updated ?: 0,
        data = sdata?.toSensorData() //?: SensorData(0,0,0,0,0,0)
     )
}

fun List<DbSensorWithData>.asSensor(): List<Sensor> {
    return map { it.toSensor() }
}

fun List<DbSensorData>.asSensorData(): List<SensorData> {
    return map { it.toSensorData() }
}

data class DbDataStat(
    @ColumnInfo(name = "d_count") val count : Int,
    @ColumnInfo(name = "d_from") val from : Long,
    @ColumnInfo(name = "d_to") val to : Long
)
