package com.example.android.weatherapp.sensorview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.weatherapp.databinding.SensorItemBinding
import com.example.android.weatherapp.databinding.SensorLogItemBinding
import com.example.android.weatherapp.domain.Sensor
import com.example.android.weatherapp.domain.SensorData


class SensorDataAdapter(/*val onClickListener: OnClickListener*/) : ListAdapter<SensorData, SensorDataAdapter.SensorViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<SensorData>() {
        override fun areItemsTheSame(oldItem: SensorData, newItem: SensorData): Boolean {
            return oldItem.sensor_id === newItem.sensor_id && oldItem.timestamp === newItem.timestamp
        }

        override fun areContentsTheSame(oldItem: SensorData, newItem: SensorData): Boolean {
            return oldItem == newItem
            //return oldItem.equalData(newItem)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorDataAdapter.SensorViewHolder {
        return SensorViewHolder(SensorLogItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: SensorDataAdapter.SensorViewHolder, position: Int) {
        val sensordata = getItem(position)
        holder.bind(sensordata)

//        holder.itemView.setOnClickListener {
//            onClickListener.onClick(sensor)
//        }

    }

    class SensorViewHolder(private var binding: SensorLogItemBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(sensordata: SensorData) {
            binding.sensordata = sensordata
            binding.executePendingBindings()
        }
    }

//    class OnClickListener(val clickListener: (sensor: Sensor) -> Unit) {
//        fun onClick(sensor: Sensor) = clickListener(sensor)
//    }

}
