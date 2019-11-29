package com.example.android.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate


class MainActivity : AppCompatActivity() {

    //lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setupNavigation()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN) // request full screen
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)  // request keeps on
    }

    /**
    * Called when the hamburger menu or back button are pressed on the Toolbar
    *
    * Delegate this to Navigation.
    */
    /*
    override fun onSupportNavigateUp()
            =
        NavigationUI.navigateUp(findNavController(R.id.nav_host_fragment), binding.drawerLayout)
    */
    /**
     * Setup Navigation for this Activity
     */
    private fun setupNavigation() {
        // first find the nav controller
        //val navController = findNavController(R.id.nav_host_fragment)


        setSupportActionBar(findViewById(R.id.toolbar))

        // then setup the action bar, tell it about the DrawerLayout
        //setupActionBarWithNavController(navController, binding.drawerLayout)

        // finally setup the left drawer (called a NavigationView)
        //binding.navigationView.setupWithNavController(navController)

    }
}



