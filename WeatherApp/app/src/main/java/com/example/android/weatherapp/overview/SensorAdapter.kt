package com.example.android.weatherapp.overview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.weatherapp.databinding.SensorItemBinding
import com.example.android.weatherapp.domain.Sensor


class SensorAdapter(/*val onClickListener: OnClickListener*/) : ListAdapter<Sensor, SensorAdapter.SensorViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Sensor>() {
        override fun areItemsTheSame(oldItem: Sensor, newItem: Sensor): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Sensor, newItem: Sensor): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorAdapter.SensorViewHolder {
        return SensorViewHolder(SensorItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: SensorAdapter.SensorViewHolder, position: Int) {
        val sensor = getItem(position)
        holder.bind(sensor)
        /*
        holder.itemView.setOnClickListener {
            onClickListener.onClick(marsProperty)
        }
        */
    }

    class SensorViewHolder(private var binding: SensorItemBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(sensor: Sensor) {
            binding.sensor = sensor
            binding.executePendingBindings()
        }
    }
/*
    class OnClickListener(val clickListener: (marsProperty: MarsProperty) -> Unit) {
        fun onClick(marsProperty:MarsProperty) = clickListener(marsProperty)
    }
*/
}
