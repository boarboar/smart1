package com.example.android.weatherapp.forecastview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.android.weatherapp.MainActivity
import com.example.android.weatherapp.WeatherApplication.Companion.ctx
import com.example.android.weatherapp.databinding.FragmentForecastBinding
import com.example.android.weatherapp.overview.ForecastAdapter
import com.example.android.weatherapp.repository.getSensorRepository

class ForecastFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentForecastBinding.inflate(inflater)
        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.setLifecycleOwner(this)
        // Giving the binding access to the OverviewViewModel
        binding.repository = getSensorRepository(ctx)

        binding.weatherForecastList.adapter = ForecastExtAdapter(12)

        binding.weatherForecastList.setHasFixedSize(true)

        (activity as MainActivity).toolbarTitle = ""
        return binding.root
    }
}

