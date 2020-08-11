package com.example.android.weatherapp.logview

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.lifecycle.Observer
//import androidx.lifecycle.Observer
import com.example.android.weatherapp.MainActivity
//import com.example.android.weatherapp.WeatherApplication
//import com.example.android.weatherapp.databinding.FragmentForecastBinding
import com.example.android.weatherapp.databinding.FragmentLogviewBinding
import com.example.android.weatherapp.domain.LogRecord
import com.example.android.weatherapp.draw.DrawView
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

        viewModel.filterNotify.observe(viewLifecycleOwner, Observer {
            if(it) {
                val adapter = LogviewAdapter()
                binding.viewModel = viewModel
                binding.logList.adapter = adapter
                binding.logList.setHasFixedSize(true)

                Timer().schedule(100) {
                    getActivity()?.runOnUiThread {
                        adapter.notifyDataSetChanged()
                    }
                }
                viewModel.onFilterNotify()
            }
        })

        return binding.root
    }
}

class LogViewModel(app: Application) : AndroidViewModel(app) {
    //var logList : LiveData<List<LogRecord>>  = getSensorRepository(app).logList
    var logList : LiveData<List<LogRecord>>  = getSensorRepository(app).getFilteredLog(LogRecord.SEVERITY_CODE.INFO)
    private val _filterNotify = MutableLiveData<Boolean>()
    val filterNotify : MutableLiveData<Boolean>
        get() = _filterNotify
    private var _severity = LogRecord.SEVERITY_CODE.INFO
    private var _interval_hr = 1

    fun onFilterNotify() {
        _filterNotify.value = false
    }

    private fun _refresh() {
        logList  = getSensorRepository(getApplication()).getFilteredLog(_severity, _interval_hr)
        _filterNotify.value = true
    }

    fun onFilterSeverityErr() {
        _severity = LogRecord.SEVERITY_CODE.ERROR
        _refresh()
        _filterNotify.value = true
    }
    fun onFilterSeverityAll() {
        _severity = LogRecord.SEVERITY_CODE.INFO
        _refresh()
        _filterNotify.value = true
    }
    fun onFilterIntervalHour() {
        _interval_hr = 1
        _refresh()
        _filterNotify.value = true
    }
    fun onFilterIntervalDay() {
        _interval_hr = 24
        _refresh()
        _filterNotify.value = true
    }
    fun onFilterIntervalAll() {
        _interval_hr = -1
        _refresh()
        _filterNotify.value = true
    }
}