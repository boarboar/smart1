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
        //val ARG_SENSOR = "param_sensor"
        val ARG_SENSOR_ID = "param_sensor_id"
        fun newInstance(fragment: SensorBaseFragment, sensor: Sensor):  SensorBaseFragment {
            val args = Bundle()
            //args.putParcelable(ARG_SENSOR, sensor)
            args.putInt(ARG_SENSOR_ID, sensor.id.toInt())
            fragment.setArguments(args)
            return fragment
        }
    }

    protected var sensorId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorId =  arguments?.getInt(ARG_SENSOR_ID) ?: 0
    }

}
