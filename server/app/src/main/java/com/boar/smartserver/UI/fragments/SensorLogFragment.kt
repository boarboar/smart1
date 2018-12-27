package com.boar.smartserver.UI.fragments

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.boar.smartserver.R
import com.boar.smartserver.UI.DateUtils
import com.boar.smartserver.presenter.MainPresenter
import kotlinx.android.synthetic.main.fragment_sensor_log.*
import kotlinx.android.synthetic.main.fragment_sensor_log.view.*
import kotlinx.android.synthetic.main.item_sensor_hist.view.*
import android.support.v7.widget.LinearLayoutManager
import com.boar.smartserver.domain.SensorHistory
import kotlinx.android.synthetic.main.item_sensor_hist.view.*


class SensorLogFragment : SensorBaseFragment() {

    override val ftag = "Log frag"
    var hist : List<SensorHistory> = listOf()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        val view = inflater?.inflate(R.layout.fragment_sensor_log, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.v(ftag, "[ ON VIEW CREATED ]")
        if (idx != -1) {
            val sensorId = (presenter.getSensor(idx)?.id ?: -1).toInt()
            hist = presenter.getSensorHistory(sensorId)
            val adapter = SensorLogListAdapter(hist) {}
            val layout = LinearLayoutManager(context)
            view?.sensor_log?.layoutManager = layout
            view?.sensor_log?.adapter = adapter
        }
    }
}

class SensorLogListAdapter(private val hist : List<SensorHistory>,
                           private val itemClick: (Int) -> Unit) :
        RecyclerView.Adapter<SensorLogListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sensor_hist, parent, false)
        return ViewHolder(view, itemClick)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindLog(hist, position)
    }

    override fun getItemCount() : Int {
        //return hist.size
        return if(hist.size <= 128) hist.size else 128
    }

    class ViewHolder(view: View, private val itemClick: (Int) -> Unit)
        : RecyclerView.ViewHolder(view) {
        fun bindLog( hist : List<SensorHistory>, position: Int) {

            hist[position].apply {
                itemView.timestamp.text = DateUtils.convertDateTime(timestamp)
                itemView.temperature.text = "$temperature"
                itemView.vcc.text = "$vcc"
            }
        }
    }
}

