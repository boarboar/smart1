package com.example.android.weatherapp.logview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.weatherapp.database.DbLog
import com.example.android.weatherapp.databinding.ForecastExtItemBinding
import com.example.android.weatherapp.databinding.LogItemBinding
import com.example.android.weatherapp.domain.LogRecord
import com.example.android.weatherapp.domain.WeatherForecastItem

// TODO  - use domain Log class

class LogviewAdapter() : ListAdapter<LogRecord, LogviewAdapter.LogviewViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<LogRecord>() {
        override fun areItemsTheSame(oldItem: LogRecord, newItem: LogRecord): Boolean {
            return oldItem.id === newItem.id
        }

        override fun areContentsTheSame(oldItem: LogRecord, newItem: LogRecord): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogviewViewHolder {
        return LogviewViewHolder(LogItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: LogviewViewHolder, position: Int) {
        val log = getItem(position)
        holder.bind(log)
    }

    class LogviewViewHolder(private var binding: LogItemBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(log: LogRecord) {
            binding.log = log
            binding.executePendingBindings()
        }
    }
}
