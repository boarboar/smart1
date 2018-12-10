package com.boar.smartserver.presenter

import android.content.IntentFilter
import android.util.Log
import com.boar.smartserver.R.id.weather_city
import com.boar.smartserver.R.id.weather_now_temp
import com.boar.smartserver.SmartServer
import com.boar.smartserver.UI.MainActivity
import com.boar.smartserver.domain.Sensor
import com.boar.smartserver.domain.Weather
import com.boar.smartserver.domain.WeatherForecast
import com.boar.smartserver.network.WeatherServiceApi
import com.boar.smartserver.receiver.MainServiceReceiver
import com.boar.smartserver.service.MainService
import kotlinx.android.synthetic.main.weather.*

class MainPresenter() {

    companion object {
        private val tag = "Main presenter"
        fun iconToUrl(w : Weather) = if(w.weather.size>0) "http://openweathermap.org/img/w/${w.weather[0].iconCode}.png" else ""
        //private val CITYCODE = "192071,Ru"
        private val CITYCODE = "193312,Ru"
        private val RETAIN_WEATHER = 300_000 // milliseconds
        val instance = MainPresenter()
    }


    private val receiver: MainServiceReceiver = MainServiceReceiver { op, idx ->
        when (op) {
            MainService.BROADCAST_EXTRAS_OP_LOAD -> dataLoadHandler?.invoke()
            MainService.BROADCAST_EXTRAS_OP_ADD -> sensorAddHandler?.invoke(idx)
            MainService.BROADCAST_EXTRAS_OP_DEL -> sensorDeleteHandler?.invoke(idx)
            MainService.BROADCAST_EXTRAS_OP_UPD -> {
                lastUpdated = System.currentTimeMillis()
                sensorUpdateHandler?.invoke(idx)
            }
            else -> Log.v(tag, "[ BRDCST $op $idx]")
        }
    }

    var lastUpdated = 0L
    var sensorRefreshIdx = -1
    var sensorOp : String = ""

    private val wservice : WeatherServiceApi by lazy  { WeatherServiceApi.obtain() }
    private var weather : Weather? = null
    private var updated : Long = 0
    private var weatherForecast : WeatherForecast? = null
    private var updatedForecast : Long = 0
    private var service: MainService? = null

    private var sensorAddHandler : ((Int)->Unit) ? = null
    private var sensorUpdateHandler : ((Int)->Unit) ? = null
    private var sensorDeleteHandler : ((Int)->Unit) ? = null
    private var dataLoadHandler : (()->Unit) ? = null

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
                    if(it.cod==200)
                        refreshView(it)
                }
            }
            Log.i(tag, "Get weather OK")
        }  catch (t: Throwable) {
            Log.w(tag, "Json error: ${t.message}")
        }
    }

    fun refreshWeatherForecast( refreshView : (WeatherForecast)->Unit ) {
        if(weatherForecast !=null && System.currentTimeMillis() < updatedForecast + RETAIN_WEATHER) {
            weatherForecast?.let {refreshView(it)}
            return
        }
        try {
            val weatherForceastResponse = wservice.getWeatherForecast(CITYCODE).execute()
            if (weatherForceastResponse.isSuccessful) {
                weatherForecast = weatherForceastResponse.body()
                weatherForecast?.let {
                    Log.i(tag, "Get weather forecast $it")
                    //updated = System.currentTimeMillis()
                    if(it.cod==200)
                        refreshView(it)
                    Log.i(tag, "Get weather forecast valid")
                }
            }
            Log.i(tag, "Get weather forecast OK")
        }  catch (t: Throwable) {
            Log.w(tag, "Json error: ${t.message}")
        }
    }

    fun attachService(srv: MainService?) { service = srv}
    fun detachService() { service = null}


    fun detachReceiver() : Unit {}
    fun attachReceiver() = apply {
        val filter = IntentFilter()
        filter.addAction(MainService.BROADCAST_ACTION)
        SmartServer.ctx.registerReceiver(receiver, filter)
    }
    fun onSensorAdd(sensorAddUandler : (Int)->Unit) = apply { this.sensorAddHandler = sensorAddUandler }
    fun onSensorUpdate(sensorUpdateUandler : (Int)->Unit) = apply { this.sensorUpdateHandler = sensorUpdateUandler }
    fun onSensorDelete(sensorDeleteUandler : (Int)->Unit) = apply { this.sensorDeleteHandler = sensorDeleteUandler }
    fun onDataLoad(dataLoadHandler : ()->Unit) = apply { this.dataLoadHandler = dataLoadHandler }


    val isDataLoaded : Boolean
    get() {
        return service?.isLoaded() ?: false
    }

    val sensorListSize : Int
    get() {
        return service?.sensorListSize ?: 0
    }

    fun getSensor(position : Int) = service?.getSensor(position)
    fun addSensor(sensor : Sensor) = service?.addSensor(sensor)
    fun editSensor(position : Int, sensor : Sensor) {
        service?.editSensor(position, sensor)
        sensorRefreshIdx = position
        sensorOp = MainService.BROADCAST_EXTRAS_OP_UPD
    }
    fun deleteSensor(position : Int) {
        service?.deleteSensor(position)
        sensorRefreshIdx = position
        sensorOp = MainService.BROADCAST_EXTRAS_OP_DEL
    }

    val logListSize : Int
        get() {
            return service?.logListSize ?: 0
        }

    fun getServiceLog(position : Int) = service?.getServiceLog(position)

    /*
    val sensorNeedToUpdate : Int
        get() {
            val ret = sensorRefreshIdx
            sensorRefreshIdx = -1
            return ret
        }
    */
}



