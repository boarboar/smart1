package com.boar.smartserver.UI

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AlertDialog
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
import kotlinx.android.synthetic.main.sensor_prop.*
import org.jetbrains.anko.*
import java.text.DateFormat
import java.util.*
import kotlin.concurrent.schedule

import java.text.SimpleDateFormat


//import org.jetbrains.anko.startActivity




class MainActivity : BaseActivity(), ToolbarManager {

    companion object {
        val df_time = SimpleDateFormat("HH:mm")
        val df_date = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
        var df_dt = DateFormat.getDateTimeInstance(
                DateFormat.SHORT, DateFormat.SHORT, Locale.GERMAN)
    }

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
        initUI()
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

    //initUI

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

    private fun initUI() {
        sensorList.layoutManager = LinearLayoutManager(this)
        sensorList.setHasFixedSize(true)   // all items of the same size (?)

        initToolbar {
            when (it) {
                R.id.action_settings -> startActivity<SettingsActivity>()
                R.id.action_add -> showSensorPropUI()
                R.id.action_sim_start -> service?.runSimulation()
                R.id.action_sim_stop -> service?.stopSimulation()
                else -> toast("Unknown option")
            }
        }

        Timer().schedule(1000, 1000){
            runOnUiThread { toolbarTitle = df_dt.format(System.currentTimeMillis()) }
        }
    }

    private fun loadSensors() {
        pBar.visibility = View.VISIBLE
        doAsync {
            service?.getSensors()?.let { sensors = it}

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
        pBar.visibility = View.GONE
    }

    private fun showSensorPropUI() {
        val sensDlg = SensorPropDialog(this)

        sensDlg.create().onCancel{
            Log.v(tag, "DCLOSE")
        }.onDone{
            Log.v(tag, "DOK ${sensDlg.sensorId.text} : ${sensDlg.sensorLoc.text}")
        }
        .show()
    }
}
