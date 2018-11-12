package com.boar.smartserver.UI

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.boar.smartserver.R
import com.boar.smartserver.presenter.MainPresenter
import kotlinx.android.synthetic.main.activity_sensor_details.*
import org.jetbrains.anko.find
import org.jetbrains.anko.toast

class SensorDetailActivity() : AppCompatActivity(), ToolbarManager {

    val presenter: MainPresenter = MainActivity.presenter

    override val toolbar by lazy { find<Toolbar>(R.id.toolbar) }

    companion object {
        val IDX = "DetailActivity:index"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_details)
        initToolbar() {
            when (it) {
                // todo
                else -> toast("Unknown option")
            }
        }
        //toolbarTitle = intent.getStringExtra(LOCATION)
        val idx : Int = intent.getIntExtra(IDX, -1)
        if(idx!=-1) {
            presenter.getSensor(idx)?.apply {
                toolbarTitle = description ?: "None"
                //date.text = if(updated !=0L ) convertTime(updated) else "--:--:--"
                sensor_id.text = id.toString()
                sensor_temperature.text = "${temperatureAsString}º"
                sensor_vcc.text = "${vccAsString} v"
                sensor_res.text = "${resolution}"
                sensor_model.text = "${model}"
                sensor_par.text = "${parasite}"


                /*
                sensor_date.text = updated.toString()


                sensor_status.text = if(validated) "Good" else "Bad"
                */

            }
        }
        enableHomeAsUp { onBackPressed() }
    }
}