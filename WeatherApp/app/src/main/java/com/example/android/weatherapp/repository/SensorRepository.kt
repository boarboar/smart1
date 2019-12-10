package com.example.android.weatherapp.repository

import android.content.Context
import android.util.Log
import com.example.android.weatherapp.database.DbSensorData
import com.example.android.weatherapp.work.RefreshDataWorker
import com.example.android.weatherapp.work.nextInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.*

class SensorRepository(appContext: Context)  {

    private val database = com.example.android.weatherapp.database.getDatabase(appContext)
    companion object {
        const val tag = "SensorRepository"
    }

    suspend fun refreshSensorData() : Boolean =
        withContext(Dispatchers.IO)  {
            try {
                Log.i(tag, "REFRESH RUN")
                val random = Random()
                for (sensId in 1..3) {
                    database.weatherDao.insert_data(
                        DbSensorData(
                            0, sensId, System.currentTimeMillis(),
                            random.nextInt(-400..400), random.nextInt(2540..4950),
                            random.nextInt(10..1000), random.nextInt(1..3)
                        )
                    )
                    Log.i(tag, "Refresh sensor $sensId")
                }
                val count = database.weatherDao.getSensorDataCount()
                Log.i(tag, "Total data records in DB: $count")
                true
            } catch (e: HttpException) {
                val msg = e.message ?: "Unknown HTTP error"
                Log.e(tag, "HTTP error: $msg")
                false
            } catch (t: Throwable) {
                val msg = t.message ?: "Unknown DB error"
                Log.e(tag, "DB error: $msg")
                false
            }
        }
}

private lateinit var INSTANCE: SensorRepository

fun getSensorRepository(context: Context): SensorRepository {
    synchronized(SensorRepository::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = SensorRepository(context.applicationContext)
        }
    }
    return INSTANCE
}
