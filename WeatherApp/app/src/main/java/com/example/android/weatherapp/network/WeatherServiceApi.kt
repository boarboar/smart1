package com.example.android.weatherapp.network

import com.example.android.weatherapp.domain.Weather
import com.example.android.weatherapp.domain.WeatherForecast
import com.google.gson.JsonObject
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Query

interface WeatherServiceApi {
    companion object {
        fun obtain(): WeatherServiceApi {
            return WeatherServiceRetrofit
                    .obtain()
                    .create(WeatherServiceApi::class.java)
        }
    }
    @GET("weather")
    fun getWeather(
            @Query("zip") zip: String,
            @Query("mode") mode: String="json",
            @Query("units") units: String =  "metric"
    ): Deferred<Weather>

    @GET("forecast")
    fun getWeatherForecast(
            @Query("zip") zip: String,
            @Query("mode") mode: String="json",
            @Query("units") units: String =  "metric",
            @Query("cnt") cnt: Int = 8
    ): Deferred<WeatherForecast>
}
