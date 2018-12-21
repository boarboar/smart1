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
import com.boar.smartserver.UI.DateUtils.Companion.convertTimeHOnly
import com.boar.smartserver.UI.DateUtils.Companion.convertTimeShort
import com.boar.smartserver.UI.DateUtils.Companion.localDateTimeToMillis
import com.boar.smartserver.UI.DateUtils.Companion.millsToLocalDateTime
import kotlinx.android.synthetic.main.item_sensor_hist.view.*
import org.threeten.bp.LocalDateTime

import java.util.*


class DrawView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    enum class DispType {
        TEMPERATURE, VCC
    }

    companion object {
        val TEMP_STEP_10 = 50 // 5 deg
        val VCC_STEP_1000 = 500 // 0.5v
        val LEFT_BORD = 50 //
        val FONT_SZ = 20
        val FONT_SZ_SMALL = 14
        val TOP_BORD = FONT_SZ
        val BOT_BORD = FONT_SZ
    }

    var sensHist : List<SensorHistory> = listOf()
    set (value) {
        field = value
        prepare()
    }

    var disp : DispType = DispType.TEMPERATURE
    var anythingToDisplay = false
    var timeMax : Long = 0
    var timeMin : Long = 0
    var timeStart : Long = 0
    var valMax : Int = 0
    var valMin : Int = 0
    private var paint = Paint()
    private var paintText = Paint()
    private var paintTextSmall = Paint()
    private var paintPath = Paint()
    //private var dash = DashPathEffect(floatArrayOf(300f, 100f), 0f)


    init {
        paint.apply {
            color = ContextCompat.getColor(context, android.R.color.holo_green_light)
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 4f
            isAntiAlias = true
        }
        paintPath.apply {
            color = ContextCompat.getColor(context, android.R.color.holo_blue_light)
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 2f
            isAntiAlias = true
        }
        paintText.apply {
            color = ContextCompat.getColor(context, android.R.color.holo_green_light)
            typeface = Typeface.MONOSPACE
            textSize = resources.displayMetrics.scaledDensity * FONT_SZ
        }
        paintTextSmall.apply {
            color = ContextCompat.getColor(context, android.R.color.holo_green_light)
            typeface = Typeface.MONOSPACE
            textSize = resources.displayMetrics.scaledDensity * FONT_SZ_SMALL
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
        //paint.pathEffect = null

        canvas.drawLine(left, top, w, top, paint)
        canvas.drawLine(left, h-bot, w, h-bot, paint)
        canvas.drawLine(left, top, left, h-bot, paint)
        canvas.drawLine(w, top, w, h-bot, paint)

        if(!anythingToDisplay) return

        val nsteps =  (valMax - valMin)/TEMP_STEP_10
        if(nsteps==0) return // TODO  - horizontal line!


        val dy = (h-top-bot)/nsteps
        var y = top
        var t = valMax


        //paint.strokeWidth = 1f

        for(row in 0..nsteps) {  // =1
            canvas.drawText(if (t>0) "+${t/10}" else if (t<0) "${t/10}" else " 0", 0F, y+td, paintText)
            if(row !=0 && row !=nsteps) {
                paint.strokeWidth = if(t==0) 4F else 1F
                canvas.drawLine(left, y, w, y, paint)
            }
            t -= TEMP_STEP_10
            y += dy
        }

        val ncols = 25
        var x = left
        val dx = (w-left)/ncols
        var time = timeStart
        for(col in 0..ncols) {
            if(col !=0 && col !=ncols)
                canvas.drawLine(x, top, x, h-bot, paint)
            canvas.drawText("${convertTimeHOnly(time)}", x, h,  paintText)
            x+=dx
            time += 3600L * 1000 // !!!!
        }

        canvas.drawText("${convertDateTime(timeMin)} .. $${convertDateTime(timeMax)} at ${convertDateTime(timeStart)}", left, h/2+dy, paintText)
        canvas.drawText("$valMin .. $valMax" , left, h/2 + dy*2, paintText)

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

        //paint.strokeWidth = 4f

        val chartw = w-left
        val charth = h -top - bot
        val chartw_l = 25*3600
        val charth_l = valMax-valMin
        val scale_x = chartw/chartw_l
        val scale_y = charth/charth_l

        val path = Path()
        var cnt = 0

        sensHist.forEach {
            if(it.timestamp > timeStart) {
                val sec_off = (it.timestamp - timeStart)/1000
                val x = left+sec_off*scale_x
                val deg_off = it.temp10 - valMin
                val y = h-bot-deg_off*scale_y
                if(cnt==0) path.moveTo(x, y)
                else path.lineTo(x, y)

                cnt++
                //Log.d("Chart", "$x $y")
            }
        }

        canvas.drawPath(path, paintPath)
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
        valMax = -273
        valMin = 273
        var count = 0

        sensHist.forEach {
            if(it.timestamp > timeStart) { // should be in window
                if (it.timestamp > timeMax) timeMax = it.timestamp
                if (it.timestamp < timeMin) timeMin = it.timestamp
                if (it.temp10 > valMax) valMax = it.temp10
                if (it.temp10 < valMin) valMin = it.temp10
                count ++
            }
        }

        if(count<2)
            return

        anythingToDisplay = true
        var tmr = (valMax/TEMP_STEP_10)  // rounded to 5 grad
        if(tmr * TEMP_STEP_10 < valMax) tmr++
        valMax = tmr*TEMP_STEP_10
        tmr = (valMin/TEMP_STEP_10)  // rounded to 5 grad
        if(tmr * TEMP_STEP_10 > valMin) tmr--
        valMin = tmr*TEMP_STEP_10

    }
}