package com.example.android.weatherapp.domain

import com.google.gson.annotations.SerializedName


data class WeatherMain(
        @SerializedName("temp") val temp_f: Float,
        @SerializedName("humidity") val humidity_f: Float,
        @SerializedName("pressure") val pressure_f: Float
) {
    val pressure_mm : Int
        get() = (pressure_f * 75 / 100).toInt()
    val humidity : Int
        get() = (humidity_f).toInt()
    val temp : Float
        get() = (temp_f*10).toInt()/10.0F

    val tempString : String
        get() = temp.toString()+"º"
    val humString : String
        get() = humidity.toString()+"%"
    val presString : String
        get() = pressure_mm.toString()+" mm"
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
        @SerializedName("speed") val speed_f: Float,
        @SerializedName("deg") val deg: Float
){
    val dir : String
        get() = when(deg.toInt()) {
            in 23..67 -> "С-В"
            in 68..112 -> "В"
            in 113..157 -> "Ю-В"
            in 158..202 -> "Ю"
            in 203..247 -> "Ю-З"
            in 248..292 -> "С-З"
            else -> "С"
        }
    val speed : Int
        get() = (speed_f).toInt()

    val speedString : String
        get() = (speed_f).toInt().toString()+" m/c"
}

data class WeatherSys(
        @SerializedName("sunrise") val sunrise: Long,
        @SerializedName("sunset") val sunset: Long
)

data class Weather(
        @SerializedName("cod") val cod: Int,
        @SerializedName("dt") val dt: Long,
        @SerializedName("name") val name: String,
        @SerializedName("main") val main: WeatherMain,
        @SerializedName("wind") val wind: WeatherWind,
        @SerializedName("sys") val sys: WeatherSys,
        @SerializedName("weather") val weather: ArrayList<WeatherWeather>)
{
    val temp : String
        get() = main.tempString
    val humidity : String
        get() = main.humString
    val pressure : String
        get() = main.presString
    val wind_value : String
        get() = wind.speedString
    val wind_dir : String
        get() = wind.dir
}



data class WeatherForecastCity(
        @SerializedName("name") val name: String,
        @SerializedName("country") val country: String
)

data class WeatherForecastItem(
        @SerializedName("dt") val dt: Long,
        @SerializedName("main") val main: WeatherMain,
        @SerializedName("wind") val wind: WeatherWind,
        @SerializedName("weather") val weather: ArrayList<WeatherWeather>
)

data class WeatherForecast(
        @SerializedName("cod") val cod: Int,
        @SerializedName("city") val city: WeatherForecastCity,
        @SerializedName("list") val forecast: ArrayList<WeatherForecastItem>
)