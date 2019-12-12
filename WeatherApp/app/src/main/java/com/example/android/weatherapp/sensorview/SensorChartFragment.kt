package com.example.android.weatherapp.sensorview

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.weatherapp.databinding.FragmentSensorchartBinding

class SensorChartFragment : SensorBaseFragment() {

    override val ftag = "Sensor chart frag"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Log.v(ftag, "[ ON VIEW CREATED ]")

        val binding = FragmentSensorchartBinding.inflate(inflater)
        binding.setLifecycleOwner(this)
        //binding.sensor = sensor
        //setHasOptionsMenu(true)

        return binding.root
    }
}


