package com.boar.smartserver.UI

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
//import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import com.boar.smartserver.presenter.MainPresenter
import com.boar.smartserver.service.MainService
//import com.boar.smartserver.R
import kotlinx.android.synthetic.main.activity_main.*


abstract class BaseActivity : AppCompatActivity() {

    protected abstract val tag : String
    protected abstract fun getLayout(): Int
    //protected abstract fun getActivityTitle(): Int

    protected val presenter : MainPresenter by lazy  { MainPresenter.instance }
    protected var service: MainService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            service = null
            isBound = false
            presenter.detachService()
            onServiceDisconnected()
            Log.v(tag, "[ SRV ONBOUND ]")
            //synchronize.enabled = false
        }
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            if (binder is MainService.MainServiceBinder) {
                service = binder.getService()
                service?.let {
                    isBound = true
                    presenter.attachService(service)
                    //updateUI()
                    onServiceConnected()
                    Log.v(tag, "[ SRV BOUND ]")
                }
            }
        }

    }

    open fun onServiceDisconnected() {
        Log.v(tag, "[ ON SERVICE DISCONNECTED ]")
    }

    open fun onServiceConnected() {
        Log.v(tag, "[ ON SERVICE CONNECTED ]")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayout())
        //setSupportActionBar(toolbar)
        Log.v(tag, "[ ON CREATE ]")
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        Log.v(tag, "[ ON SAVE INST STATE ]")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //menuInflater.inflate(R.menu.main, menu)
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
        presenter.onUserInteraction(this)

        val intent = Intent(this, MainService::class.java)
        bindService(intent, serviceConnection,  Context.BIND_AUTO_CREATE + Context.BIND_IMPORTANT)

        Log.v(tag, "[ ON RESUME ]")
    }
    override fun onPostResume() {
        super.onPostResume()
        Log.v(tag, "[ ON POST RESUME ]")
    }
    override fun onPause() {
        super.onPause()
        unbindService(serviceConnection)
        presenter.onUserInteraction(null)

        Log.v(tag, "[ ON PAUSE ]")

    }
    override fun onStop() {
        super.onStop()
        Log.v(tag, "[ ON STOP ]")
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.v(tag, "[ ON DESTROY ]")
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        presenter.onUserInteraction(this)
        //Log.v(tag, "[ ON USER INTERACT ]")
    }

}
