package com.boar.smartserver.network

import android.content.Context
import android.util.Log
import com.boar.smartserver.extensions.getLocalIpAddress
import java.net.Socket
import java.net.SocketException
import java.io.PrintWriter


class TcpTester(val ctx: Context, val port : Int) {
    private val tag = "TcpClient"
    fun send(msg : String) {
        var client: Socket? = null
        try {
            client = Socket("0.0.0.0", port)
            val printWriter = PrintWriter(client.getOutputStream())
            printWriter.write(msg)
            printWriter.flush()
            printWriter.close()
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
            client?.close()
        }
    }
}