package com.boar.smartserver.UI.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.boar.smartserver.R
import com.boar.smartserver.SmartServer.Companion.ctx
import com.boar.smartserver.extensions.getLocalIpAddress
import kotlinx.android.synthetic.main.fragment_system_stat.view.*


class SystemStatFragment : SensorBaseFragment() {

    override val ftag = "Stat frag"

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        val view = inflater?.inflate(R.layout.fragment_system_stat, container, false)

        view.wifi_ip.text = ctx.getLocalIpAddress()
        view.sensor_cnt.text = "${presenter.sensorListSize}"
        view.log_cnt.text = "${presenter.logListSize}"
        view.sensor_hist_cnt.text = "${presenter.sensorHistSize}"

        return view
    }
}
