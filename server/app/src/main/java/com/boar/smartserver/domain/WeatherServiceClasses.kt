package com.boar.smartserver.domain

import com.google.gson.annotations.SerializedName

data class WeatherMain(
        @SerializedName("temp") val temp: Int,
        @SerializedName("humidity") val humidity: Int,
        @SerializedName("pressure") val pressure: Int
)

data class WeatherWeather(
        @SerializedName("main") val descr: String,
        @SerializedName("icon") val iconCode: String
)

data class Weather(
        @SerializedName("name") val name: String,
        @SerializedName("main") val main: WeatherMain,
        @SerializedName("weather") val weather: ArrayList<WeatherWeather>
)
