package com.example.android.weatherapp.overview

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.android.weatherapp.MainActivity
import com.example.android.weatherapp.R
import com.example.android.weatherapp.databinding.FragmentOverviewBinding


class OverviewFragment : Fragment() {

    /**
     * Lazily initialize our [OverviewViewModel].
     */
    private val viewModel: OverviewViewModel by lazy {
        ViewModelProviders.of(this).get(OverviewViewModel::class.java)
    }

    /**
     * Inflates the layout with Data Binding, sets its lifecycle owner to the OverviewFragment
     * to enable Data Binding to observe LiveData, and sets up the RecyclerView with an adapter.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = FragmentOverviewBinding.inflate(inflater)
        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.setLifecycleOwner(this)
        // Giving the binding access to the OverviewViewModel
        binding.viewModel = viewModel

        binding.weatherForecastList.adapter = ForecastAdapter()
        //binding.weatherForecastList.setHasFixedSize(true)

        binding.sensorsGrid.adapter = SensorAdapter(SensorAdapter.OnClickListener {
            viewModel.displaySensorDetails(it)
        })

        viewModel.navigateToSelectedSensor.observe(this, Observer {
            if ( null != it ) {
                this.findNavController().navigate(OverviewFragmentDirections.actionOverviewFragmentToSensorFragment(it))
                //this.findNavController().navigate(OverviewFragmentDirections.actionOverviewFragmentToSensorFragment(it.id))
                viewModel.displaySensorDetailsComplete()
            }
        })

        binding.sensorsGrid.setHasFixedSize(true)

        setHasOptionsMenu(true)
        (activity as MainActivity).toolbarTitle = ""
        return binding.root
    }

    /**
     * Inflates the overflow menu that contains filtering options.
     */

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.overview_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //R.id.action_settings -> startActivity<SettingsActivity>()
            R.id.refresh -> viewModel.updateForecast()
            R.id.populate_sensors -> viewModel.onPopulate()
            R.id.update_sensors -> viewModel.onUpdate()
            R.id.delete_sensor_data -> viewModel.onDeleteSensorData()
        }
        return true
    }

}
