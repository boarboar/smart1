package com.example.android.weatherapp.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import retrofit2.HttpException

class RefreshDataWorker(appContext: Context, params: WorkerParameters):
    CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "RefreshDataWorker"
    }

    override suspend fun doWork(): Result {

        //val database = getDatabase(applicationContext)
        //val repository = VideosRepository(database)

        return try {
            Log.i(WORK_NAME, "REFRESH")
            //repository.refreshVideos()
            Result.success()
        } catch (e: HttpException) {
            Result.failure()
        }

    }
}