package com.example.android.weatherapp.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.android.weatherapp.domain.SensorTransferData
import com.example.android.weatherapp.network.SensorServiceApi
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

    private val sservice : SensorServiceApi by lazy  { SensorServiceApi.obtain() }
    private val sensorRepository = getSensorRepository(appContext)

    override suspend fun doWork(): Result {
        var res = false
        var getSensorsDeferred = sservice.getSensors()
        try {
            val sdata = getSensorsDeferred.await()
//            for(s in sdata) {
//                Log.i("WORKER", "Retrieved: $s")
//            }
            res = sensorRepository.refreshSensorsData(sdata)

        } catch (e: Exception) {
            val msg = e.message ?: "Unknown network error"
            Log.e("WORKER", "NET error: $msg")
        }

        // fake data
//        val random = Random()
//        var data = mutableListOf<SensorTransferData>()
//        for (sensId in 1..2) {
//            data.add(
//                SensorTransferData(sensId, System.currentTimeMillis(),
//                    random.nextInt(-400..400).toShort(), random.nextInt(2540..4950).toShort(),
//                    random.nextInt(10..1000).toShort(), random.nextInt(1..3).toShort()
//                )
//            )
//        }
//        return if(sensorRepository.refreshSensorsData(data)==true) Result.success() else Result.failure()
        return if(res) Result.success() else Result.failure()
    }
}