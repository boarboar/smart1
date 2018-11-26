package com.boar.smartserver.network

import com.boar.smartserver.domain.Weather
import com.boar.smartserver.domain.WeatherForecast
import com.google.gson.JsonObject
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
    ):  Call<Weather>

    @GET("forecast")
    fun getWeatherForecast(
            @Query("zip") zip: String,
            @Query("mode") mode: String="json",
            @Query("units") units: String =  "metric",
            @Query("cnt") cnt: Int = 8
    ):  Call<WeatherForecast>
}
