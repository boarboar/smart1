package com.example.android.weatherapp.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface WeatherDao {
    @Query("select * from dbsensor")
    fun getSensors(): LiveData<List<DbSensor>>
    @Insert
    fun insert(night : DbSensor)
    //@Insert(onConflict = OnConflictStrategy.REPLACE)
    //fun insertAll(vararg videos: DatabaseVideo)
}

@Database(entities = [DbSensor::class], version = 1)
abstract class WeatherDatabase : RoomDatabase() {
    abstract val weatherDao: WeatherDao
}

private lateinit var INSTANCE: WeatherDatabase

fun getDatabase(context: Context): WeatherDatabase {
    synchronized(WeatherDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                WeatherDatabase::class.java,
                "weather").build()
        }
    }
    return INSTANCE
}

