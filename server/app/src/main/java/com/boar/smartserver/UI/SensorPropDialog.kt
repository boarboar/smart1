package com.boar.smartserver.UI

import android.content.Context
import android.support.design.widget.TextInputEditText
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import com.boar.smartserver.R

class SensorPropDialog(context: Context) : BaseDialog() {

    //  dialog view
    override val dialogView: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.sensor_prop, null)
    }

    override val builder: AlertDialog.Builder = AlertDialog.Builder(context).setView(dialogView)

    override fun create(): SensorPropDialog {
        super.create()
        return this
    }

    fun onCancel(func: (() -> Unit)? = null): SensorPropDialog {
        with(closeIcon) {
            setClickListenerToDialogIcon(func)
        }
        return this
    }

    fun onDone(func: (() -> Unit)? = null): SensorPropDialog {
        with(doneIcon) {
            setClickListenerToDialogIcon(func)
        }
        return this
    }

    //  notes edit text
    val sensorId by lazy {
        dialogView.findViewById<EditText>(R.id.sensor_id)
    }

    val sensorLoc by lazy {
        dialogView.findViewById<EditText>(R.id.sensor_loc)
    }

    //  done icon
    private val doneIcon: ImageView by lazy {
        dialogView.findViewById<ImageView>(R.id.done_icon)
    }

    //  close icon
    private val closeIcon: ImageView by lazy {
        dialogView.findViewById<ImageView>(R.id.close_icon)
    }

    //  view click listener as extension function
    private fun View.setClickListenerToDialogIcon(func: (() -> Unit)?) =
            setOnClickListener {
                func?.invoke()
                dialog?.dismiss()
            }
}