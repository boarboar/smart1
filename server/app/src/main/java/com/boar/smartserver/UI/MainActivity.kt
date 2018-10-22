package com.boar.smartserver.UI

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
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
    val pBar by lazy { find<ProgressBar>(R.id.pBar) }

    override fun getLayout() = R.layout.activity_main
    override fun getActivityTitle() = R.string.app_name

    private var sensors = SensorList()

    private val receiver: MainServiceReceiver = MainServiceReceiver {op, idx ->
        when (op) {
            MainService.BROADCAST_EXTRAS_OP_ADD -> Toast.makeText(this, "ADD $idx", Toast.LENGTH_LONG).show()
            MainService.BROADCAST_EXTRAS_OP_UPD -> sensorList.adapter?.notifyItemChanged(idx)
            else -> Log.v(tag, "[ BRDCST $op $idx]")
        }
    }

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
    }

    override fun onDestroy() {
        super.onDestroy()
    }

/*
    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        //unbindService(serviceConnection)
    }
*/
    override fun onResume() {
        super.onResume()
    sensorList.layoutManager = LinearLayoutManager(this)

    initToolbar {
        when (it) {
            //R.id.action_settings -> startActivity<SettingsActivity>()
            R.id.action_add -> service?.addSensor()
            R.id.action_sim -> service?.runSimulation()
            else -> toast("Unknown option")
        }
    }


    val intent = Intent(this, MainService::class.java)
    bindService(intent, serviceConnection,  android.content.Context.BIND_AUTO_CREATE)

    val filter = IntentFilter()
    filter.addAction(MainService.BROADCAST_ACTION)
    registerReceiver(receiver, filter)

}

    override fun onPause() {
        super.onPause()
        unbindService(serviceConnection)
        unregisterReceiver(receiver)
    }


    private fun loadSensors() {
        pBar.visibility = View.VISIBLE
        doAsync {
            service?.getSensors()?.let { sensors = it}
            //Log.v(tag, "Sensors : $sensors")

            uiThread {
                updateUI()
            }

        }
        //pBar.visibility = View.GONE
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
        pBar.visibility = View.GONE

        //Log.v(tag, "Sensors updated")

    }
}
