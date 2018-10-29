package com.boar.smartserver.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.boar.smartserver.db.SensorDb
import com.boar.smartserver.domain.Sensor
import com.boar.smartserver.domain.SensorList
import com.boar.smartserver.extensions.getLocalIpAddress
import org.jetbrains.anko.doAsync
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import java.util.concurrent.Future

/*
private fun getLocalIpAddress(ctx : Context): String? {

    fun ipToString(i: Int): String {
        return (i and 0xFF).toString() + "." +
                (i shr 8 and 0xFF) + "." +
                (i shr 16 and 0xFF) + "." +
                (i shr 24 and 0xFF)

    }

    try {
        val wifiManager: WifiManager = ctx?.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return ipToString(wifiManager.connectionInfo.ipAddress)
    } catch (ex: Exception) {
        Log.e("IP Address", ex.toString())
    }

    return null
}
*/

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
    private var executor = TaskExecutor.getInstance(2)
    private var simFuture  : Future<Unit>? = null

    //private var sensors = SensorList()

    //val sensors = SensorList()

    val sensors : SensorList by lazy {
        loadSensors()
    }

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


        // test to lazy init here!
        // test

        val sensfromdb = db.requestSensors()
        Log.v(tag, "Sens DB load total ${sensfromdb.size}")


        //loadSensors()
        // TODO: exec it here and send broadcast too init UI!

        executor.execute {
            Log.i(tag, "Listener thread [ START ]")
            Log.d("Listener", "WiFi Address detected as: {$applicationContext.getLocalIpAddress()}")
            val server = ServerSocket(9999)
            Log.d("Listener", "Server running on port ${server.inetAddress.hostAddress} : ${server.localPort} (${server.inetAddress.hostName})")

            while (true) {

                val client = server.accept()
                //println("Client conected : ${client.inetAddress.hostAddress}")

                Log.d("Listener", "Client connected : ${client.inetAddress.hostAddress}")

                executor.execute { handleClient(client) }

                //client.close()
            }

            server.close()
            Log.i(tag, "Listener thread [ STOP ]")
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

    private fun loadSensors() : SensorList {
        val ss = SensorList()
        ss.add(Sensor(1, "Window"))
        ss.add(Sensor(2, "Balcony"))
        Thread.sleep(2_000) // test
        return ss
    }

    fun addSensor(sensor: Sensor) {
        Log.v(tag, "[ ADD SENSOR ]")
        sensors.add(sensor)
        val intent = Intent()
        intent.action = BROADCAST_ACTION
        intent.putExtra(BROADCAST_EXTRAS_OPERATION, BROADCAST_EXTRAS_OP_ADD)
        intent.putExtra(BROADCAST_EXTRAS_IDX, sensors.size-1)
        sendBroadcast(intent)

    }

    fun runSimulation() {
        Log.v(tag, "Start simulation")
        simFuture = doAsync {
            while(true) {
                Thread.sleep(2_000)
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

    fun stopSimulation() {
        simFuture?.cancel(true)
    }

    fun handleClient(client: Socket) {
        val scanner = Scanner(client.inputStream)
        while (scanner.hasNextLine()) {
            val text = scanner.nextLine()

            Log.d("Client", "Raw: $text")

            val idx = sensors.update(text)

            if(idx!=-1) {
                val intent = Intent()
                intent.action = BROADCAST_ACTION
                intent.putExtra(BROADCAST_EXTRAS_OPERATION, BROADCAST_EXTRAS_OP_UPD)
                intent.putExtra(BROADCAST_EXTRAS_IDX, idx)
                sendBroadcast(intent)
            }
        }

        scanner.close()
        client.close()
    }
}

/*

TODO - try to send json with missing field

 */