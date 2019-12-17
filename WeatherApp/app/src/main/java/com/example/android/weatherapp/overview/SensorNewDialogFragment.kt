package com.example.android.weatherapp.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.android.weatherapp.databinding.FragmentSensorNewBinding

class SensorNewDialogFragment : DialogFragment() {

    /**
     * Inflates the layout with Data Binding, sets its lifecycle owner to the OverviewFragment
     * to enable Data Binding to observe LiveData, and sets up the RecyclerView with an adapter.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //val sensor = SensorFragmentArgs.fromBundle(arguments!!).selectedSensor

        val binding = FragmentSensorNewBinding.inflate(inflater)
        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        //binding.setLifecycleOwner(this)

        return binding.root
    }
}