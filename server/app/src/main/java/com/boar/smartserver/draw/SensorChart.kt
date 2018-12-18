package com.boar.smartserver.draw

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.util.AttributeSet
import android.view.View
import com.boar.smartserver.R
import com.boar.smartserver.UI.DateUtils.Companion.convertDateTime
import com.boar.smartserver.domain.SensorHistory
import android.graphics.DashPathEffect



class DrawView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    companion object {
        val TEMP_STEP_10 = 50 // 5 deg
        val LEFT_BORD = 50 //
        val FONT_SZ = 20
        val TOP_BORD = FONT_SZ
        val BOT_BORD = FONT_SZ
    }

    var sensHist : List<SensorHistory> = listOf()
    set (value) {
        field = value
        prepare()
    }

    var timeMax : Long = 0
    var timeMin : Long = 0
    var tempMax : Int = 0
    var tempMin : Int = 0
    private var paint = Paint()
    private var paintText = Paint()
    private var dash = DashPathEffect(floatArrayOf(300f, 100f), 0f)


    init {
        paint.apply {
            color = ContextCompat.getColor(context, android.R.color.holo_green_light)
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 4f
            isAntiAlias = true
        }
        paintText.apply {
            color = ContextCompat.getColor(context, android.R.color.holo_green_light)
            //style = Paint.Style.STROKE
            //strokeJoin = Paint.Join.ROUND
            //strokeCap = Paint.Cap.ROUND
            //strokeWidth = 1f
            //isAntiAlias = true

            typeface = Typeface.MONOSPACE
            textSize = resources.displayMetrics.scaledDensity * FONT_SZ
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val left = resources.displayMetrics.scaledDensity * LEFT_BORD
        //val top = resources.displayMetrics.scaledDensity * TOP_BORD
        val top = 0F
        val bot = resources.displayMetrics.scaledDensity * BOT_BORD
        val td = resources.displayMetrics.scaledDensity * FONT_SZ

        paint.strokeWidth = 4f
        paint.pathEffect = null

        canvas.drawLine(left, top, w, top, paint)
        canvas.drawLine(left, h-bot, w, h-bot, paint)
        canvas.drawLine(left, top, left, h-bot, paint)
        canvas.drawLine(w, top, w, h-bot, paint)

        //canvas.drawLine(0F, 0F, w, h, paint)
        //canvas.drawLine(w, 0F, 0F, h, paint)

        val nsteps =  (tempMax - tempMin)/TEMP_STEP_10
        if(nsteps==0) return // TODO
        val dy = (h-top-bot)/nsteps
        var y = top
        var t = tempMax

        paint.strokeWidth = 1f
        paint.pathEffect = dash

        for(row in 0..nsteps) {  // =1
            canvas.drawText(if (t>0) "+${t/10}" else if (t<0) "${t/10}" else " 0", 0F, y+td, paintText)
            if(row !=0 && row !=nsteps)
                canvas.drawLine(left, y, w, y, paint)
            t -= TEMP_STEP_10
            y += dy
        }

        canvas.drawText("${convertDateTime(timeMin)} .. $${convertDateTime(timeMax)}   $tempMin .. $tempMax", 0F, h/2, paintText)

    }

    fun prepare() {
        timeMax = System.currentTimeMillis() - 30L * 24 * 3600 * 1000
        timeMin = System.currentTimeMillis() + 30L * 24 * 3600 * 1000
        tempMax = -273
        tempMin = 273

        sensHist.forEach {
            if(it.timestamp > timeMax) timeMax=it.timestamp
            if(it.timestamp < timeMin) timeMin=it.timestamp
            if(it.temp10 > tempMax) tempMax=it.temp10
            if(it.temp10 < tempMin) tempMin=it.temp10
        }

        var tmr = (tempMax/TEMP_STEP_10)  // rounded to 5 grad
        if(tmr * TEMP_STEP_10 < tempMax) tmr++
        tempMax = tmr*TEMP_STEP_10
        tmr = (tempMin/TEMP_STEP_10)  // rounded to 5 grad
        if(tmr * TEMP_STEP_10 > tempMin) tmr--
        tempMin = tmr*TEMP_STEP_10


    }
}