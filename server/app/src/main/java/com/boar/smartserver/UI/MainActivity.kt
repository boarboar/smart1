package com.boar.smartserver.UI

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.util.Log
import com.boar.smartserver.R
import com.boar.smartserver.domain.Sensor
import com.boar.smartserver.domain.SensorList
import com.boar.smartserver.domain.SensorMeasurement
import com.boar.smartserver.service.MainService
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.util.ArrayList

//import org.jetbrains.anko.startActivity

class MainActivity : BaseActivity(), ToolbarManager {
    override val tag = "Main activity"
    override val toolbar by lazy { find<Toolbar>(R.id.toolbar) }

    override fun getLayout() = R.layout.activity_main
    override fun getActivityTitle() = R.string.app_name

    private val sensors = SensorList()


    private var service: MainService? = null
    var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            service = null
            isBound = false
            //synchronize.enabled = false
        }
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            if (binder is MainService.MainServiceBinder) {
                service = binder.getService()
                service?.let {
                    isBound = true
                    //synchronize.enabled = true
                }
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Log.v(tag, "[ ON CREATE - CHECK]")

        sensorList.layoutManager = LinearLayoutManager(this)

        initToolbar {
            when (it) {
                //R.id.action_settings -> startActivity<SettingsActivity>()
                R.id.action_sim -> runSimulation()
                else -> toast("Unknown option")
            }
        }

        loadSensors()

        val intent = Intent(this, MainService::class.java)
        bindService(intent, serviceConnection,  android.content.Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }

/*
    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        //unbindService(serviceConnection)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }
*/

    private fun loadSensors() {
        doAsync {
            sensors.add(Sensor(1, "Window"))
            sensors.add(Sensor(2, "Balcony"))

        }
        updateUI()
    }

    private fun updateUI() {
        val adapter = SensorListAdapter(sensors) {
            /*
            startActivity<DetailActivity>(DetailActivity.ID to it.id,
                    DetailActivity.CITY_NAME to weekForecast.city)
                    */
        }

        sensorList.adapter = adapter
        toolbarTitle = "Updated"

        Log.v(tag, "Sensors updated")

        //runSimulation()

    }

    private fun runSimulation() {
        Log.v(tag, "Start simulation")
        doAsync {
            while(true) {
                Thread.sleep(5_000)
                val idx = sensors.simulate()
                if (idx!=-1) {
                    Log.v(tag, "Siimulated : idx=$idx")
                    uiThread {
                        sensorList.adapter?.notifyItemChanged(idx)
                    }
                }
            }
        }
    }
}
