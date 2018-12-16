package com.boar.smartserver.network

import android.content.Context
import android.util.Log
import com.boar.smartserver.SmartServer.Companion.tag
import com.boar.smartserver.extensions.getLocalIpAddress
import com.boar.smartserver.service.MainService
import com.boar.smartserver.service.TaskExecutor
import java.net.ServerSocket
import java.net.Socket
import java.util.*

class TcpServer(val ctx: Context, val port : Int, val srv: MainService) {
    private val tag = "TcpServ"
    private var executor = TaskExecutor.getInstance(2)
    lateinit var server : ServerSocket

    fun run(handler : (String) -> Unit ) {
        server = ServerSocket(port)
        executor.execute {
            Log.i(tag, "Listener thread [ START ]")
            Log.d(tag, "WiFi Address detected as: ${ctx.getLocalIpAddress()}")
            Log.d("Listener", "Server running on port ${server.inetAddress.hostAddress} : ${server.localPort} (${server.inetAddress.hostName})")

            while (true) {

                //val client = server.accept()

                lateinit var client : Socket

                try {
                    client = server.accept()
                }
                catch (t: Throwable) {
                    val msg = t.message ?: "Unknown TCP error"
                    Log.w(tag, "TCP error: $msg")
                    srv.logEventDb(msg)
                    continue
                }
                finally {
                    ;
                }

                Log.d("Listener", "Client connected : ${client.inetAddress.hostAddress}")
                srv.logEventDb("connected : ${client.inetAddress.hostAddress}")

                executor.execute {
                    srv.logEventDb("exec IN : ${client.inetAddress.hostAddress}")
                    try {
                        val scanner = Scanner(client.inputStream)
                        val builder = StringBuilder()
                        while (scanner.hasNextLine()) {
                            builder.append(scanner.nextLine())
                        }
                        Log.d("Client", "Raw: $builder")
                        handler(builder.toString())
                        scanner.close()
                    }
                    catch (t: Throwable) {
                        val msg = t.message ?: "Unknown TCP error"
                        Log.w(tag, "TCP error: $msg")
                        srv.logEventDb(msg)
                    }
                    finally {
                        client.close()
                    }
                    srv.logEventDb("exec OUT : ${client.inetAddress.hostAddress}")
                }

                //client.close()
            }

            server.close()
            Log.i(tag, "Listener thread [ STOP ]")

            srv.logEventDb("Listener thread [ STOP ]")
        }
    }

    fun stop() {
        try {
            server.close()
        }
        catch (t: Throwable) {
            val msg = t.message ?: "Unknown TCP error"
            Log.w(tag, "TCP error: $msg")
            srv.logEventDb(msg)
        }
    }
}
