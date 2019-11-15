package com.example.android.weatherapp.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import android.icu.lang.UCharacter.GraphemeClusterBreak.V

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE  dbsensor ADD COLUMN updated INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE 'dbsensordata' ( " +
                "'_id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"+
                "'sensor_id' INTEGER NOT NULL,"+
                "'timestamp' INTEGER NOT NULL,"+
                "'temp' INTEGER NOT NULL,"+
                "'vcc' INTEGER NOT NULL,"+
                "'hum' INTEGER NOT NULL,"+
                "'dhum' INTEGER NOT NULL)")
    }
}

@Dao
interface WeatherDao {
    @Query("select * from dbsensor")
    fun getSensors(): LiveData<List<DbSensor>>
    @Insert
    fun insert(sensor : DbSensor)
    @Insert
    fun insert_data(sensor_data : DbSensorData)
    //@Insert(onConflict = OnConflictStrategy.REPLACE)
    //fun insertAll(vararg videos: DatabaseVideo)
}

@Database(entities = [DbSensor::class, DbSensorData::class], version = 3)
abstract class WeatherDatabase : RoomDatabase() {
    abstract val weatherDao: WeatherDao
}

private lateinit var INSTANCE: WeatherDatabase

fun getDatabase(context: Context): WeatherDatabase {
    synchronized(WeatherDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                WeatherDatabase::class.java,
                "weather")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
        }
    }
    return INSTANCE
}

