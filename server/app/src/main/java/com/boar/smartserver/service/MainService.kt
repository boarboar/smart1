package com.boar.smartserver.service

import android.app.Service
import android.content.Intent
import android.hardware.SensorAdditionalInfo
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.boar.smartserver.SmartServer.Companion.ctx
import com.boar.smartserver.db.SensorDb
import com.boar.smartserver.domain.*
import com.boar.smartserver.network.TcpServer
import com.boar.smartserver.network.TcpTester
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import java.util.concurrent.Future
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import com.boar.smartserver.R
import android.app.PendingIntent
import android.support.v4.app.NotificationCompat
import com.boar.smartserver.UI.MainActivity



class MainService : Service() {

    companion object {
        val TCP_PORT = 9999

        val SIMULATION_TIMEOUT = 10_000L

        //val HIST_KEEP_REC_MAX = 1_344 // ca 1 week for 2 sensors
        //val HIST_KEEP_REC_MAX = 2_688 // ca 2 weeks for 2 sensors
        val HIST_KEEP_REC_MAX = 5_376 // ca 4 weeks for 2 sensors
        val LOG_KEEP_REC_MAX = 256
        val BACKGROUND_TASK_TIMEOUT = 900_000L // every 15 min

        //val LOG_KEEP_REC_MAX = 8
        //val LOG_CLEAN_TIMEOUT = 60_000L // every 5 min

        val BROADCAST_ACTION = "com.boar.smartserver.service"
        val BROADCAST_EXTRAS_OPERATION = "operation"

        // enum ?
        val BROADCAST_EXTRAS_OP_LOAD = "load"
        val BROADCAST_EXTRAS_OP_ADD = "add"
        val BROADCAST_EXTRAS_OP_UPD = "update"
        val BROADCAST_EXTRAS_OP_DEL = "del"

        val BROADCAST_EXTRAS_IDX = "param_idx"
    }

    private val tag = "Main service"
    private var binder = getServiceBinder()
    private var executor = TaskExecutor.getInstance(2)
    private var simFuture  : Future<Unit>? = null
    private var bgTaskFuture  : Future<Unit>? = null
    private val lock : Lock =  ReentrantLock()

    private var sensors : SensorList? = null
    private var logsdb : MutableList<ServiceLog>? = null  // DESC order

    lateinit private var tcpServer : TcpServer

    /*
    private val logsdb : MutableList<ServiceLog> by lazy {
        db.requestLog(LOG_KEEP_REC_MAX)
    }
    */

    val sensorListSize : Int
        get() = lock.withLock { sensors?.size ?: 0 }

    fun getSensor(idx : Int) : Sensor? = lock.withLock { sensors?.getOrNull(idx)?.copy() }  // shallow, be sure to nullify refs

    val logListSize : Int
        get() = lock.withLock { logsdb?.size ?: 0 }

    val sensorHistSize : Int
        get() = db.getSensorHistSize()

    fun getServiceLog(idx : Int) : ServiceLog? = lock.withLock { logsdb?.getOrNull(idx)?.copy() }  // shallow, be sure to nullify refs

    fun isLoaded() = sensors !=null

    val db : SensorDb by lazy {
        SensorDb()
    }

    fun startFg() {
        val notificationIntent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0)

        val notification = NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("My Awesome App")
                .setContentText("Doing some work...")
                .setContentIntent(pendingIntent).build()

