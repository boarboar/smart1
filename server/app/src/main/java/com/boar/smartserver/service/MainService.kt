package com.boar.smartserver.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.boar.smartserver.db.SensorDb
import com.boar.smartserver.domain.Sensor
import com.boar.smartserver.domain.SensorList
import com.boar.smartserver.network.TcpServer
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import java.util.concurrent.Future
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class MainService : Service() {

    companion object {
        val BROADCAST_ACTION = "com.boar.smartserver.service"
        val BROADCAST_EXTRAS_OPERATION = "operation"
        val BROADCAST_EXTRAS_OP_LOAD = "load"
        val BROADCAST_EXTRAS_OP_ADD = "add"
        val BROADCAST_EXTRAS_OP_UPD = "update"
        val BROADCAST_EXTRAS_IDX = "param_idx"
    }

    private val tag = "Main service"
    private var binder = getServiceBinder()
    private var executor = TaskExecutor.getInstance(2)
    private var simFuture  : Future<Unit>? = null
    private val lock : Lock =  ReentrantLock()


    //private var sensors = SensorList()

    //val sensors = SensorList()
    /*
    val sensors : SensorList by lazy {
        loadSensors()
    }
    */

    //var sensors : SensorList = SensorList()

    private var sensors : SensorList? = null

    val sensorListSize : Int
        get() = lock.withLock { sensors?.size ?: 0 }

    fun getSensor(idx : Int) : Sensor? = lock.withLock { sensors?.getOrNull(idx)?.copy() }  // shallow, be sure to nullify refs

    fun isLoaded() = sensors !=null

    val db : SensorDb by lazy {
        SensorDb()
    }

    override fun onCreate() {
        super.onCreate()
        // called
        Log.v(tag, "[ ON CREATE ]")

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.v(tag, "[ ON START COMMAND ]")
        sensors = db.requestSensors()
        val intent = Intent()
        intent.action = BROADCAST_ACTION
        intent.putExtra(BROADCAST_EXTRAS_OPERATION, BROADCAST_EXTRAS_OP_LOAD)
        sendBroadcast(intent)

        TcpServer(applicationContext, 9999).run {
            val idx = lock.withLock { sensors?.update(it) ?: -1 }
            if(idx!=-1) {
                val intent = Intent()
                intent.action = BROADCAST_ACTION
                intent.putExtra(BROADCAST_EXTRAS_OPERATION, BROADCAST_EXTRAS_OP_UPD)
                intent.putExtra(BROADCAST_EXTRAS_IDX, idx)
                sendBroadcast(intent)
            }
        }

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
/*
    fun getSensors() : SensorList {
        Thread.sleep(2_000)
        return sensors
    }
  */
    /*
    private fun loadSensors() : SensorList {
        val ss = SensorList()
        ss.add(Sensor(1, "Window"))
        ss.add(Sensor(2, "Balcony"))
        Thread.sleep(2_000) // test
        return ss
    }
    */
    fun addSensor(sensor: Sensor) {
        Log.v(tag, "[ ADD SENSOR ]")
        executor.execute {
            val (res, errmsg) = db.saveSensor(sensor)
            if(res) sensors?.apply {

                lock.withLock { add(sensor) }
                val intent = Intent()
                intent.action = BROADCAST_ACTION
                intent.putExtra(BROADCAST_EXTRAS_OPERATION, BROADCAST_EXTRAS_OP_ADD)
                intent.putExtra(BROADCAST_EXTRAS_IDX, size - 1)
                sendBroadcast(intent)
            }
            else {
                runOnUiThread {
                    Toast.makeText(this, "DB Err : $errmsg", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun editSensor(position: Int, sensor: Sensor) {
        Log.v(tag, "[ EDIT SENSOR ]")
        executor.execute {
            val (res, errmsg) = db.updateSensor(sensor)
            if(res) sensors?.apply {
                lock.withLock { edit(position, sensor) }
                val intent = Intent()
                intent.action = BROADCAST_ACTION
                intent.putExtra(BROADCAST_EXTRAS_OPERATION, BROADCAST_EXTRAS_OP_UPD)
                intent.putExtra(BROADCAST_EXTRAS_IDX, position)
                sendBroadcast(intent)
            }
            else {
                runOnUiThread {
                    Toast.makeText(this, "DB Err : $errmsg", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    fun runSimulation() {
        Log.v(tag, "Start simulation")
        simFuture = doAsync {
            while(true) {
                Thread.sleep(2_000)
                val idx = sensors?.simulate() ?: -1
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

    fun stopSimulation() {
        simFuture?.cancel(true)
    }
}
