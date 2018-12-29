package com.boar.smartserver.network

import android.content.Context
import android.util.Log
import com.boar.smartserver.SmartServer.Companion.tag
import com.boar.smartserver.extensions.getLocalIpAddress
import com.boar.smartserver.service.MainService
import com.boar.smartserver.service.TaskExecutor
import java.io.ByteArrayOutputStream
import java.io.InterruptedIOException
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.*

class TcpServer(val ctx: Context, val port : Int, val srv: MainService) {
    private val tag = "TcpServ"
    private var executor = TaskExecutor.getInstance(2)
    private lateinit var server : ServerSocket
    private var isRunning = false


    fun run(handler : (String) -> Unit ) {
        server = ServerSocket(port)
        executor.execute {
            Log.i(tag, "Listener thread [ START ]")
            Log.d(tag, "WiFi Address detected as: ${ctx.getLocalIpAddress()}")
            Log.d("Listener", "Server running on port ${server.inetAddress.hostAddress} : ${server.localPort} (${server.inetAddress.hostName})")

            srv.logEventDb("Listener thread [ START ] on port ${server.inetAddress.hostAddress} : ${server.localPort}")

            isRunning = true

            while (true) {

                lateinit var client : Socket

                try {
                    client = server.accept()
                }
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

                Log.d("Listener", "Client connected : ${client.inetAddress.hostAddress}")
                srv.logEventDb("connected : ${client.inetAddress.hostAddress}")

                executor.execute {
                    process(client, handler)
                }
            }

            server.close()
            isRunning = false

            Log.i(tag, "Listener thread [ STOP ]")
            srv.logEventDb("Listener thread [ STOP ]")
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

    private fun process(socket: Socket, handler : (String) -> Unit) {
        srv.logEventDb("exec IN : ${socket.inetAddress.hostAddress}")
        try {
            socket.soTimeout= 5_1000

            /*
            val scanner = Scanner(client.inputStream)
            val builder = StringBuilder()


            while (scanner.hasNextLine()) {
                builder.append(scanner.nextLine())
            }


            //if (scanner.hasNextLine()) {
            //    builder.append(scanner.nextLine())
            //}



            Log.d("Client", "Raw: $builder")
            srv.logEventDb("Raw: $builder")
            handler(builder.toString())
            scanner.close()
            */


            val baos = ByteArrayOutputStream()
            socket.inputStream.use { it.copyTo(baos) }
            val inputAsString = baos.toString()

            Log.d("Client", "Raw: $inputAsString")
            srv.logEventDb("Raw: $inputAsString")
            handler(inputAsString)
        }
        catch (t: SocketTimeoutException) {
            Log.w(tag, "TCP error: SocketTimeoutException")
            srv.logEventDb("accept: SocketTimeoutException")
        }
        catch (t: Throwable) {
            val msg = t.message ?: "Unknown TCP error"
            Log.w(tag, "read:: TCP error: $msg")
            srv.logEventDb(msg)
        }
        finally {
            srv.logEventDb("CLOSE")
            socket.close()
        }
        srv.logEventDb("exec OUT")
    }
}
