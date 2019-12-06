package com.example.android.weatherapp.sensorview

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.android.weatherapp.databinding.FragmentSensorBinding
import com.example.android.weatherapp.domain.Sensor

abstract class SensorBaseFragment : Fragment() {

    protected abstract val ftag : String

    companion object {
        val ARG_SENSOR = "param_sensor"
        fun newInstance(fragment: SensorBaseFragment, sensor: Sensor):  SensorBaseFragment {
            val args = Bundle()
            args.putParcelable(ARG_SENSOR, sensor)
            fragment.setArguments(args)
            return fragment
        }
    }

    protected var sensor : Sensor? = null
    //protected val presenter : MainPresenter by lazy  { MainPresenter.instance }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensor = arguments?.getParcelable(ARG_SENSOR) ?: null
        Log.v(ftag, "[ ON BASE FRAG CREATE ]")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.v(ftag, "[ ON SAVE INST STATE ]")
    }

    override fun onStart() {
        super.onStart()
        Log.v(ftag, "[ ON BASE FRAG START ]")
    }

    override fun onPause() {
        super.onPause()
        Log.v(ftag, "[ ON BASE FRAG PAUSE ]")
    }

    override fun onResume() {
        super.onResume()
        Log.v(ftag, "[ ON BASE FRAG RESUME ]")
    }

}
