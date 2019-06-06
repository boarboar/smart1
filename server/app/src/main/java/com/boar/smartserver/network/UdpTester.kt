package com.boar.smartserver.network

import android.content.Context
import android.util.Log
import com.boar.smartserver.extensions.getLocalIpAddress
import java.io.PrintWriter
import android.R.id.message
import java.net.*


class UdpTester(val ctx: Context, val port : Int) {
    private val tag = "UdpClient"
    fun send(msg : String) {
        var ds: DatagramSocket? = null
        try {
            ds = DatagramSocket()
            // IP Address below is the IP address of that Device where server socket is opened.
            val dp  = DatagramPacket(msg.toByteArray(), msg.length, InetAddress.getByName("0.0.0.0"), port)
            ds.send(dp)
            Log.d(tag, "Msg sent $msg")
        }
        catch (t: SocketException) {
            Log.w(tag, "TCP error: SocketException")
        }
        catch (t: Throwable) {
            val msg = t.message ?: "Unknown TCP error"
            Log.w(tag, "accept:: TCP error: $msg")
        }
        finally {
            ds?.close()
        }
    }
}