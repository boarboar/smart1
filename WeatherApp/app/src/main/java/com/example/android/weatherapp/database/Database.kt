package com.example.android.weatherapp.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

/*
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

val MIGRATION_4_5: Migration = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE 'dbsensorlatestdata' ( " +
                "'_id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"+
                "'sensor_id' INTEGER NOT NULL,"+
                "'timestamp' INTEGER NOT NULL,"+
                "'temp' INTEGER NOT NULL,"+
                "'vcc' INTEGER NOT NULL,"+
                "'hum' INTEGER NOT NULL,"+
                "'dhum' INTEGER NOT NULL,"+
                "'d_temp' INTEGER NOT NULL,"+
                "'d_vcc' INTEGER NOT NULL,"+
                "'d_hum' INTEGER NOT NULL,"+
                "'d_ddhum' INTEGER NOT NULL"+
                ")")
        database.execSQL("CREATE INDEX idx_sensor_latest_id ON dbsensorlatestdata (sensor_id);")

    }
}

val MIGRATION_5_6: Migration = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE 'dbsensorlatestdata';")

        database.execSQL("CREATE TABLE 'dbsensorlatestdata' ( " +
                "'sensor_id' INTEGER PRIMARY KEY NOT NULL,"+
                "'timestamp' INTEGER NOT NULL,"+
                "'temp' INTEGER NOT NULL,"+
                "'vcc' INTEGER NOT NULL,"+
                "'hum' INTEGER NOT NULL,"+
                "'dhum' INTEGER NOT NULL,"+
                "'d_temp' INTEGER NOT NULL,"+
                "'d_vcc' INTEGER NOT NULL,"+
                "'d_hum' INTEGER NOT NULL,"+
                "'d_ddhum' INTEGER NOT NULL"+
                ")")
    }
}

val MIGRATION_6_7: Migration = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE 'dbsensorlatestdata';")
    }
}

val MIGRATION_7_8: Migration = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE 'dbsensorlatestdata' ( " +
                "'sensor_id' INTEGER PRIMARY KEY NOT NULL,"+
                "'timestamp' INTEGER NOT NULL,"+
                "'temp' INTEGER NOT NULL,"+
                "'vcc' INTEGER NOT NULL,"+
                "'hum' INTEGER NOT NULL,"+
                "'dhum' INTEGER NOT NULL,"+
                "'d_temp' INTEGER NOT NULL,"+
                "'d_vcc' INTEGER NOT NULL,"+
                "'d_hum' INTEGER NOT NULL,"+
                "'d_ddhum' INTEGER NOT NULL"+
                ")")
    }
}

val MIGRATION_8_9: Migration = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
    }
}

val MIGRATION_9_10: Migration = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE UNIQUE INDEX idx_sensor_latest_id ON dbsensorlatestdata (sensor_id);")
    }
}

*/

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert_latest_data(sensor_latest_data : DbSensorLatestData)

    @Query("select * from dbsensordata")
    fun getSensorsData(): LiveData<List<DbSensorData>>

    @Query("select * from dbsensordata where sensor_id = :id LIMIT 1")
    //fun getOneSensorData(id : Int): LiveData<List<DbSensorData>>
    fun getOneSensorData(id : Int): DbSensorData

    @Query("DELETE FROM dbsensor where id=:id")
    fun deleteSensor(id : Int)

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

    @Query("""
        select * from dbsensor left outer join dbsensordata on (
        dbsensor.id=dbsensordata.sensor_id and dbsensordata._id in 
          (select _id from dbsensordata sd where dbsensor.id=sd.sensor_id order by timestamp desc limit 1) 
          ) where dbsensor.id = :id limit 1
          """
    )
    fun getOneSensorWithData(id : Int): LiveData<DbSensorWithData?>
    // todo - filter validated only


    @Query("DELETE FROM dbsensordata where sensor_id=:id")
    fun deleteSensorData(id : Int)

    @Query("select count(*) from dbsensordata")
    fun getSensorDataCount(): Int

    @Query("select max(id) from dbsensor")
    fun getLastSensorId(): Int

    @Query("select count(*) as d_count, min(timestamp) as d_from, max(timestamp) as d_to from dbsensordata")
    fun getSensorDataStat(): DbDataStat

    @Query("select * from dbsensordata where sensor_id=:id order by timestamp desc")
    fun getSensorData(id: Int): LiveData<List<DbSensorData>>

    @Query("select * from dbsensorlatestdata where sensor_id=:id")
    fun getSensorLatestData(id: Int): DbSensorLatestData?

}

@Database(entities = [DbSensor::class, DbSensorData::class, DbSensorLatestData::class], version = 1)
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
                //.addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
                //    MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
                .build()
        }
    }
    return INSTANCE
}

