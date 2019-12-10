package com.example.android.weatherapp.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.android.weatherapp.repository.getSensorRepository
import java.util.*

fun Random.nextInt(range: IntRange): Int {
    return range.start + nextInt(range.last - range.start)
}


class RefreshDataWorker(appContext: Context, params: WorkerParameters):
    CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "RefreshDataWorker"
    }

    //private val database = getDatabase(appContext)
    private val sensorRepository = getSensorRepository(appContext)

    override suspend fun doWork(): Result {
         return if(sensorRepository.refreshSensorData()==true) Result.success() else Result.failure()
    }
}