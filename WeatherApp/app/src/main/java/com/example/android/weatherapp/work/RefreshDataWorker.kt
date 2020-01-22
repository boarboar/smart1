package com.example.android.weatherapp.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.android.weatherapp.database.DbLog
import com.example.android.weatherapp.domain.LogRecord
import com.example.android.weatherapp.domain.SensorTransferData
import com.example.android.weatherapp.network.SensorServiceApi
import com.example.android.weatherapp.repository.getSensorRepository
import java.net.SocketTimeoutException
import java.util.*

fun Random.nextInt(range: IntRange): Int {
    return range.start + nextInt(range.last - range.start)
}


class RefreshDataWorker(appContext: Context, params: WorkerParameters):
    CoroutineWorker(appContext, params) {

    companion object {
        const val tag = "WORKER"
        const val WORK_NAME = "RefreshDataWorker"
    }

    private val sservice : SensorServiceApi by lazy  { SensorServiceApi.obtain() }
    private val sensorRepository = getSensorRepository(appContext)

    override suspend fun doWork(): Result {
        var res = false
        var getSensorsDeferred = sservice.getSensors()
        try {
            val sdata = getSensorsDeferred.await()
            res = sensorRepository.refreshSensorsData(sdata)
            sensorRepository.logEvent(LogRecord.SEVERITY_CODE.INFO, tag, "Refresh OK")
        } catch (se: SocketTimeoutException) {
            Log.e(tag, "Socket timeout")
            sensorRepository.logEvent(LogRecord.SEVERITY_CODE.ERROR, tag, "Controller - Socket timeout")
        } catch (e: Exception) {
            val msg = e.message ?: "Unknown network error"
            Log.e(tag, "NET error: $msg")
            sensorRepository.logEvent(LogRecord.SEVERITY_CODE.ERROR, tag, "Controller - Unknown error")
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