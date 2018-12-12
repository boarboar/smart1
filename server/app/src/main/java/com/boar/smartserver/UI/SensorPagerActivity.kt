package com.boar.smartserver.UI

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.Toast
import com.boar.smartserver.R
import com.boar.smartserver.UI.fragments.SensorPagerAdapter
import kotlinx.android.synthetic.main.activity_sensor_pager.*
import org.jetbrains.anko.find
import org.jetbrains.anko.toast

class SensorPagerActivity() : BaseActivity(), ToolbarManager {

    override val tag = "Detail activity"

    override fun getLayout() = R.layout.activity_sensor_pager

    override val toolbar by lazy { find<Toolbar>(R.id.toolbar) }

    companion object {
        val IDX = "DetailActivity:index"
    }

    var currentIdx = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initToolbar(R.menu.menu_sensor) {
            when (it) {
                R.id.action_edit -> editSensor()
                R.id.action_delete -> deleteSensor()
                else -> toast("Unknown option")
            }
        }
        showSensor(intent.getIntExtra(IDX, -1))

        enableHomeAsUp { onBackPressed() }
    }

    override fun onResume() {
        super.onResume()
        presenter.attachReceiver()
                .onSensorUpdate { if (it == currentIdx) showSensor(it) }
                .onSensorDelete { onBackPressed() }
    }

    override fun onPause() {
        super.onPause()
        presenter.detachReceiver()
    }

    private fun showSensor(idx: Int) {
        if (idx != -1) {
            val page = viewpager.currentItem
            presenter.getSensor(idx)?.apply {
                toolbarTitle = description ?: "None"
                currentIdx = idx
                viewpager.adapter = SensorPagerAdapter(supportFragmentManager, idx)
                viewpager.currentItem = page
                sliding_tabs.setupWithViewPager(viewpager)
            }
        }
    }

    private fun editSensor() {
        if (currentIdx != -1) {
            presenter.getSensor(currentIdx)?.apply {
                val sensDlg = SensorPropDialog(this@SensorPagerActivity, this, isEdit = true)
                sensDlg.create().onCancel {
                    Log.v(tag, "DCLOSE")
                }.onDone {
                    if (!it.validate()) {
                        Toast.makeText(this@SensorPagerActivity, "Check data", Toast.LENGTH_LONG).show()
                        false
                    } else {
                        Log.v(tag, "DOK ${it}")
                        presenter.editSensor(currentIdx, it)
                        true
                    }
                }.show()
            }
        }
    }

    private fun deleteSensor() {
        if (currentIdx != -1) {
            presenter.getSensor(currentIdx)?.apply {
                val builder = AlertDialog.Builder(this@SensorPagerActivity)
                        .setTitle(this.description)
                        .setMessage(R.string.sensor_del_msg)
                        .setPositiveButton("YES") { dialog, which ->
                            presenter.deleteSensor(currentIdx)
                            //onBackPressed()
                        }
                builder.setNegativeButton("No") { dialog, which ->
                }
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }
    }

}