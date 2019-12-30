package com.example.android.weatherapp.network

import android.content.Context
import androidx.preference.PreferenceManager
import com.example.android.weatherapp.BuildConfig
import com.example.android.weatherapp.WeatherApplication.Companion.ctx
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object SensorServiceRetrofit {

    const val DEF_BASE_URL = "http://10.0.2.2:8080/"
    //const val BASE_URL = "http://192.168.1.144:80/"

    fun obtain(
            readTimeoutInSeconds: Long = 5,
            connectTimeoutInSeconds: Long = 15
    ): Retrofit {
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(ctx)
        var url = sharedPreferences.getString("sensor_service_url", DEF_BASE_URL)
        if(!url.startsWith("http://"))  url = "http://"+url
        if(!url.endsWith("/"))  url = url + "/"

        return Retrofit.Builder()
                .baseUrl(url)
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
