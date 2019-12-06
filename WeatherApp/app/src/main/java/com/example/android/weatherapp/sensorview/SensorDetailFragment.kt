package com.example.android.weatherapp.sensorview

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.android.weatherapp.databinding.FragmentSensorBinding
import com.example.android.weatherapp.databinding.FragmentSensordetailBinding
import com.example.android.weatherapp.domain.Sensor

class SensorDetailFragment : SensorBaseFragment() {

    override val ftag = "Detail frag"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Log.v(ftag, "[ ON VIEW CREATED ]")

        val binding = FragmentSensordetailBinding.inflate(inflater)
        binding.setLifecycleOwner(this)
        binding.sensor = sensor
        //setHasOptionsMenu(true)

        return binding.root
    }
}

//class SensorDetailFragment : Fragment() {
//
//    companion object {
//        val ftag = "Detail frag"
//        val ARG_SENSOR = "param_sensor"
//        fun newInstance(fragment: SensorDetailFragment, sensor: Sensor):  SensorDetailFragment {
//            val args = Bundle()
//            args.putParcelable(ARG_SENSOR, sensor)
//            fragment.setArguments(args)
//            return fragment
//        }
//    }
//
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
//    ): View? {
//        Log.v(ftag, "[ ON VIEW CREATED ]")
//
//        val binding = FragmentSensordetailBinding.inflate(inflater)
//        binding.setLifecycleOwner(this)
//        //binding.sensor = Sensor(777, "Something")
//        binding.sensor = arguments?.getParcelable(SensorBaseFragment.ARG_SENSOR) ?: null
//        //setHasOptionsMenu(true)
//
//        return binding.root
//    }
//}


