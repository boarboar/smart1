package com.example.android.weatherapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.preference.PreferenceManager
import com.example.android.weatherapp.WeatherApplication.Companion.ctx
import com.example.android.weatherapp.repository.SensorRepository


class BootUpReceiver : BroadcastReceiver() {
    companion object {
        const val tag = "BootUpReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        Log.i(tag, "Boot Up detected")
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(ctx)
        if(!sharedPreferences.getBoolean("run_at_startup", false)) return

        val i = Intent(
            context,
            MainActivity::class.java
        )
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(i)
    }
}