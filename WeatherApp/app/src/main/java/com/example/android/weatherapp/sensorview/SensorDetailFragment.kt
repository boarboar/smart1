package com.example.android.weatherapp.sensorview

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.android.weatherapp.MainActivity
import com.example.android.weatherapp.R
import com.example.android.weatherapp.common_ui.SensorPropDialog
import com.example.android.weatherapp.databinding.FragmentSensordetailBinding
import com.example.android.weatherapp.repository.getSensorRepository

class SensorDetailFragment : SensorBaseFragment() {
    override val ftag = "Detail frag"
    private val viewModel: SensorDetailViewModel by lazy {
        val viewModelFactory = SensorDetailViewModelFactory(sensorId, (activity as MainActivity).application)
        ViewModelProvider(this, viewModelFactory).get(SensorDetailViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        //Log.v(ftag, "[ ON VIEW CREATED ]")

        val binding = FragmentSensordetailBinding.inflate(inflater)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel
        setHasOptionsMenu(true)
        return binding.root
    }

    /**
     * Inflates the overflow menu that contains filtering options.
     */

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.sensor_detail_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete_sensor -> {
                val dialog = DeleteSensorDialogFragment(getSensorRepository(activity as Context).currentSensor.value?.description ?: "") {
                    viewModel.onDeleteSensor()
                    activity?.onBackPressed()
                }
                //dialog.show(fragmentManager!!, "DELETE_SENSOR_ALERT")
                dialog.show(requireActivity().supportFragmentManager, "DELETE_SENSOR_ALERT")
            }
            R.id.edit_sensor -> {
                val sensDlg = SensorPropDialog(activity as Context, getSensorRepository(activity as Context).currentSensor.value, true)
                sensDlg.create().onCancel{
                }.onDone{
                    if(!it.validate()) {
                        Toast.makeText(activity, "Check sensor data", Toast.LENGTH_LONG).show()
                        false
                    } else {
                        viewModel.onUpdateSensor(it)
                        true
                    }
                }
                    .show()
            }
            else -> return false
        }
        return true
    }

    class DeleteSensorDialogFragment(val sensorName : String, val action : () -> Unit) : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog { // Use the Builder class for convenient dialog construction
            val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
            builder.setMessage(getString(R.string.dialog_sensor_delete, sensorName))
                .setPositiveButton(R.string.yes) { dialog, id -> action() }
                .setNeutralButton(R.string.no) { dialog, id ->  }

            // Create the AlertDialog object and return it
            return builder.create()
        }
    }


}
