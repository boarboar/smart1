package com.example.android.weatherapp.overview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.weatherapp.databinding.ForecastItemBinding
import com.example.android.weatherapp.domain.WeatherForecastItem

class ForecastAdapter(/*val onClickListener: OnClickListener*/) : ListAdapter<WeatherForecastItem, ForecastAdapter.ForecastViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<WeatherForecastItem>() {
        override fun areItemsTheSame(oldItem: WeatherForecastItem, newItem: WeatherForecastItem): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: WeatherForecastItem, newItem: WeatherForecastItem): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastAdapter.ForecastViewHolder {
        return ForecastViewHolder(ForecastItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ForecastAdapter.ForecastViewHolder, position: Int) {
        val forecast = getItem(position)
        holder.bind(forecast)
        /*
        holder.itemView.setOnClickListener {
            onClickListener.onClick(marsProperty)
        }
        */
    }

    class ForecastViewHolder(private var binding: ForecastItemBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(forecast: WeatherForecastItem) {
            binding.forecast = forecast
            binding.executePendingBindings()
        }
    }
/*
    class OnClickListener(val clickListener: (marsProperty: MarsProperty) -> Unit) {
        fun onClick(marsProperty:MarsProperty) = clickListener(marsProperty)
    }
*/
}
