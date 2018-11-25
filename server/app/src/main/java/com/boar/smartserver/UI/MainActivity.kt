package com.boar.smartserver.UI

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import com.boar.smartserver.R
import com.boar.smartserver.domain.Sensor
import com.boar.smartserver.presenter.MainPresenter
import com.boar.smartserver.receiver.MainServiceReceiver
import com.boar.smartserver.service.MainService
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.weather.*
import kotlinx.android.synthetic.main.weather.view.*
import org.jetbrains.anko.*
import java.util.*
import kotlin.concurrent.schedule


class MainActivity : BaseActivity(), ToolbarManager {

    companion object {
        private val picasso : Picasso by lazy {
            Picasso.get()
        }
    }


    override val tag = "Main activity"
    override val toolbar by lazy { find<Toolbar>(R.id.toolbar) }
    val pBar by lazy { find<ProgressBar>(R.id.pBar) }

    override fun getLayout() = R.layout.activity_main
    //override fun getActivityTitle() = R.string.app_name

    private var isLoaded = false


    override fun onServiceConnected() {
        updateUI()
        Log.v(tag, "[ ON SERVICE CONNECTED ]")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN) // request full screen
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)  // request keeps on
        initUI()
    }

    /*

    override fun onDestroy() {
        super.onDestroy()
    }

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

        if(!isLoaded && !presenter.isDataLoaded)
            pBar.visibility = View.VISIBLE

        updateUI()

        // recommended to move to onStart
        presenter.attachReceiver()
                .onDataLoad {updateUI() }
                .onSensorAdd { sensorList.adapter?.notifyItemInserted(it) }
                .onSensorUpdate { sensorList.adapter?.notifyItemChanged(it) }
    }

    override fun onPause() {
        super.onPause()
        presenter.detachReceiver()
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
            val showUpdated = if(System.currentTimeMillis() < presenter.lastUpdated + 5000) "U" else ""
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
                    pressure.text = "${it.main.pressure_mm} мм"
                    wind.text = "${it.wind.speed} м/с"
                    wind_dir.text = it.wind.dir
                    iconpng.into(icon)

                    //.error(R.drawable.user_image).resize(110, 110).centerCrop()
                    //setIndicatorsEnabled(true) // to show if cached
                }
            }
        }
    }

    fun updateUI() {
        Log.v(tag, "UPDATE VIEW ${isLoaded} ... ${presenter.isDataLoaded}")
        if(isLoaded && presenter.isDataLoaded) {
            if(presenter.sensorRefreshIdx !=-1) {
                when(presenter.sensorOp) {
                    MainService.BROADCAST_EXTRAS_OP_UPD -> sensorList.adapter?.notifyItemChanged(presenter.sensorRefreshIdx)
                    MainService.BROADCAST_EXTRAS_OP_DEL -> sensorList.adapter?.notifyItemRemoved(presenter.sensorRefreshIdx)
                    else -> Log.v(tag, "something else...")
                }
                presenter.sensorRefreshIdx =-1
            }
            return
        }

        if(!isLoaded && presenter.isDataLoaded) {

            val adapter = SensorListAdapter(presenter) {
                startActivity<SensorDetailActivity>(
                        SensorDetailActivity.IDX to it
                )
            }
            sensorList.adapter = adapter
            isLoaded = true
            Log.v(tag, "UPDATE VIEW DONE ")
            pBar.visibility = View.GONE
        }

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
                presenter.addSensor(it)
                true
            }
        }
        .show()
    }
}
