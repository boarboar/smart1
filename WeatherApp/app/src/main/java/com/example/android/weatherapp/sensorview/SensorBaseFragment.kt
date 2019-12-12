package com.example.android.weatherapp.sensorview

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android.weatherapp.MainActivity
import com.example.android.weatherapp.databinding.FragmentSensorBinding
import com.example.android.weatherapp.domain.Sensor
import com.example.android.weatherapp.repository.getSensorRepository

abstract class SensorBaseFragment : Fragment() {

    protected abstract val ftag : String

    companion object {
        val ARG_SENSOR = "param_sensor"
        val ARG_SENSOR_ID = "param_sensor_id"
        fun newInstance(fragment: SensorBaseFragment, sensor: Sensor):  SensorBaseFragment {
            val args = Bundle()
            //args.putParcelable(ARG_SENSOR, sensor)
            args.putInt(ARG_SENSOR_ID, sensor.id.toInt())
            fragment.setArguments(args)
            return fragment
        }
    }

    //protected var sensor : Sensor? = null
    //lateinit protected var sensor: LiveData<Sensor>

    protected var sensorId: Int = 0

    // repository, not sensor!!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //sensor = arguments?.getParcelable(ARG_SENSOR) ?: null
        /*
        sensor = arguments?.let{
            getSensorRepository((activity as MainActivity)).getOneSensor(it.getInt(ARG_SENSOR_ID))
        } ?: MutableLiveData<Sensor>()
        */
        sensorId =  arguments?.getInt(ARG_SENSOR_ID) ?: 0
        //Log.v(ftag, "[ ON BASE FRAG CREATE ]")
    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        Log.v(ftag, "[ ON SAVE INST STATE ]")
//    }
//
//    override fun onStart() {
//        super.onStart()
//        Log.v(ftag, "[ ON BASE FRAG START ]")
//    }
//
//    override fun onPause() {
//        super.onPause()
//        Log.v(ftag, "[ ON BASE FRAG PAUSE ]")
//    }
//
//    override fun onResume() {
//        super.onResume()
//        Log.v(ftag, "[ ON BASE FRAG RESUME ]")
//    }

}
