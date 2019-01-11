package com.boar.smartserver.UI

import android.content.Context
import android.support.design.widget.TextInputEditText
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.boar.smartserver.R
import com.boar.smartserver.domain.Sensor

class SensorPropDialog(context: Context, var sensor: Sensor?, val isEdit:Boolean = false) : BaseDialog() {

    //  dialog view
    override val dialogView: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.sensor_prop, null)
    }

    //  notes edit text
    val sensorId by lazy {
        dialogView.findViewById<EditText>(R.id.sensor_id)
    }

    val sensorLoc by lazy {
        dialogView.findViewById<EditText>(R.id.sensor_loc)
    }

    //  done icon
    private val doneIcon: Button by lazy {
        //dialogView.findViewById<ImageView>(R.id.done_icon)
        dialogView.findViewById<Button>(R.id.done_but)
    }

    //  close icon
    private val closeIcon: Button by lazy {
        //dialogView.findViewById<ImageView>(R.id.close_icon)
        dialogView.findViewById<Button>(R.id.close_but)
    }

    override val builder: AlertDialog.Builder = AlertDialog.Builder(context).setView(dialogView)

    override fun create(): SensorPropDialog {
        super.create()
        sensorId.setText(sensor?.id.toString())
        sensorLoc.setText(sensor?.description)
        if(isEdit) sensorId.isEnabled = false
        return this
    }

    fun onCancel(func: (() -> Unit)? = null): SensorPropDialog {
        with(closeIcon) {
            //setClickListenerToDialogIcon(func)
            setOnClickListener {
                func?.invoke()
                dialog?.dismiss()
            }
        }
        return this
    }

    fun onDone(func: ((Sensor) -> Boolean)? = null): SensorPropDialog {
        with(doneIcon) {
            //setClickListenerToDialogIcon(func)

            setOnClickListener {
                val sens_id = sensorId.text.toString()
                val sens_loc = sensorLoc.text.toString()
                // verify TODO
                //sensor = Sensor(sens_id.toShortOrNull()?:0, sens_loc)
                val res: Boolean =
                        func?.invoke(Sensor(sens_id.toIntOrNull()?:0, sens_loc)) ?: false
                if (res) dialog?.dismiss()
            }
        }
        return this
    }

    //  view click listener as extension function
    private fun View.setClickListenerToDialogIcon(func: (() -> Unit)?) =
            setOnClickListener {
                func?.invoke()
                dialog?.dismiss()
            }
}