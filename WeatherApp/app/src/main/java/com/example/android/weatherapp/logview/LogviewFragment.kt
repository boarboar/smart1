package com.example.android.weatherapp.logview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.android.weatherapp.MainActivity
import com.example.android.weatherapp.WeatherApplication
import com.example.android.weatherapp.databinding.FragmentForecastBinding
import com.example.android.weatherapp.databinding.FragmentLogviewBinding
import com.example.android.weatherapp.forecastview.ForecastExtAdapter
import com.example.android.weatherapp.repository.getSensorRepository

class LogviewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentLogviewBinding.inflate(inflater)
        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.setLifecycleOwner(this)
        // Giving the binding access to the OverviewViewModel
        binding.repository = getSensorRepository(WeatherApplication.ctx)

        binding.logList.adapter = LogviewAdapter()

        binding.logList.setHasFixedSize(true)

        (activity as MainActivity).toolbarTitle = ""
        return binding.root
    }
}

