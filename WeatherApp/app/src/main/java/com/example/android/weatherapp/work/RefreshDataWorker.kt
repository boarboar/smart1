package com.example.android.weatherapp.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.android.weatherapp.database.*
import retrofit2.HttpException
import java.util.*

fun Random.nextInt(range: IntRange): Int {
    return range.start + nextInt(range.last - range.start)
}


class RefreshDataWorker(appContext: Context, params: WorkerParameters):
    CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "RefreshDataWorker"
    }

    private val database = getDatabase(appContext)

    override suspend fun doWork(): Result {

        return try {
            Log.i(WORK_NAME, "REFRESH WORKER RUN")
            val random = Random()
            for( sensId in 1..3) {
                database.weatherDao.insert_data(DbSensorData(0, sensId, System.currentTimeMillis(),
                    random.nextInt(-400..400), random.nextInt(2540..4950),
                    random.nextInt(10..1000), random.nextInt(1..3)))
                Log.i(WORK_NAME, "Refresh sensor $sensId")

            }

            val count = database.weatherDao.getSensorDataCount()
            Log.i(WORK_NAME, "Total data records in DB: $count")

            Result.success()
        } catch (e: HttpException) {
            val msg = e.message ?: "Unknown HTTP error"
            Log.e(WORK_NAME, "HTTP error: $msg")
            Result.failure()
        } catch (t: Throwable) {
            val msg = t.message ?: "Unknown DB error"
            Log.e(WORK_NAME, "DB error: $msg")
            Result.failure()
        }

    }
}