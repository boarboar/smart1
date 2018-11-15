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
        fun bindForecast( presenter : MainPresenter, position: Int) {
            presenter.getSensor(position)?.apply {
                 itemView.date.text = if(updated !=0L ) DateUtils.convertTime(updated) else "--:--:--"
                 itemView.description.text = description
                 itemView.temperature.text = "${temperatureAsString}º"
                 itemView.vcc.text = "${vccAsString} v"
                 if(validated) {
                     itemView.status.text = if(updated !=0L ) "Ok" else ""
                     itemView.status.setTextColor(ctx.resolveColor(android.R.color.holo_green_light))
                 } else {
                     itemView.status.text = "Bad"
                     itemView.status.setTextColor(ctx.resolveColor(android.R.color.holo_red_light))
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
