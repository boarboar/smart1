package com.example.android.weatherapp.network

import com.example.android.weatherapp.domain.SensorTransferData
import com.example.android.weatherapp.domain.Weather
import com.example.android.weatherapp.domain.WeatherForecast
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query

interface SensorServiceApi {
    companion object {
        fun obtain(): SensorServiceApi {
            return SensorServiceRetrofit
                    .obtain()
                    .create(SensorServiceApi::class.java)
        }
    }
    @GET("sensors")
    fun getSensors(): Deferred<List<SensorTransferData>>
}
