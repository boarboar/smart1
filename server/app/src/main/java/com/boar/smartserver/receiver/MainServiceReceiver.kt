package com.boar.smartserver.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.boar.smartserver.service.MainService

class MainServiceReceiver : BroadcastReceiver() {
    private val tag = "Main receiver"
    override fun onReceive(context: Context, intent: Intent) {
        val op = intent.getStringExtra(MainService.BROADCAST_EXTRAS_OPERATION)
        val idx = intent.getIntExtra(MainService.BROADCAST_EXTRAS_IDX, -1)
        Log.v(tag, "[ BRDCST $op $idx]")
        //Toast.makeText(context, "Broadcast Intent Detected.", Toast.LENGTH_LONG).show()

        when (op) {
            MainService.BROADCAST_EXTRAS_OP_ADD -> Toast.makeText(context, "ADD $idx", Toast.LENGTH_LONG).show()
            MainService.BROADCAST_EXTRAS_OP_UPD -> Toast.makeText(context, "UPD $idx", Toast.LENGTH_LONG).show()
            else -> Log.v(tag, "[ BRDCST $op $idx]")
        }
    }
}

//https://stackoverflow.com/questions/47742474/kotlin-call-a-function-to-update-ui-from-broadcastreceiver-onreceive
//https://startandroid.ru/ru/uroki/vse-uroki-spiskom/161-urok-96-service-obratnaja-svjaz-s-pomoschju-broadcastreceiver.html