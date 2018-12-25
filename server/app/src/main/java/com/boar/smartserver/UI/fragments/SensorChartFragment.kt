package com.boar.smartserver.UI.fragments


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.boar.smartserver.R
import com.boar.smartserver.UI.DateUtils
import com.boar.smartserver.domain.SensorHistory
import com.boar.smartserver.draw.DrawView
import kotlinx.android.synthetic.main.fragment_sensor_chart.view.*

class SensorChartFragment : SensorBaseFragment() {

    override val ftag = "Chart frag"

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Log.v(ftag, "[ ON VIEW CREATED ]")
        val view = inflater?.inflate(R.layout.fragment_sensor_chart, container, false)
        if (idx != -1) {
            presenter.getSensor(idx)?.apply {
                view.draw_view.sensHist = presenter.getSensorHistory(id.toInt())
            }
            view.draw_view.disp =  if(view.radio_temp.isChecked) DrawView.DispType.TEMPERATURE
            else DrawView.DispType.VCC
        }
        view.radio_temp.setOnClickListener { update() }
        view.radio_vcc.setOnClickListener { update() }
        return view
    }

    fun update() {

        val mview = view

        if(mview==null) return

        mview.draw_view.disp =  if(mview.radio_temp.isChecked) DrawView.DispType.TEMPERATURE
            else DrawView.DispType.VCC
        mview.draw_view.invalidate()
    }
}
