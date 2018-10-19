package com.boar.smartserver.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class MainServiceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
       // throw UnsupportedOperationException("Not yet implemented")
        Toast.makeText(context, "Broadcast Intent Detected.", Toast.LENGTH_LONG).show()
    }
}
