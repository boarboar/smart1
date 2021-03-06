package com.example.android.weatherapp.overview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.weatherapp.databinding.ForecastItemBinding
import com.example.android.weatherapp.domain.WeatherForecastItem

class ForecastAdapter(val limit : Int = 6, val onClickListener: OnClickListener) : ListAdapter<WeatherForecastItem, ForecastAdapter.ForecastViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<WeatherForecastItem>() {
        override fun areItemsTheSame(oldItem: WeatherForecastItem, newItem: WeatherForecastItem): Boolean {
            return oldItem.dt === newItem.dt
        }

        override fun areContentsTheSame(oldItem: WeatherForecastItem, newItem: WeatherForecastItem): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        return ForecastViewHolder(ForecastItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ForecastAdapter.ForecastViewHolder, position: Int) {
        val forecast = getItem(position)
        holder.bind(forecast)

        holder.itemView.setOnClickListener {
            onClickListener.onClick()
        }
    }

    override fun getItemCount(): Int {
        val count = super.getItemCount()
        return if (count > limit) limit  else count
    }

    class ForecastViewHolder(private var binding: ForecastItemBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(forecast: WeatherForecastItem) {
            binding.forecast = forecast
            binding.executePendingBindings()
        }
    }

    class OnClickListener(val clickListener: () -> Unit) {
        fun onClick() = clickListener()
    }
}
