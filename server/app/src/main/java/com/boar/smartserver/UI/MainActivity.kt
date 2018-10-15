package com.boar.smartserver.UI

//import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.boar.smartserver.R
import com.boar.smartserver.SmartServer

class MainActivity : BaseActivity() {
    override val tag = "Main activity"

    override fun getLayout() = R.layout.activity_main
    override fun getActivityTitle() = R.string.app_name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(tag, "[ ON CREATE - CHECK]")
    }
}
