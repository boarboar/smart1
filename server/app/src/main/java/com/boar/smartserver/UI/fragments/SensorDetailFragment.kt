package com.boar.smartserver.UI.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.boar.smartserver.R
import com.boar.smartserver.UI.DateUtils
import com.boar.smartserver.presenter.MainPresenter
import kotlinx.android.synthetic.main.activity_sensor_details.*
import kotlinx.android.synthetic.main.activity_sensor_details.view.*

class SensorDetailFragment : Fragment() {

    companion object {
        val ARG_SENSOR_ID = "param_idx"
        fun newInstance(page: Int): SensorDetailFragment {
            val args = Bundle()
            args.putInt(ARG_SENSOR_ID, page)
            val fragment = SensorDetailFragment()
            fragment.setArguments(args)
            return fragment
        }
    }

    private var idx : Int = -1
    protected val presenter : MainPresenter by lazy  { MainPresenter.instance }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        idx = arguments?.getInt(ARG_SENSOR_ID) ?: -1
    }


    //val tag = "Sensor Detail fragment"
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
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
                //currentIdx = idx
            }
        }
        return view
    }
}
