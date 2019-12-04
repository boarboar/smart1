package com.example.android.weatherapp.utils

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat

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

fun resolveThemeAttr(context: Context, @AttrRes attrRes: Int): TypedValue {
    val theme = context.theme
    val typedValue = TypedValue()
    theme.resolveAttribute(attrRes, typedValue, true)
    return typedValue
}

@ColorInt
fun Context.resolveColorAttr(@AttrRes colorAttr: Int): Int {
    val resolvedAttr = resolveThemeAttr(this, colorAttr)
    // resourceId is used if it's a ColorStateList, and data if it's a color reference or a hex color
    val colorRes = if (resolvedAttr.resourceId != 0)
        resolvedAttr.resourceId
    else
        resolvedAttr.data
    return ContextCompat.getColor(this, colorRes)
}
