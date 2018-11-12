package com.boar.smartserver.UI


import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.boar.smartserver.R
import com.boar.smartserver.SmartServer
import com.boar.smartserver.SmartServer.Companion.ctx
import com.boar.smartserver.domain.Sensor
import com.boar.smartserver.service.MainService
import com.boar.smartserver.extensions.resolveColor
import com.boar.smartserver.presenter.MainPresenter
import kotlinx.android.synthetic.main.item_sensor.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

//class SensorListAdapter(private val sensors: List<Sensor>,
//class SensorListAdapter(private val service : MainService?,
class SensorListAdapter(private val presenter : MainPresenter,
                        //private val itemClick: (Sensor) -> Unit) :
                        private val itemClick: (Int) -> Unit) :
        RecyclerView.Adapter<SensorListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sensor, parent, false)
        return ViewHolder(view, itemClick)
    }
/*
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindForecast(sensors[position])
    }
     override fun getItemCount() = sensors.size

    */

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //holder.bindForecast(service?.getSensor(position))
        //holder.bindForecast(presenter.getSensor(position))
        holder.bindForecast(presenter, position)
    }

    //override fun getItemCount() = service?.sensorListSize ?: 0
    override fun getItemCount() = presenter.sensorListSize


    //class ViewHolder(view: View, private val itemClick: (Sensor) -> Unit)
    class ViewHolder(view: View, private val itemClick: (Int) -> Unit)
        : RecyclerView.ViewHolder(view) {

        companion object {
            val df_time = SimpleDateFormat("HH:mm:ss")
            val df_date = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
        }

        //fun bindForecast(sensor: Sensor?) {
        fun bindForecast( presenter : MainPresenter, position: Int) {
             //sensor?.apply {
            presenter.getSensor(position)?.apply {
                 itemView.date.text = if(updated !=0L ) convertTime(updated) else "--:--:--"
                 itemView.description.text = description
                 itemView.temperature.text = "${temperatureAsString}º"
                 itemView.vcc.text = "${vccAsString} v"
                 //itemView.status.text = if(validated) "" else "!"
                 if(validated) {
                     itemView.status.text = if(updated !=0L ) "Ok" else ""
                     itemView.status.setTextColor(ctx.resolveColor(android.R.color.holo_green_light))
                 } else {
                     itemView.status.text = "Bad"
                     itemView.status.setTextColor(ctx.resolveColor(android.R.color.holo_red_light))
                 }
                 //itemView.setOnClickListener { itemClick(this) }
                itemView.setOnClickListener { itemClick(position) }
            }
            /*

            textColor

            in -50..0 -> android.R.color.holo_red_dark
            in 0..15 -> android.R.color.holo_orange_dark
            else -> android.R.color.holo_green_dark
            */
        }


        // TODO: move formatters to instance, they rae not tredsafe bit it's no issue cince called from UIThread only
        private fun convertTime(date: Long): String {
            //val df = SimpleDateFormat("HH:mm:ss")
            return df_time.format(date)
        }

        private fun convertDate(date: Long): String {
            //val df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
            return df_date.format(date)
        }

    }
}
