package com.boar.smartserver.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.boar.smartserver.SmartServer
//import com.boar.smartserver.domain.Sensor
import org.jetbrains.anko.db.*

class DbHelper(ctx: Context = SmartServer.ctx) : ManagedSQLiteOpenHelper(ctx,
        DbHelper.DB_NAME, null, DbHelper.DB_VERSION) {

    companion object {
        private const val tag = "DB HLP"
        val DB_NAME = "smartserver.db"
        val DB_VERSION = 1
        val instance by lazy { DbHelper() }
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.v(tag, "[ ON DB CREATE ]")

        db.createTable(SensorTable.NAME, true,
                SensorTable.ID to INTEGER + PRIMARY_KEY,
                SensorTable.DESCRIPTION to TEXT
                )
/*
        db.createTable(DayForecastTable.NAME, true,
                DayForecastTable.ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                DayForecastTable.DATE to INTEGER,
                DayForecastTable.DESCRIPTION to TEXT,
                DayForecastTable.HIGH to INTEGER,
                DayForecastTable.LOW to INTEGER,
                DayForecastTable.ICON_URL to TEXT,
                DayForecastTable.CITY_ID to INTEGER)
                */
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        Log.v(tag, "[ ON DB UPGRADE ]")

        db.dropTable(SensorTable.NAME, true)
        //db.dropTable(DayForecastTable.NAME, true)
        onCreate(db)
    }
}