package com.boar.smartserver.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.boar.smartserver.domain.Sensor
import com.boar.smartserver.domain.SensorList
import org.jetbrains.anko.doAsync

class MainService : Service() {

    companion object {
        val BROADCAST_ACTION = "com.boar.smartserver.service"
        val BROADCAST_EXTRAS_OPERATION = "operation"
        val BROADCAST_EXTRAS_OP_ADD = "add"
        val BROADCAST_EXTRAS_OP_UPD = "update"
        val BROADCAST_EXTRAS_IDX = "param_idx"
    }

    private val tag = "Main service"
    private var binder = getServiceBinder()
    private var sensors = SensorList()
    //private var executor = TaskExecutor.getInstance(1)
    override fun onCreate() {
        super.onCreate()
        // called
        Log.v(tag, "[ ON CREATE ]")

    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.v(tag, "[ ON START COMMAND ]")
        // called


        sensors.add(Sensor(1, "Window"))
        sensors.add(Sensor(2, "Balcony"))


        return Service.START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder {
        Log.v(tag, "[ ON BIND ]")
        // do smth
        return binder
    }

    override fun onRebind(p0: Intent?) {
        Log.v(tag, "[ ON REBIND ]")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        val result = super.onUnbind(intent)
        Log.v(tag, "[ ON UNBIND ]")
        // do smth
        return result
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(tag, "[ ON DESTROY ]")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(tag, "[ ON LOW MEMORY ]")
    }

    private fun getServiceBinder(): MainServiceBinder =
            MainServiceBinder()

    inner class MainServiceBinder : Binder() {
        fun getService(): MainService = this@MainService
    }

    fun getSensors() : SensorList {
        Thread.sleep(5_000)
        return sensors
    }

    fun addSensor() {
        Log.v(tag, "[ ADD SENSOR ]")

        val intent = Intent()
        intent.action = BROADCAST_ACTION
        intent.putExtra(BROADCAST_EXTRAS_OPERATION, BROADCAST_EXTRAS_OP_ADD)
        intent.putExtra(BROADCAST_EXTRAS_IDX, 666)
        sendBroadcast(intent)

    }

    fun runSimulation() {
        Log.v(tag, "Start simulation")
        doAsync {
            while(true) {
                Thread.sleep(5_000)
                val idx = sensors.simulate()
                if (idx!=-1) {
                    Log.v(tag, "Siimulated : idx=$idx")
                    val intent = Intent()
                    intent.action = BROADCAST_ACTION
                    intent.putExtra(BROADCAST_EXTRAS_OPERATION, BROADCAST_EXTRAS_OP_UPD)
                    intent.putExtra(BROADCAST_EXTRAS_IDX, idx)
                    sendBroadcast(intent)
                }
            }
        }
    }
}