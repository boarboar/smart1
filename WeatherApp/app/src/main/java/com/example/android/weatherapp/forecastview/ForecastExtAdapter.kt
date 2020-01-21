package com.example.android.weatherapp.forecastview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.weatherapp.databinding.ForecastExtItemBinding
import com.example.android.weatherapp.databinding.ForecastItemBinding
import com.example.android.weatherapp.domain.WeatherForecastItem

class ForecastExtAdapter(val limit : Int = 6) : ListAdapter<WeatherForecastItem, ForecastExtAdapter.ForecastExtViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<WeatherForecastItem>() {
        override fun areItemsTheSame(oldItem: WeatherForecastItem, newItem: WeatherForecastItem): Boolean {
            return oldItem.dt === newItem.dt
        }

        override fun areContentsTheSame(oldItem: WeatherForecastItem, newItem: WeatherForecastItem): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastExtViewHolder {
        return ForecastExtViewHolder(ForecastExtItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ForecastExtViewHolder, position: Int) {
        val forecast = getItem(position)
        holder.bind(forecast)
    }

    override fun getItemCount(): Int {
        val count = super.getItemCount()
        return if (count > limit) limit  else count
    }

    class ForecastExtViewHolder(private var binding: ForecastExtItemBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(forecast: WeatherForecastItem) {
            binding.forecast = forecast
            binding.executePendingBindings()
        }
    }
}
