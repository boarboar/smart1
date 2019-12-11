/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.example.android.weatherapp

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.android.weatherapp.WeatherApplication.Companion.ctx
import com.example.android.weatherapp.domain.Sensor
import com.example.android.weatherapp.domain.WeatherForecastItem
import com.example.android.weatherapp.domain.WeatherWeather
import com.example.android.weatherapp.overview.DbStatus
import com.example.android.weatherapp.overview.ForecastAdapter
import com.example.android.weatherapp.overview.SensorAdapter

import com.example.android.weatherapp.overview.WeatherApiStatus
import com.example.android.weatherapp.utils.resolveColor
import com.example.android.weatherapp.utils.resolveColorAttr
import com.squareup.picasso.Picasso
import java.util.ArrayList

@BindingAdapter("listForecastData")
fun bindRecyclerView(recyclerView: RecyclerView, data: ArrayList<WeatherForecastItem>?) {
    val adapter = recyclerView.adapter as ForecastAdapter
    adapter.submitList(data)
}

@BindingAdapter("listSensors")
fun bindRecyclerSensorView(recyclerView: RecyclerView, data: List<Sensor>?) {
    val adapter = recyclerView.adapter as SensorAdapter
    adapter.submitList(data)
}

@BindingAdapter("weatherImage")
fun bindImage(imgView: ImageView, wa : ArrayList<WeatherWeather>?) {
    wa?.let {
        if(wa.size>0) {
            Picasso.get().load("http://openweathermap.org/img/w/${wa[0].iconCode}.png")
                .into(imgView)
        }
    }
}

@BindingAdapter("weatherApiStatus")
fun bindStatus(view: View, status: WeatherApiStatus?) {
    when (status) {
        WeatherApiStatus.LOADING -> {
            view.visibility = View.VISIBLE
        }
        WeatherApiStatus.ERROR -> {
            //statusImageView.visibility = View.VISIBLE
            view.visibility = View.GONE
        }
        WeatherApiStatus.DONE -> {
            view.visibility = View.GONE
        }
    }
}

//@BindingAdapter("dbStatus")
//fun bindDbStatus(view: View, status: DbStatus?) {
//    when (status) {
//        DbStatus.LOADING -> {
//            //Log.e("BIND", "sensor progress ON")
//            view.visibility = View.VISIBLE
//        }
//        DbStatus.ERROR -> {
//            //statusImageView.visibility = View.VISIBLE
//            view.visibility = View.GONE
//            //Log.e("BIND", "sensor progress OFF")
//        }
//        DbStatus.DONE -> {
//            view.visibility = View.GONE
//            //Log.e("BIND", "sensor progress OFF")
//        }
//    }
//}

@BindingAdapter("weatherApiTextStatus")
fun bindTextStatus(statusTextView: TextView, status: String?) {
    statusTextView.text = status?.toLowerCase()
}

@BindingAdapter("sensorVccColor")
fun bindSensorVccColor(vccTextView: TextView, sensor: Sensor?) {
    sensor?.let {
        vccTextView.setTextColor(
            if (it.isVccLow)
                ctx.resolveColor(android.R.color.holo_orange_light)
            else ctx.resolveColorAttr(android.R.attr.textColorPrimary)
        )
    }
}

