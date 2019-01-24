package com.boar.smartserver.UI

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.boar.smartserver.R
import com.boar.smartserver.SmartServer.Companion.ctx
import com.boar.smartserver.extensions.resolveColor
import com.boar.smartserver.presenter.MainPresenter
import kotlinx.android.synthetic.main.item_sensor.view.*


class SensorListAdapter(private val presenter : MainPresenter,
                        private val itemClick: (Int) -> Unit) :
        RecyclerView.Adapter<SensorListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sensor, parent, false)
        return ViewHolder(view, itemClick)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindForecast(presenter, position)
    }

    override fun getItemCount() = presenter.sensorListSize


    class ViewHolder(view: View, private val itemClick: (Int) -> Unit)
        : RecyclerView.ViewHolder(view) {
        companion object {
            val color_ok = ctx.resolveColor(android.R.color.holo_green_light)
            val color_bad = ctx.resolveColor(android.R.color.holo_red_light)
        }
        fun bindForecast( presenter : MainPresenter, position: Int) {
            presenter.getSensor(position)?.apply {
                 itemView.date.text = if(measUpdatedTime !=0L ) DateUtils.convertTime(measUpdatedTime) else "--:--:--"
                 itemView.date.setTextColor(if(measUpdatedTime !=0L && outdated) color_bad else color_ok)
                 itemView.description.text = description
                 itemView.temperature.text = "${temperatureAsString}º"
                 itemView.vcc.text = "${vccAsString} v"
                 itemView.hm.text = "$humidityAsString"
                 itemView.hd.text = if(humidityDig==1) "\u2614" else ""
                 if(measValidated && !outdated) {
                     itemView.status.text = if(measUpdatedTime !=0L ) "\u2713" else ""
                     //itemView.status.setTextColor(ctx.resolveColor(android.R.color.holo_green_light))
                     itemView.status.setTextColor(color_ok)
                 } else {
                     itemView.status.text = "\u2717"
                     //itemView.status.setTextColor(ctx.resolveColor(android.R.color.holo_red_light))
                     itemView.status.setTextColor(color_bad)
                 }
                 itemView.temp_grad.text = when {
                     //temp_grad > 0 -> "\u2197"
                     //temp_grad < 0 -> "\u2198"
                     temp_grad > 0 -> "\u21D7"
                     temp_grad < 0 -> "\u21D8"
                     else -> ""
                 }

                 itemView.setOnClickListener { itemClick(position) }
            }
            /*

            textColor

            in -50..0 -> android.R.color.holo_red_dark
            in 0..15 -> android.R.color.holo_orange_dark
            else -> android.R.color.holo_green_dark
            */
        }

    }
}
