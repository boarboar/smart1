package com.boar.smartserver.draw

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View

class DrawView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var paint = Paint()

    //private var mPath = Path()

    init {
        paint.apply {
            color = ContextCompat.getColor(context, android.R.color.holo_green_light)
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 8f
            isAntiAlias = true
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawLine(0F, 0F, width.toFloat(), 0F, paint)
        canvas.drawLine(0F, height.toFloat(), width.toFloat(), height.toFloat(), paint)
        canvas.drawLine(0F, 0F, 0F, height.toFloat(), paint)
        canvas.drawLine(width.toFloat(), 0F, width.toFloat(), height.toFloat(), paint)

        canvas.drawLine(0F, 0F, width.toFloat(), height.toFloat(), paint)
        canvas.drawLine(width.toFloat(), 0F, 0F, height.toFloat(), paint)

    }
}