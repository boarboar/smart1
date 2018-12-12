package com.boar.smartserver.UI.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.boar.smartserver.R
import com.boar.smartserver.SmartServer.Companion.ctx
import com.boar.smartserver.extensions.getLocalIpAddress
import kotlinx.android.synthetic.main.activity_settings.view.*

class SystemStatFragment : SensorBaseFragment() {

    override val ftag = "Stat frag"

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        val view = inflater?.inflate(R.layout.activity_settings, container, false)

        view.wifi_ip.text = ctx.getLocalIpAddress()

        return view
    }
}
