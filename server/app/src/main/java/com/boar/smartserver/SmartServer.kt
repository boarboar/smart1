package com.boar.smartserver

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import com.boar.smartserver.service.MainService
import com.jakewharton.threetenabp.AndroidThreeTen

class SmartServer : Application() {

    companion object {
        val tag = "SmartServer"
        lateinit var ctx: Context

    }
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        ctx = applicationContext
        startService()
        Log.v(tag, "[ ON CREATE ]")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(tag, "[ ON LOW MEMORY ]")
        //stopService()
    }
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.d(tag, "[ ON TRIM MEMORY ]: $level")
    }

    private fun startService() {
        val serviceIntent = Intent(this, MainService::class.java)
        startService(serviceIntent)
    }
    private fun stopService() {
        val serviceIntent = Intent(this, MainService::class.java)
        stopService(serviceIntent)
    }


}

