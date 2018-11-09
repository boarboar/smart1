package com.boar.smartserver.UI

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.boar.smartserver.R
import org.jetbrains.anko.find
import org.jetbrains.anko.toast

class SensorDetailActivity : AppCompatActivity(), ToolbarManager {

    override val toolbar by lazy { find<Toolbar>(R.id.toolbar) }

    companion object {
        //val ID = "DetailActivity:id"
        val LOCATION = "DetailActivity:location"
        val SENSOR = "DetailActivity:sensor"
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
        toolbarTitle = intent.getStringExtra(LOCATION)
        enableHomeAsUp { onBackPressed() }
    }
}