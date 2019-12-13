package com.example.android.weatherapp.sensorview

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.example.android.weatherapp.MainActivity
import com.example.android.weatherapp.databinding.FragmentSensorlogBinding

class SensorLogFragment : SensorBaseFragment() {

    override val ftag = "Sensor log frag"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Log.v(ftag, "[ ON VIEW CREATED ]")

        val binding = FragmentSensorlogBinding.inflate(inflater)
        binding.setLifecycleOwner(this)

        val viewModelFactory = SensorLogViewModelFactory(sensorId, (activity as MainActivity).application)

        binding.viewModel = ViewModelProviders.of(
            this, viewModelFactory).get(SensorLogViewModel::class.java)

        binding.sensorLogList.adapter = SensorDataAdapter()

        //setHasOptionsMenu(true)

        return binding.root
    }
}


