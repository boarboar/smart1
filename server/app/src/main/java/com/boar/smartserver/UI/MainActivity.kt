package com.boar.smartserver.UI

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import com.boar.smartserver.R
import com.boar.smartserver.domain.Sensor
import com.boar.smartserver.domain.SensorList
import com.boar.smartserver.domain.SensorMeasurement
import com.boar.smartserver.network.WeatherServiceApi
import com.boar.smartserver.presenter.MainPresenter
import com.boar.smartserver.receiver.MainServiceReceiver
import com.boar.smartserver.service.MainService
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.sensor_prop.*
import kotlinx.android.synthetic.main.weather.*
import kotlinx.android.synthetic.main.weather.view.*
import org.jetbrains.anko.*
import java.text.DateFormat
import java.util.*
import kotlin.concurrent.schedule

import java.text.SimpleDateFormat


//import org.jetbrains.anko.startActivity

class MainActivity : BaseActivity(), ToolbarManager {

    companion object {
        /*
        private val df_time = SimpleDateFormat("HH:mm")
        private val df_date = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
        private var df_dt = DateFormat.getDateTimeInstance(
                DateFormat.SHORT, DateFormat.SHORT, Locale.GERMAN)
                */
        private val picasso : Picasso by lazy {
            Picasso.get()
        }
        val presenter : MainPresenter by lazy  { MainPresenter() }
    }

    override val tag = "Main activity"
    override val toolbar by lazy { find<Toolbar>(R.id.toolbar) }
    val pBar by lazy { find<ProgressBar>(R.id.pBar) }

    override fun getLayout() = R.layout.activity_main
    override fun getActivityTitle() = R.string.app_name

    //private var sensors = SensorList()
    private var lastUpdated = 0L

    private val receiver: MainServiceReceiver = MainServiceReceiver {op, idx ->
        when (op) {
            // MainService.BROADCAST_EXTRAS_OP_LOAD -> loadSensors()
            MainService.BROADCAST_EXTRAS_OP_ADD -> sensorList.adapter?.notifyItemInserted(idx)
            MainService.BROADCAST_EXTRAS_OP_UPD -> {
                lastUpdated = System.currentTimeMillis()
                sensorList.adapter?.notifyItemChanged(idx)
            }
            else -> Log.v(tag, "[ BRDCST $op $idx]")
        }
    }

    //private lateinit var sensors

    private var service: MainService? = null
    private var isBound = false

    //private val presenter : MainPresenter by lazy  { MainPresenter(this) }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            service = null
            isBound = false
            presenter.detachService()
            Log.v(tag, "[ SRV ONBOUND ]")
            //synchronize.enabled = false
        }
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            if (binder is MainService.MainServiceBinder) {
                service = binder.getService()
                service?.let {
                    isBound = true
                    presenter.attachService(service)
                    //loadSensors()
                    updateUI()
                    Log.v(tag, "[ SRV BOUND ]")
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN) // request full screen
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)  // request keeps on
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

        initToolbar(R.menu.menu_main) {
            when (it) {
                R.id.action_settings -> startActivity<SettingsActivity>()
                R.id.action_add -> showSensorPropUI()
                R.id.action_sim_start -> service?.runSimulation()
                R.id.action_sim_stop -> service?.stopSimulation()
                else -> toast("Unknown option")
            }
        }

        Timer().schedule(100, 1000){// Time
            val showUpdated = if(System.currentTimeMillis() < lastUpdated + 5000) "U" else ""
            runOnUiThread { toolbarTitle = "${DateUtils.convertDateTime(System.currentTimeMillis())} $showUpdated" }
        }

        Timer().schedule(100, 1000*60*15){// Weather
            presenter.refreshWeather {
                //val iconpng = picasso.load("http://openweathermap.org/img/w/${it.weather[0].iconCode}.png") // is it lazy?
                val iconpng = picasso.load(MainPresenter.iconToUrl(it)) // is it lazy?
                runOnUiThread {
                    weather_city.text = "${it.name}"
                    weather_now_temp.text = "${it.main.temp}º"
                    humidity.text = "${it.main.humidity} %"
                    pressure.text = "${it.main.pressure} kPa"
                    iconpng.into(icon)

                    //.error(R.drawable.user_image).resize(110, 110).centerCrop()
                    //setIndicatorsEnabled(true) // to show if cached
                }
            }
        }
    }
/*
    private fun loadSensors() {
        pBar.visibility = View.VISIBLE
        doAsync {
            service?.sensors?.let {
                sensors = it
                uiThread {
                    updateUI()
                }
            }
        }
        //pBar.visibility = View.GONE
    }
*/

    private fun updateUI() {
        // val adapter = SensorListAdapter(sensors) {
        //val adapter = SensorListAdapter(service) {
        val adapter = SensorListAdapter(presenter) {
            startActivity<SensorDetailActivity>(
                    //SensorDetailActivity.LOCATION to it.description
                    SensorDetailActivity.IDX to it
                   )
        }
        sensorList.adapter = adapter
        pBar.visibility = View.GONE
    }

    private fun showSensorPropUI() {
        val sensDlg = SensorPropDialog(this, Sensor(77, "New"))

        sensDlg.create().onCancel{
            Log.v(tag, "DCLOSE")
        }.onDone{
            if(!it.validate()) {
                Toast.makeText(this, "Check data", Toast.LENGTH_LONG).show()
                false
            } else {
                Log.v(tag, "DOK ${it}")
                service?.addSensor(it)
                true
            }
        }
        .show()
    }
}
