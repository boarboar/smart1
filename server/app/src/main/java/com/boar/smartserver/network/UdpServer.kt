package com.boar.smartserver.network

import android.content.Context
import android.util.Log
import com.boar.smartserver.SmartServer.Companion.tag
import com.boar.smartserver.extensions.getLocalIpAddress
import com.boar.smartserver.service.MainService
import com.boar.smartserver.service.TaskExecutor
import java.io.ByteArrayOutputStream
import java.io.InterruptedIOException
import android.os.Process
import java.net.*

import java.util.*
import java.lang.reflect.Array.getLength



class UdpServer(val ctx: Context, val port : Int, val srv: MainService) {
    private val tag = "UdpServ"
    private var executor = TaskExecutor.getInstance(2)
    private lateinit var server : DatagramSocket
    private var isRunning = false


    fun run(handler : (String) -> Unit ) {
        server = DatagramSocket(port)
        //server.soTimeout = 10_000
        executor.execute {
            Log.i(tag, "Datagram listener thread [ START ] with PRIO ${Process.getThreadPriority(0)}")
            //Log.d(tag, "WiFi Address detected as: ${ctx.getLocalIpAddress()}")
            Log.d("Listener", "Datagram server running on port  ${server.localPort}")
            srv.logEventDb("Datagram listener thread [ START ] on port  ${server.localPort}")

            isRunning = true

            while (true) {

                val lMsg = ByteArray(1024)
                val dp = DatagramPacket(lMsg, lMsg.size)

                try {
                    server.receive(dp)
                }
                /*
                catch (t: SocketTimeoutException) {
                    continue;
                }
                */
                catch (t: InterruptedIOException) {
                    Log.w(tag, "TCP error: InterruptedIOException")
                    srv.logEventDb("accept: InterruptedIOException")
                    break
                }
                catch (t: SocketException) {
                    Log.w(tag, "TCP error: SocketException")
                    srv.logEventDb("accept: SocketException")
                    break
                }
                catch (t: Throwable) {
                    val msg = t.message ?: "Unknown TCP error"
                    Log.w(tag, "accept:: TCP error: $msg")
                    srv.logEventDb(msg)
                    continue
                }
                finally {
                }

                Log.d("Datagram Listener", "Client rcv : ${dp.address}")
                srv.logEventDb("DG rcv : ${dp.address.hostAddress}")

                executor.execute {
                    process(String(lMsg, 0, dp.length), handler)
                }
            }

            server.close()
            isRunning = false

            Log.i(tag, "Datagram Listener thread [ STOP ]")
            srv.logEventDb("Datagram Listener thread [ STOP ]")
        }
    }

    fun stop() {
        try {
            if(this::server.isInitialized)
                server.close()
            while(!server.isClosed) {} // wait
            while(isRunning) {} // wait
        }
        catch (t: Throwable) {
            val msg = t.message ?: "Unknown TCP error"
            Log.w(tag, "Stop:: TCP error: $msg")
            srv.logEventDb(msg)
        }
        Log.w(tag, "Stop:: done")
    }

    private fun process(inputAsString: String, handler : (String) -> Unit) {
        Log.d("DG exec IN :", "Raw: $inputAsString")
        srv.logEventDb("DG Raw: $inputAsString")
        handler(inputAsString)
        srv.logEventDb("exec OUT")
    }
}
