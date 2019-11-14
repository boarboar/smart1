package com.example.android.weatherapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.android.weatherapp.domain.Sensor

@Entity
data class DbSensor(
    @PrimaryKey
    val id: Int,
    val description: String
    )

fun List<DbSensor>.asDomainModel(): List<Sensor> {
    return map {
        Sensor (
            id = it.id,
            description = it.description
        )
    }
}