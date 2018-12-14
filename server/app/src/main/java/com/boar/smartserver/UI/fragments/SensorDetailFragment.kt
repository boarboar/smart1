package com.boar.smartserver.UI.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.boar.smartserver.R
import com.boar.smartserver.UI.DateUtils
import kotlinx.android.synthetic.main.fragment_sensor_details.view.*

class SensorDetailFragment : SensorBaseFragment() {

    override val ftag = "Detail frag"

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Log.v(ftag, "[ ON VIEW CREATED ]")
        val view = inflater?.inflate(R.layout.fragment_sensor_details, container, false)
        if (idx != -1) {
            presenter.getSensor(idx)?.apply {
                view.sensor_id.text = id.toString()
                view.sensor_temperature.text = "${temperatureAsString}º"
                view.sensor_vcc.text = "${vccAsString} v"
                view.sensor_res.text = "${resolution}"
                view.sensor_model.text = "${model}"
                view.sensor_par.text = "${parasite}"
                view.sensor_last_updated.text = if (updated != 0L) DateUtils.convertTime(updated) else "--:--:--"
                view.sensor_last_valid.text = if (lastValidMeasTime != 0L) DateUtils.convertTime(lastValidMeasTime) else "--:--:--"
                view.sensor_msg.text = msg
            }
        }
        return view
    }
}
