package com.example.android.weatherapp.logview

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.android.weatherapp.MainActivity
import com.example.android.weatherapp.WeatherApplication
import com.example.android.weatherapp.databinding.FragmentForecastBinding
import com.example.android.weatherapp.databinding.FragmentLogviewBinding
import com.example.android.weatherapp.domain.LogRecord
import com.example.android.weatherapp.forecastview.ForecastExtAdapter
import com.example.android.weatherapp.repository.SensorRepository
import com.example.android.weatherapp.repository.getSensorRepository

class LogviewFragment : Fragment() {

    lateinit var logList : LiveData<List<LogRecord>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentLogviewBinding.inflate(inflater)
        binding.setLifecycleOwner(this)

        logList = getSensorRepository(WeatherApplication.ctx).logList

        Log.w("LOGFRAG", "LOG SZ ==== ${logList.value?.size}")

//        logList.observe (this, Observer {
//            if (null != it) {
//                Log.w("LOGFRAG", "==== READ ${it.size} logs")
//            }
//        }
//        )

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        // Giving the binding access to the OverviewViewModel
        //binding.repository = getSensorRepository(WeatherApplication.ctx)
        binding.model = this

        binding.logList.adapter = LogviewAdapter()

        binding.logList.setHasFixedSize(true)

        (activity as MainActivity).toolbar.setNavigationOnClickListener { activity?.onBackPressed() }

        (activity as MainActivity).toolbarTitle = ""

        return binding.root
    }
}

