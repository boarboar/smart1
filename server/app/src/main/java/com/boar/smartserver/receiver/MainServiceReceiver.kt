package com.boar.smartserver.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.boar.smartserver.service.MainService

class MainServiceReceiver(val handle: (String, Int) -> Unit) : BroadcastReceiver() {
    private val tag = "Main receiver"
    override fun onReceive(context: Context, intent: Intent) {
        val op : String = intent.getStringExtra(MainService.BROADCAST_EXTRAS_OPERATION)
        val idx : Int = intent.getIntExtra(MainService.BROADCAST_EXTRAS_IDX, -1)
        Log.v(tag, "[ BRDCST $op $idx]")
        handle(op, idx)
        //Toast.makeText(context, "Broadcast Intent Detected.", Toast.LENGTH_LONG).show()
    }
}

