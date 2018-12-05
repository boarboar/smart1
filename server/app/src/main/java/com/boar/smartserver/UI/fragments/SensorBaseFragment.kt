package com.boar.smartserver.UI.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import com.boar.smartserver.presenter.MainPresenter

abstract class SensorBaseFragment : Fragment() {

    companion object {
        val ARG_SENSOR_ID = "param_idx"
        fun newInstance(fragment: SensorBaseFragment, page: Int):  SensorBaseFragment {
            val args = Bundle()
            args.putInt(ARG_SENSOR_ID, page)
            fragment.setArguments(args)
            return fragment
        }
    }

    protected var idx : Int = -1
    protected val presenter : MainPresenter by lazy  { MainPresenter.instance }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        idx = arguments?.getInt(ARG_SENSOR_ID) ?: -1
    }

    //val tag = "Sensor Detail fragment"

}
