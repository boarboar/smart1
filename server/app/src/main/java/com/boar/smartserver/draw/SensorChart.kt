package com.boar.smartserver.draw

import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.util.AttributeSet
import android.view.View
import com.boar.smartserver.R
import com.boar.smartserver.UI.DateUtils.Companion.convertDateTime
import com.boar.smartserver.domain.SensorHistory
import android.util.Log
import com.boar.smartserver.UI.DateUtils.Companion.localDateTimeToMillis
import com.boar.smartserver.UI.DateUtils.Companion.millsToLocalDateTime
import kotlinx.android.synthetic.main.item_sensor_hist.view.*
import org.threeten.bp.LocalDateTime

import java.util.*


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

    var anythingToDisplay = false
    var timeMax : Long = 0
    var timeMin : Long = 0
    var timeStart : Long = 0
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

        if(!anythingToDisplay) return

        val nsteps =  (tempMax - tempMin)/TEMP_STEP_10
        if(nsteps==0) return // TODO  - horizontal line!


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

        val ncols = 24
        var x = left
        val dx = (w-left)/ncols
        for(col in 0..ncols) {
            if(col !=0 && col !=ncols)
                canvas.drawLine(x, top, x, h-bot, paint)
            canvas.drawText("$col", x, h,  paintText)
            x+=dx
        }

        canvas.drawText("${convertDateTime(timeMin)} .. $${convertDateTime(timeMax)} at ${convertDateTime(timeStart)}", left, h/2, paintText)
        canvas.drawText("$tempMin .. $tempMax" , left, h/2 + dy, paintText)

        /*
        var i = sensHist.indexOfFirst { it.timestamp > timeStart }
        if(i==-1) return

        while(i<sensHist.size) {
            val e = sensHist[i]
            Log.d("Chart", "$i ${convertDateTime(e.timestamp)}")
            i++
        }
        */

        // NOTE - timestamp DESC (right to left) !!!!

        paint.strokeWidth = 4f

        val chartw = w-left
        val charth = h -top - bot
        val chartw_l = 24*3600
        val charth_l = tempMax-tempMin
        val scale_x = chartw/chartw_l
        val scale_y = charth/charth_l

        val path = Path()
        var cnt = 0

        sensHist.forEach {
            if(it.timestamp > timeStart) {
                val sec_off = (it.timestamp - timeStart)/1000
                val x = sec_off*scale_x
                val deg_off = it.temp10 - tempMin
                val y = h-bot-deg_off*scale_y
                if(cnt==0) path.moveTo(x, y)
                else path.lineTo(x, y)

                cnt++
                //Log.d("Chart", "$x $y")
            }
        }

        canvas.drawPath(path, paint)
    }

    fun prepare() {

        anythingToDisplay = false
        // calculate time window

        // for 1 day so far

        val time24hrs = System.currentTimeMillis() - 24L * 3600 * 1000
        var dateStart = millsToLocalDateTime(time24hrs)
        dateStart = LocalDateTime.of(dateStart.year, dateStart.month, dateStart.dayOfMonth, dateStart.hour, 0)
        timeStart = localDateTimeToMillis(dateStart)

        Log.d("Chart", "Now ${convertDateTime(System.currentTimeMillis())}")
        Log.d("Chart", "Start ${convertDateTime(timeStart)}")


        timeMax = System.currentTimeMillis() - 30L * 24 * 3600 * 1000
        timeMin = System.currentTimeMillis() + 30L * 24 * 3600 * 1000
        tempMax = -273
        tempMin = 273
        var count = 0

        sensHist.forEach {
            if(it.timestamp > timeStart) { // should be in window
                if (it.timestamp > timeMax) timeMax = it.timestamp
                if (it.timestamp < timeMin) timeMin = it.timestamp
                if (it.temp10 > tempMax) tempMax = it.temp10
                if (it.temp10 < tempMin) tempMin = it.temp10
                count ++
            }
        }

        if(count<2)
            return

        anythingToDisplay = true
        var tmr = (tempMax/TEMP_STEP_10)  // rounded to 5 grad
        if(tmr * TEMP_STEP_10 < tempMax) tmr++
        tempMax = tmr*TEMP_STEP_10
        tmr = (tempMin/TEMP_STEP_10)  // rounded to 5 grad
        if(tmr * TEMP_STEP_10 > tempMin) tmr--
        tempMin = tmr*TEMP_STEP_10

    }
}