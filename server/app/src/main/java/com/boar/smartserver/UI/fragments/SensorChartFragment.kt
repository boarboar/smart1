package com.boar.smartserver.UI.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.boar.smartserver.R
import com.boar.smartserver.UI.DateUtils
import kotlinx.android.synthetic.main.activity_sensor_details.view.*

class SensorChartFragment : SensorBaseFragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater?.inflate(R.layout.fragment_sensor_chart, container, false)
        if (idx != -1) {
            presenter.getSensor(idx)?.apply {
                view.sensor_id.text = id.toString()
            }
        }
        return view
    }
}
