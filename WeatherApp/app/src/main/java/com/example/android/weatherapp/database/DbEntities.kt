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
    fun toSensorData() = SensorData(sensor_id, timestamp, temp.toShort(), vcc.toShort(), hum.toShort(), dhum.toShort())
}
// todo - add validated field

@Entity
data class DbSensorLatestData(
    @PrimaryKey
    val sensor_id: Int, // should be indexed
    val timestamp: Long,
    val temp: Int,
    val vcc: Int,
    val hum: Int,
    val dhum: Int,
    val d_temp: Int,
    val d_vcc: Int,
    val d_hum: Int,
    val d_dhum: Int
)
{
    constructor(data: DbSensorData ) :
            this(data.sensor_id, data.timestamp, data.temp, data.vcc, data.hum, data.dhum,
                0,0,0,0)
    constructor(latest: DbSensorLatestData, data: DbSensorData ) :
            this(data.sensor_id, data.timestamp, data.temp, data.vcc, data.hum, data.dhum,
                data.temp-latest.temp,data.vcc-latest.vcc,
                data.hum-latest.hum, data.dhum-latest.dhum)
}

// combine

@Entity
data class DbSensorWithData(
    @Embedded
    val s: DbSensor?,
    @Embedded
    val sdata: DbSensorData?
)
{
    fun toSensor() = Sensor(s?.id ?: 0,
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
