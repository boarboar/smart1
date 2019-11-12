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

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.android.weatherapp.domain.WeatherForecastItem
import com.example.android.weatherapp.domain.WeatherWeather
import com.example.android.weatherapp.overview.ForecastAdapter

import com.example.android.weatherapp.overview.WeatherApiStatus
import com.squareup.picasso.Picasso
import java.util.ArrayList

/*
@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
        Glide.with(imgView.context)
                .load(imgUri)
                .apply(RequestOptions()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image))
                .into(imgView)
    }
}

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<MarsProperty>?) {
    val adapter = recyclerView.adapter as PhotoGridAdapter
    adapter.submitList(data)
}
*/

/*
@BindingAdapter("weatherApiStatus")
fun bindStatus(statusImageView: ImageView, status: WeatherApiStatus?) {
    when (status) {
        WeatherApiStatus.LOADING -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.loading_animation)

        }
        WeatherApiStatus.ERROR -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.ic_connection_error)
        }
        WeatherApiStatus.DONE -> {
            statusImageView.visibility = View.GONE
        }
    }
}
*/

@BindingAdapter("listForecastData")
fun bindRecyclerView(recyclerView: RecyclerView, data: ArrayList<WeatherForecastItem>?) {
    val adapter = recyclerView.adapter as ForecastAdapter
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

@BindingAdapter("weatherApiTextStatus")
fun bindTextStatus(statusTextView: TextView, status: String?) {
    statusTextView.text = status?.toLowerCase()
}