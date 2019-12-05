package com.example.android.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.android.weatherapp.utils.DateUtils
import java.util.*
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity() {

    //lateinit var binding: ActivityMainBinding
    val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }

    var toolbarTitle: String
        get() = toolbar.title.toString()
        set(value) {
            toolbar.title = value
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        toolbarTitle = ""

        setupNavigation()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN) // request full screen
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)  // request keeps on

        Timer().schedule(100, 1000){// Time
            runOnUiThread { toolbarTitle = "${DateUtils.convertDateTime(System.currentTimeMillis())}" }
        }

        toolbarTitle = "Hi!"
    }

    /**
    * Called when the hamburger menu or back button are pressed on the Toolbar
    *
    * Delegate this to Navigation.
    */

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.nav_host_fragment)
        return navController.navigateUp()
    }
    /**
     * Setup Navigation for this Activity
     */
    private fun setupNavigation() {
        // first find the nav controller
        val navController = findNavController(R.id.nav_host_fragment)


        //setSupportActionBar(findViewById(R.id.toolbar))
        setSupportActionBar(toolbar)

        NavigationUI.setupActionBarWithNavController(this, navController)
        // then setup the action bar, tell it about the DrawerLayout
        //setupActionBarWithNavController(navController, binding.drawerLayout)

        // finally setup the left drawer (called a NavigationView)
        //binding.navigationView.setupWithNavController(navController)

    }
}



