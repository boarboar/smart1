package com.boar.smartserver.UI

import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
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
import com.boar.smartserver.receiver.MainServiceReceiver
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

    private var sensors = SensorList()

    val receiver: MainServiceReceiver = MainServiceReceiver()

    //private lateinit var sensors

    private var service: MainService? = null
    var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            service = null
            isBound = false
            Log.v(tag, "[ SRV ONBOUND ]")
            //synchronize.enabled = false
        }
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            if (binder is MainService.MainServiceBinder) {
                service = binder.getService()
                service?.let {
                    isBound = true
                    loadSensors()
                    //synchronize.enabled = true
                    Log.v(tag, "[ SRV BOUND ]")
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
                R.id.action_add -> service?.addSensor()
                R.id.action_sim -> runSimulation()
                else -> toast("Unknown option")
            }
        }


        val intent = Intent(this, MainService::class.java)
        bindService(intent, serviceConnection,  android.content.Context.BIND_AUTO_CREATE)

        val filter = IntentFilter()
        filter.addAction("com.example.Broadcast")
        registerReceiver(receiver, filter)

    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
        unregisterReceiver(receiver)
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
            service?.getSensors()?.let { sensors = it}
            //Log.v(tag, "Sensors : $sensors")

            uiThread {
                updateUI()
            }

        }
        //updateUI()
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

        //Log.v(tag, "Sensors updated")

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
