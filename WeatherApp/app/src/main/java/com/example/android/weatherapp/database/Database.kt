package com.example.android.weatherapp.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

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

val MIGRATION_3_4: Migration = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE INDEX idx_sensor_id ON dbsensordata (sensor_id);"+
                "CREATE INDEX idx_timestamp ON dbsensordata (timestamp);"
        )
    }
}


@Dao
interface WeatherDao {
    @Query("select * from dbsensor")
    fun getSensors(): LiveData<List<DbSensor>>
    @Insert
    fun insert(sensor : DbSensor)
    @Update
    fun update(sensor : DbSensor)

    @Insert
    fun insert_data(sensor_data : DbSensorData)
    //@Insert(onConflict = OnConflictStrategy.REPLACE)
    //fun insertAll(vararg videos: DatabaseVideo)

    @Query("select * from dbsensordata")
    fun getSensorsData(): LiveData<List<DbSensorData>>

    @Query("select * from dbsensordata where sensor_id = :id LIMIT 1")
    //fun getOneSensorData(id : Int): LiveData<List<DbSensorData>>
    fun getOneSensorData(id : Int): DbSensorData

//    @Query("""
//        select * from dbsensor left outer join dbsensordata on (dbsensor.id=dbsensordata.sensor_id) where
//          dbsensordata._id in
//          (select _id from dbsensordata sd where dbsensor.id=sd.sensor_id order by timestamp desc limit 1)
//          """
//    )
    @Query("""
        select * from dbsensor left outer join dbsensordata on (
        dbsensor.id=dbsensordata.sensor_id and dbsensordata._id in 
          (select _id from dbsensordata sd where dbsensor.id=sd.sensor_id order by timestamp desc limit 1) 
          ) 
          """
    )
    fun getSensorsWithData(): LiveData<List<DbSensorWithData>>
    // todo - filter validated only

    @Query("DELETE FROM dbsensordata where sensor_id=:id")
    fun deleteSensorData(id : Int)

    @Query("select count(*) from dbsensordata")
    fun getSensorDataCount(): Int

}

@Database(entities = [DbSensor::class, DbSensorData::class], version = 4)
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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
        }
    }
    return INSTANCE
}

