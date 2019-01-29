package com.boar.smartserver.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.boar.smartserver.SmartServer
//import com.boar.smartserver.domain.Sensor
import org.jetbrains.anko.db.*


fun SQLiteDatabase.addColumn(tableName: String, column: String, type: SqlType, default: String) {
    val escapedTableName = tableName.replace("`", "``")
    execSQL("ALTER TABLE `$escapedTableName`  ADD COLUMN $column ${type.render()} DEFAULT $default;")
}

class DbHelper(ctx: Context = SmartServer.ctx) : ManagedSQLiteOpenHelper(ctx,
        DbHelper.DB_NAME, null, DbHelper.DB_VERSION) {

    companion object {
        private const val tag = "DB HLP"
        val DB_NAME = "smartserver.db"
        val DB_VERSION = 4
        val instance by lazy { DbHelper() }
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.v(tag, "[ ON DB CREATE ]")

        db.createTable(SensorTable.NAME, true,
                SensorTable.ID to INTEGER + PRIMARY_KEY,
                SensorTable.DESCRIPTION to TEXT
                )

        db.createTable(LogTable.NAME, true,
                LogTable.ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                LogTable.TIMESTAMP to INTEGER,
                LogTable.MSG to TEXT
        )
        db.createTable(SensorHistoryTable.NAME, true,
                SensorHistoryTable.ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                SensorHistoryTable.SENSOR_ID to INTEGER ,
                SensorHistoryTable.TIMESTAMP to INTEGER,
                SensorHistoryTable.TEMPERATURE to INTEGER,
                SensorHistoryTable.VCC to INTEGER
        )
        db.createIndex(SensorHistoryTable.SENSOR_ID, SensorHistoryTable.NAME,
                false, true, SensorHistoryTable.SENSOR_ID)
    }



    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        Log.v(tag, "[ ON DB UPGRADE ]")

        //db.dropTable(SensorTable.NAME, true)
        //db.dropTable(LogTable.NAME, true)
        //db.dropTable(SensorHistoryTable.NAME, true)

        //onCreate(db)

        db.addColumn(SensorHistoryTable.NAME, SensorHistoryTable.HUMIDITY, INTEGER, "0")
        db.addColumn(SensorHistoryTable.NAME, SensorHistoryTable.DHUMIDITY, INTEGER, "0")
    }
}