package com.boar.smartserver.UI.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.boar.smartserver.R
import com.boar.smartserver.UI.DateUtils
import com.boar.smartserver.presenter.MainPresenter
import kotlinx.android.synthetic.main.fragment_sensor_log.view.*
import kotlinx.android.synthetic.main.item_sensor_log.view.*

class SystemLogFragment : SensorBaseFragment() {

    override val ftag = "SysLog frag"

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        val view = inflater?.inflate(R.layout.fragment_sensor_log, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.v(ftag, "[ ON VIEW CREATED ]")
        val adapter = SystemLogListAdapter(presenter) {}
        val layout = LinearLayoutManager(context)
        view?.sensor_log?.layoutManager = layout
        view?.sensor_log?.adapter = adapter
    }

}


class SystemLogListAdapter(private val presenter : MainPresenter,
                           private val itemClick: (Int) -> Unit) :
        RecyclerView.Adapter<SystemLogListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sensor_log, parent, false)
        return ViewHolder(view, itemClick)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindLog(presenter, position)
    }

    override fun getItemCount() : Int {
        return if(presenter.logListSize <= 128) presenter.logListSize else 128
    }

    class ViewHolder(view: View, private val itemClick: (Int) -> Unit)
        : RecyclerView.ViewHolder(view) {
        fun bindLog(presenter : MainPresenter, position: Int) {
            presenter.getServiceLog(position)?.apply {
                itemView.timestamp.text = DateUtils.convertDateTime(timestamp)
                itemView.msg.text = message
            }
        }

    }
}
