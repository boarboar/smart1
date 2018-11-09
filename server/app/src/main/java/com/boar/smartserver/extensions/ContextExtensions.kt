package com.boar.smartserver.extensions

import android.content.Context
import android.net.wifi.WifiManager
import android.support.v4.content.ContextCompat
import android.util.Log

fun Context.getLocalIpAddress(): String {

    fun ipToString(i: Int): String {
        return (i and 0xFF).toString() + "." +
                (i shr 8 and 0xFF) + "." +
                (i shr 16 and 0xFF) + "." +
                (i shr 24 and 0xFF)

    }

    try {
        val wifiManager: WifiManager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return ipToString(wifiManager.connectionInfo.ipAddress)
    } catch (ex: Exception) {
        Log.e("IP Address", ex.toString())
    }

    return ""
}

fun Context.resolveColor(color : Int) = ContextCompat.getColor(this, color)