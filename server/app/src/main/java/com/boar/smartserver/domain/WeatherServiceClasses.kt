package com.boar.smartserver.domain

import com.google.gson.annotations.SerializedName


data class WeatherMain(
        @SerializedName("temp") val temp: Int,
        @SerializedName("humidity") val humidity: Int,
        @SerializedName("pressure") val pressure: Int
) {
    val pressure_mm : Int
        get() = pressure * 75 / 100
}

data class WeatherWeather(
        @SerializedName("main") val descr: String,
        @SerializedName("icon") val iconCode: String
)

/*

Wind direction is measured in degrees clockwise from due north. Consequently, a wind blowing from the north has a wind direction of 0°;
a wind blowing from the east has a wind direction of 90°; a wind blowing from the south has a wind direction of 180°;
and a wind blowing from the west has a wind direction of 270°.
In general, wind directions are measured in units from 0° to 360°, but can alternatively be expressed from -180° to 180°.
 */

data class WeatherWind(
        @SerializedName("speed") val speed: Int,
        @SerializedName("deg") val deg: Int
){
    val dir : String
        get() = when(deg) {
            in 23..67 -> "С-В"
            in 68..112 -> "И"
            in 113..157 -> "Ю-В"
            in 158..202 -> "Ю"
            in 203..247 -> "Ю-З"
            in 248..292 -> "С-З"
            else -> "С"
        }
}


data class Weather(
        @SerializedName("name") val name: String,
        @SerializedName("main") val main: WeatherMain,
        @SerializedName("wind") val wind: WeatherWind,
        @SerializedName("weather") val weather: ArrayList<WeatherWeather>
)
