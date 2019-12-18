package com.example.android.weatherapp.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.android.weatherapp.R
import com.example.android.weatherapp.databinding.FragmentSensorNewBinding
import com.example.android.weatherapp.sensorview.SensorFragmentArgs

class SensorNewDialogFragment : DialogFragment() {

    var nextId = 0
        /**
     * Inflates the layout with Data Binding, sets its lifecycle owner to the OverviewFragment
     * to enable Data Binding to observe LiveData, and sets up the RecyclerView with an adapter.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        nextId = SensorNewDialogFragmentArgs.fromBundle(arguments!!).id

        val binding = FragmentSensorNewBinding.inflate(inflater)
        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        //binding.setLifecycleOwner(this)

        return binding.root
    }

    override fun onViewCreated(view:View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //binding.id= 777
        val editId = view.findViewById<EditText>(R.id.id)
        val btnDone = view.findViewById<Button>(R.id.done)
        editId.setText(nextId)
        btnDone.setOnClickListener {
            dismiss()
        }
    }
}