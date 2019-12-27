package com.example.android.weatherapp.network

import com.example.android.weatherapp.BuildConfig
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object SensorServiceRetrofit {
    fun obtain(
            readTimeoutInSeconds: Long = 5,
            connectTimeoutInSeconds: Long = 5
    ): Retrofit {
        return Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .client(
                        OkHttpClient
                                .Builder()
                                .addInterceptor(makeLoggingInterceptor())
                                .addInterceptor(makeHeadersInterceptor())
                                .readTimeout(readTimeoutInSeconds, TimeUnit.SECONDS)
                                .connectTimeout(connectTimeoutInSeconds, TimeUnit.SECONDS)
                                .build()
                )
                .build()
    }
}
