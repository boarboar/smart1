package com.boar.smartserver.presenter

import android.util.Log
import com.boar.smartserver.R.id.weather_city
import com.boar.smartserver.R.id.weather_now_temp
import com.boar.smartserver.UI.MainActivity
import com.boar.smartserver.domain.Weather
import com.boar.smartserver.network.WeatherServiceApi
import kotlinx.android.synthetic.main.weather.*

class MainPresenter(/*val view: MainActivity*/) {

    companion object {
        private val tag = "Main presenter"
        fun iconToUrl(w : Weather) = if(w.weather.size>0) "http://openweathermap.org/img/w/${w.weather[0].iconCode}.png" else ""
        private val CITYCODE = "192071,Ru"
        private val RETAIN_WEATHER = 300_000 // milliseconds
    }


    private val wservice : WeatherServiceApi by lazy  { WeatherServiceApi.obtain() }
    private var weather : Weather? = null
    private var updated : Long = 0


    fun refreshWeather( refreshView : (Weather)->Unit ) {
        if(weather !=null && System.currentTimeMillis() < updated + RETAIN_WEATHER) {
            weather?.let {refreshView(it)}
            return
        }
        try {
            val weatherResponse = wservice.getWeather(CITYCODE).execute()
            if (weatherResponse.isSuccessful) {
                weather = weatherResponse.body()
                weather?.let {
                    Log.i(tag, "Get weather  $it")
                    updated = System.currentTimeMillis()
                    refreshView(it)
                }
            }
            Log.i(tag, "Get weather OK")
        }  catch (t: Throwable) {
            Log.w(tag, "Json error: ${t.message}")
        }
    }


}