        startForeground(1337, notification)
    }

    override fun onCreate() {
        super.onCreate()
        // called
        Log.v(tag, "[ ON CREATE ]")

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.v(tag, "[ ON START COMMAND ]")

        startFg()

        doAsync {
            sensors = db.requestSensors()
            logsdb = db.requestLog(LOG_KEEP_REC_MAX) // TODO - lazy!

            //val latest = db.requestLatestSensorHist()
            //Log.v(tag, "Latest: $latest")

            sensors?.let {
                db.setLatestSensorHist(it)
            }

            logEventDb("SRV START")
            val intent = Intent()
            intent.action = BROADCAST_ACTION
            intent.putExtra(BROADCAST_EXTRAS_OPERATION, BROADCAST_EXTRAS_OP_LOAD)
            sendBroadcast(intent)
        }

        tcpServer = TcpServer(applicationContext, TCP_PORT, this)
        tcpServer.run { processMessage(it) }

        startBgTask()

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

    fun restartTcpService() {
        executor.execute {
            tcpServer.stop()
            tcpServer.run { processMessage(it) }
        }
    }

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

    fun deleteSensor(position: Int) {
        Log.v(tag, "[ DEL SENSOR ]")
        val sensor :Sensor = lock.withLock { sensors?.getOrNull(position)?.copy() } ?: return
        executor.execute {
            val (res, errmsg) = db.deleteSensor(sensor)
            if(res) sensors?.apply {
                lock.withLock { removeAt(position) }
                val intent = Intent()
                intent.action = BROADCAST_ACTION
                intent.putExtra(BROADCAST_EXTRAS_OPERATION, BROADCAST_EXTRAS_OP_DEL)
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

    fun processMessage(msg : String) {
        //logEventDb(msg)
        val meas = sensors?.measFromJson(msg)
        if (meas != null) {
            val idx = lock.withLock { sensors?.update(meas) ?: -1 }
            if (idx != -1) {
                if (meas.validated) saveToHist(meas)

                val intent = Intent()
                intent.action = BROADCAST_ACTION
                intent.putExtra(BROADCAST_EXTRAS_OPERATION, BROADCAST_EXTRAS_OP_UPD)
                intent.putExtra(BROADCAST_EXTRAS_IDX, idx)
                sendBroadcast(intent)
            }
        }
    }

    fun saveToHist(meas : SensorMeasurement) {
        if(meas.validated) {
            val hist = SensorHistory(meas.id.toInt(), meas.temp10, meas.vcc1000,
                    meas.hum10, meas.hd)
            db.saveSensorHist(hist)
        }
    }

    fun logEventDb(msg : String) {
        val event = ServiceLog(msg)
        val (res, errmsg) = db.saveLog(event)
        if (res) {
            lock.withLock { logsdb?.add(0, event) } // DESC order
        }
    }

    fun startBgTask() {
        Log.v(tag, "Start log cleaner daemmon")
        bgTaskFuture = doAsync {
            while(true) {
                Thread.sleep(MainService.BACKGROUND_TASK_TIMEOUT)
                Log.v(tag, "Do Log/Hist clean")
                if(db.cleanLog(LOG_KEEP_REC_MAX)>0) {
                    lock.withLock { logsdb = logsdb?.take(LOG_KEEP_REC_MAX)?.toMutableList()} //
                }
                if(db.cleanSensorHist(HIST_KEEP_REC_MAX)>0) {
                    // do smth...
                }

                val idxs = lock.withLock { sensors?.checkForOutdated()}
                idxs?.forEach {
                    Log.v(tag, "OUTDATED $it")
                    val intent = Intent()
                    intent.action = BROADCAST_ACTION
                    intent.putExtra(BROADCAST_EXTRAS_OPERATION, BROADCAST_EXTRAS_OP_UPD)
                    intent.putExtra(BROADCAST_EXTRAS_IDX, it)
                    sendBroadcast(intent)
                }
            }
        }
    }

    fun getSensorHistory(sensorId : Int, size: Int = 128) : List<SensorHistory> {
        // temporarily...
        // make cache for sensorId
        // add paging / periods...
        return db.requestSensorHist(sensorId, size)
    }


    fun runSimulation() {
        Log.v(tag, "Start simulation")
        val tester = TcpTester (ctx, TCP_PORT)
        simFuture = doAsync {
            while(true) {
                Thread.sleep(MainService.SIMULATION_TIMEOUT)
                val msg =  SensorList.simulate()
                //processMessage(msg)
                tester.send(msg)
            }
        }
    }

    fun stopSimulation() {
        simFuture?.cancel(true)
    }


}
