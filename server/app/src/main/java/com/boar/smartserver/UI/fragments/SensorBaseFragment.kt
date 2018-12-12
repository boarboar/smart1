package com.boar.smartserver.UI.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import com.boar.smartserver.presenter.MainPresenter

abstract class SensorBaseFragment : Fragment() {

    protected abstract val ftag : String

    companion object {
        val ARG_SENSOR_ID = "param_idx"
        fun newInstance(fragment: SensorBaseFragment, idx: Int = -1):  SensorBaseFragment {
            val args = Bundle()
            args.putInt(ARG_SENSOR_ID, idx)
            fragment.setArguments(args)
            return fragment
        }
    }

    protected var idx : Int = -1
    protected val presenter : MainPresenter by lazy  { MainPresenter.instance }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        idx = arguments?.getInt(ARG_SENSOR_ID) ?: -1
        Log.v(ftag, "[ ON BASE FRAG CREATE ]")
    }

    override fun onStart() {
        super.onStart()
        Log.v(ftag, "[ ON BASE FRAG START ]")
    }

    override fun onResume() {
        super.onResume()
        Log.v(ftag, "[ ON BASE FRAG RESUME ]")
    }

}
