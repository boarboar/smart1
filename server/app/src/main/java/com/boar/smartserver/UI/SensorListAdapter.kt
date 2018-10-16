package com.boar.smartserver.UI

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.boar.smartserver.R
import com.boar.smartserver.domain.Sensor
import kotlinx.android.synthetic.main.item_sensor.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class SensorListAdapter(private val sensors: List<Sensor>,
                          private val itemClick: (Sensor) -> Unit) :
        RecyclerView.Adapter<SensorListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sensor, parent, false)
        return ViewHolder(view, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindForecast(sensors[position])
    }

    override fun getItemCount() = sensors.size

    class ViewHolder(view: View, private val itemClick: (Sensor) -> Unit)
        : RecyclerView.ViewHolder(view) {

        fun bindForecast(sensor: Sensor) {
            with(sensor) {
                itemView.date.text = if(updated !=0L ) convertTime(updated) else "--:--:--"
                itemView.description.text = description
                itemView.temperature.text = "${temperatureAsString}º"
                itemView.vcc.text = "${vccAsString} v"
                itemView.setOnClickListener { itemClick(this) }
            }
        }


        // TODO: move formatters to instance, they rae not tredsafe bit it's no issue cince called from UIThread only
        private fun convertTime(date: Long): String {
            val df = SimpleDateFormat("HH:mm:ss")
            return df.format(date)
        }

        private fun convertDate(date: Long): String {
            val df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
            return df.format(date)
        }

    }
}
