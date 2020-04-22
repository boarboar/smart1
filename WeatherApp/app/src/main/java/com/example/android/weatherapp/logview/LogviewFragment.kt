package com.example.android.weatherapp.logview

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
//import androidx.lifecycle.Observer
import com.example.android.weatherapp.MainActivity
//import com.example.android.weatherapp.WeatherApplication
//import com.example.android.weatherapp.databinding.FragmentForecastBinding
import com.example.android.weatherapp.databinding.FragmentLogviewBinding
import com.example.android.weatherapp.domain.LogRecord
//import com.example.android.weatherapp.forecastview.ForecastExtAdapter
//import com.example.android.weatherapp.repository.SensorRepository
import com.example.android.weatherapp.repository.getSensorRepository
import java.util.*
import kotlin.concurrent.schedule

class LogviewFragment : Fragment() {

    private val viewModel: LogViewModel by lazy {
        ViewModelProvider(this).get(LogViewModel::class.java)
    }

    lateinit var binding: FragmentLogviewBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLogviewBinding.inflate(inflater)
        binding.setLifecycleOwner(this)
        val adapter = LogviewAdapter()
        binding.viewModel = viewModel
        binding.logList.adapter = adapter
        binding.logList.setHasFixedSize(true)
        (activity as MainActivity).toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
        (activity as MainActivity).toolbarTitle = ""

        // hack to make log appear at first time
        Timer().schedule(100) {
            getActivity()?.runOnUiThread {
                adapter.notifyDataSetChanged()
                binding.logList.scrollToPosition(0)
            }
        }

        return binding.root
    }
}

class LogViewModel(app: Application) : AndroidViewModel(app) {
    var logList : LiveData<List<LogRecord>>  = getSensorRepository(app).logList
}