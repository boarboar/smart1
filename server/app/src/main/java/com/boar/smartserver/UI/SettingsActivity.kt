package com.boar.smartserver.UI

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.boar.smartserver.R
import com.boar.smartserver.extensions.getLocalIpAddress
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.toolbar.*

class SettingsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        wifi_ip.text = applicationContext.getLocalIpAddress()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        //zipCode = cityCode.text.toString().toLong()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> false
    }
}