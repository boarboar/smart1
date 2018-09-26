package com.example.boar.ankotest

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.example.boar.ankotest.R.id.recyclerView
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiInfo
import android.support.v4.content.ContextCompat.getSystemService
import android.net.wifi.WifiManager
import android.text.format.Formatter.formatIpAddress



private fun getLocalIpAddress(ctx : Context): String? {
    fun ipToString(i: Int): String {
        return (i and 0xFF).toString() + "." +
                (i shr 8 and 0xFF) + "." +
                (i shr 16 and 0xFF) + "." +
                (i shr 24 and 0xFF)

    }

    try {
        val wifiManager: WifiManager = ctx?.getSystemService(WIFI_SERVICE) as WifiManager
        return ipToString(wifiManager.connectionInfo.ipAddress)
    } catch (ex: Exception) {
        Log.e("IP Address", ex.toString())
    }

    return null
}



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /*
        verticalLayout {
            val name = editText()
            button("Say Hello") {
                onClick { toast("Hello, ${name.text}!") }
            }
        }*/
        recyclerView.layoutManager = LinearLayoutManager(this)

        val list: ArrayList<Movie> = arrayListOf()
        list.add(Movie("Sherlock Holmes",2009))
        list.add(Movie("The Shawshank Redemption",1994))
        list.add(Movie("Forrest Gump",1994))
        list.add(Movie("Titanic",1997))
        list.add(Movie("Taxi",1998))
        list.add(Movie("Inception",1994))
        list.add(Movie("The Imitation Game",2014))

        recyclerView.adapter = MovieAdapter(list)

        doAsync {
            //var result = runLongTask()
            /*
            val wifiMan = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            val conInfo = wifiMan.getConnectionInfo();
            val ipAddress = conInfo.getIpAddress();

            //int ipAddress = wifiInf.getIpAddress();
            //String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff))
            */

            Log.d("Listener", "Start")

            val ips = getLocalIpAddress(applicationContext) ?: "No Wifi IP"

            Log.d("Listener", ips)

            uiThread {
                toast(ips)
            }

            for (i in 1..6) {
                Thread.sleep(5_000)
                uiThread {
                    toast("Wakeup $i")
                    Log.d("Listener", "Tick!")
                    list[i-1].year=i
                    recyclerView.adapter.notifyItemChanged(i-1)
                    Log.d("Listener", "Update!")
                }
            }
        }

    }
}
