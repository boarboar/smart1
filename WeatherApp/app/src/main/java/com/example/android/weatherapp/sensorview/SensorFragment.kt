package com.example.android.weatherapp.sensorview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.android.weatherapp.MainActivity
import com.example.android.weatherapp.databinding.FragmentSensorBinding
import com.example.android.weatherapp.domain.Sensor

class SensorFragment : Fragment() {


    /**
     * Inflates the layout with Data Binding, sets its lifecycle owner to the OverviewFragment
     * to enable Data Binding to observe LiveData, and sets up the RecyclerView with an adapter.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = FragmentSensorBinding.inflate(inflater)
        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.setLifecycleOwner(this)
        //binding.sensor = Sensor(777, "Something")
        binding.sensor = SensorFragmentArgs.fromBundle(arguments!!).selectedSensor
        //setHasOptionsMenu(true)
        (activity as MainActivity).toolbarTitle = ""
        return binding.root
    }

    /**
     * Inflates the overflow menu that contains filtering options.
     */

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.overview_menu, menu)
//        super.onCreateOptionsMenu(menu, inflater)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            //R.id.action_settings -> startActivity<SettingsActivity>()
//            R.id.refresh -> viewModel.updateForecast()
//            R.id.populate_sensors -> viewModel.onPopulate()
//            R.id.update_sensors -> viewModel.onUpdate()
//            R.id.delete_sensor_data -> viewModel.onDeleteSensorData()
//        }
//        return true
//    }

}
