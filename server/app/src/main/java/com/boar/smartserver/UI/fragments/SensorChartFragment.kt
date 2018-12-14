package com.boar.smartserver.UI.fragments


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.boar.smartserver.R
import com.boar.smartserver.UI.DateUtils
import com.boar.smartserver.domain.SensorHistory
import kotlinx.android.synthetic.main.fragment_sensor_chart.view.*

class SensorChartFragment : SensorBaseFragment() {

    override val ftag = "Chart frag"
    var sensHist : List<SensorHistory> = listOf()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Log.v(ftag, "[ ON VIEW CREATED ]")
        val view = inflater?.inflate(R.layout.fragment_sensor_chart, container, false)
        if (idx != -1) {
            presenter.getSensor(idx)?.apply {
                view.sensor_id.text = id.toString()
                sensHist = presenter.getSensorHistory(id.toInt())
            }
        }
        return view
    }
}
