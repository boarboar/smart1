package com.boar.smartserver.UI

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AlertDialog
import android.view.View

abstract class BaseDialog {

    abstract val dialogView: View
    abstract val builder: AlertDialog.Builder

    //  required bools
    protected var cancelable: Boolean = true
    protected var isBackGroundTransparent: Boolean = true

    //  dialog
    protected var dialog: AlertDialog? = null

    //  dialog create
    /*
    fun create(): AlertDialog {
        dialog = builder
                .setCancelable(cancelable)
                .create()

        //  very much needed for customised dialogs
        if (isBackGroundTransparent)
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return dialog!!
    }
    */

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
    fun onCancelListener(func: () -> Unit): AlertDialog.Builder =
            builder.setOnCancelListener {
                func()
            }
}
