package com.example.android.weatherapp.sensorview

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.android.weatherapp.MainActivity
import com.example.android.weatherapp.databinding.FragmentSensorBinding
import com.example.android.weatherapp.domain.Sensor
import com.example.android.weatherapp.repository.getSensorRepository
import kotlinx.android.synthetic.main.fragment_sensor.*

class SensorFragment : Fragment() {
    companion object {
        const val tag = "SensorFragment"
    }

    /**
     * Inflates the layout with Data Binding, sets its lifecycle owner to the OverviewFragment
     * to enable Data Binding to observe LiveData, and sets up the RecyclerView with an adapter.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        Log.v(SensorFragment.tag, "[ ON CREATE ]")
        val sensor = SensorFragmentArgs.fromBundle(arguments!!).selectedSensor
        (activity as MainActivity).sensorDescr = sensor.description

        val binding = FragmentSensorBinding.inflate(inflater)
        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.setLifecycleOwner(this)
        binding.viewpager.adapter = SensorPagerAdapter(childFragmentManager, sensor)
        binding.sensorTabs.setupWithViewPager(binding.viewpager)

        (activity as MainActivity).toolbar.setNavigationOnClickListener { activity?.onBackPressed() }

        return binding.root
    }

    override fun onPause() {
        (activity as MainActivity).sensorDescr = null
            super.onPause()
        //Log.v(SensorFragment.tag, "[ ON PAUSE ]")
    }

    override fun onDestroy() {
        super.onDestroy()
        //Log.v(SensorFragment.tag, "[ ON DESTROY ]")
    }

}
