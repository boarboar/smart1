package com.boar.smartserver.presenter

import android.util.Log
import com.boar.smartserver.R.id.weather_city
import com.boar.smartserver.R.id.weather_now_temp
import com.boar.smartserver.UI.MainActivity
import com.boar.smartserver.domain.Weather
import com.boar.smartserver.network.WeatherServiceApi
import kotlinx.android.synthetic.main.weather.*

class MainPresenter(val view: MainActivity) {
    private val tag = "Main presenter"
    val wservice : WeatherServiceApi by lazy  { WeatherServiceApi.obtain() }

    fun refreshWeather( refreshView : (Weather)->Unit ) {

        val weatherResponse = wservice.getWeather("192071,Ru").execute()
        if (weatherResponse.isSuccessful) {
            val resp = weatherResponse.body()
            resp?.let {
                Log.i(tag, "Get weather  $resp")
                refreshView(resp)
            }
        }
        Log.i(tag, "Get weather OK")
    }
}