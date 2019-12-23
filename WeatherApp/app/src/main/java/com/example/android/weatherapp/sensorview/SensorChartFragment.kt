package com.example.android.weatherapp.sensorview

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.android.weatherapp.MainActivity
import com.example.android.weatherapp.databinding.FragmentSensorchartBinding

class SensorChartFragment : SensorBaseFragment() {

    override val ftag = "Sensor chart frag"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Log.v(ftag, "[ ON VIEW CREATED ]")

        val binding = FragmentSensorchartBinding.inflate(inflater)
        binding.setLifecycleOwner(this)
        val viewModelFactory = SensorChartViewModelFactory(sensorId, (activity as MainActivity).application)

        val viewModel = ViewModelProviders.of(
            this, viewModelFactory).get(SensorChartViewModel::class.java)

        binding.viewModel = viewModel

        viewModel.chartDispType.observe(this, Observer {
            binding.drawView.disp = it
            binding.drawView.prepare()
            binding.drawView.invalidate()
        })

        viewModel.chartDispPeriod.observe(this, Observer {
            binding.drawView.dispPeriod = it
            binding.drawView.prepare()
            binding.drawView.invalidate()
        })

        return binding.root
    }

}


