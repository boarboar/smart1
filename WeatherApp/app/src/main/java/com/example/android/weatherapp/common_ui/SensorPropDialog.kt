package com.example.android.weatherapp.common_ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.example.android.weatherapp.R
import com.example.android.weatherapp.domain.Sensor

abstract class BaseDialog {

    abstract val dialogView: View
    abstract val builder: AlertDialog.Builder

    //  required bools
    protected var cancelable: Boolean = true
    protected var isBackGroundTransparent: Boolean = false

    //  dialog
    protected var dialog: AlertDialog? = null

    open fun create(): BaseDialog {
        dialog = builder
            .setCancelable(cancelable)
            .create()

        //  very much needed for customised dialogs
        if (isBackGroundTransparent)
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return this
    }

    fun show() {
        dialog?.show()
    }

    //  cancel listener
//    fun onCancelListener(func: () -> Unit): AlertDialog.Builder =
//        builder.setOnCancelListener {
//            func()
//        }
}

class SensorPropDialog(context: Context, var sensor: Sensor?, val isEdit:Boolean = false) : BaseDialog() {

    //  dialog view
    override val dialogView: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.dialog_sensor_prop, null)
    }

    //  notes edit text
    val sensorId by lazy {
        dialogView.findViewById<EditText>(R.id.sensor_id)
    }

    val sensorLoc by lazy {
        dialogView.findViewById<EditText>(R.id.sensor_loc)
    }

    //  done icon
    private val doneBtn: Button by lazy {
        //dialogView.findViewById<ImageView>(R.id.done_icon)
        dialogView.findViewById<Button>(R.id.done_but)
    }

    //  close icon
    private val closeBtn: Button by lazy {
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
        with(closeBtn) {
            setOnClickListener {
                func?.invoke()
                dialog?.dismiss()
            }
        }
        return this
    }

    fun onDone(func: (Sensor) -> Boolean): SensorPropDialog {
        with(doneBtn) {
            setOnClickListener {
                val sens_id = sensorId.text.toString()
                val sens_loc = sensorLoc.text.toString()
                sensor = Sensor(sens_id.toIntOrNull()?:0, sens_loc)
                val res = func(Sensor(sens_id.toIntOrNull()?:0, sens_loc))
                if (res) dialog?.dismiss()
            }
        }
        return this
    }

//    //  view click listener as extension function
//    private fun View.setClickListenerToDialogIcon(func: (() -> Unit)?) =
//            setOnClickListener {
//                func?.invoke()
//                dialog?.dismiss()
//            }
}