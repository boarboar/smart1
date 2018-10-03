package com.journaler.activity

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.journaler.R
import com.journaler.permission.PermissionCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


abstract class BaseActivity : PermissionCompatActivity() {
    companion object {
        val REQUEST_GPS = 0
        }
    protected abstract val tag : String
    protected abstract fun getLayout(): Int
    protected abstract fun getActivityTitle(): Int
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        setContentView(getLayout())
        //activity_title.setText(getActivityTitle())
        setSupportActionBar(toolbar)
        requestPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        )
        Log.v(tag, "[ ON CREATE ]")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        Log.v(tag, "[ ON POST CREATE ]")
    }
    override fun onRestart() {
        super.onRestart()
        Log.v(tag, "[ ON RESTART ]")
    }
    override fun onStart() {
        super.onStart()
        Log.v(tag, "[ ON START ]")
    }
    override fun onResume() {
        super.onResume()
        Log.v(tag, "[ ON RESUME ]")
        val animation = getAnimation(R.anim.top_to_bottom)
        findViewById<Toolbar>(R.id.toolbar).startAnimation(animation)
    }
    override fun onPostResume() {
        super.onPostResume()
        Log.v(tag, "[ ON POST RESUME ]")
    }
    override fun onPause() {
        super.onPause()
        Log.v(tag, "[ ON PAUSE ]")
        val animation = getAnimation(R.anim.hide_to_top)
        findViewById<Toolbar>(R.id.toolbar).startAnimation(animation)
    }
    override fun onStop() {
        super.onStop()
        Log.v(tag, "[ ON STOP ]")
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.v(tag, "[ ON DESTROY ]")
    }

}

fun Activity.getAnimation(animation: Int): Animation =
        AnimationUtils.loadAnimation(this, animation)