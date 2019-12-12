package com.example.android.weatherapp.sensorview

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.example.android.weatherapp.MainActivity
import com.example.android.weatherapp.databinding.FragmentSensordetailBinding

class SensorDetailFragment : SensorBaseFragment() {

    override val ftag = "Detail frag"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Log.v(ftag, "[ ON VIEW CREATED ]")

        val binding = FragmentSensordetailBinding.inflate(inflater)
        binding.setLifecycleOwner(this)
        //binding.sensor = sensor
        // replace with repository and access currentSensor !!!

        val viewModelFactory = SensorDetailViewModelFactory(sensorId, (activity as MainActivity).application)

        binding.viewModel = ViewModelProviders.of(
            this, viewModelFactory).get(SensorDetailViewModel::class.java)

        //setHasOptionsMenu(true)

        return binding.root
    }
}
