package com.example.android.weatherapp.sensorview

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.lifecycle.ViewModelProviders
import com.example.android.weatherapp.MainActivity
import com.example.android.weatherapp.R
import com.example.android.weatherapp.databinding.FragmentSensordetailBinding

class SensorDetailFragment : SensorBaseFragment() {

    override val ftag = "Detail frag"

    private val viewModel: SensorDetailViewModel by lazy {
        val viewModelFactory = SensorDetailViewModelFactory(sensorId, (activity as MainActivity).application)
        ViewModelProviders.of(
            this, viewModelFactory).get(SensorDetailViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Log.v(ftag, "[ ON VIEW CREATED ]")

        val binding = FragmentSensordetailBinding.inflate(inflater)
        binding.setLifecycleOwner(this)

        binding.viewModel = viewModel

        setHasOptionsMenu(true)

        return binding.root
    }

    /**
     * Inflates the overflow menu that contains filtering options.
     */
/*
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.sensor_detail_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete_sensor_data -> viewModel.onDeleteSensor()
        }
        return true
    }
    */

}
