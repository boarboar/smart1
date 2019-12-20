package com.example.android.weatherapp.draw

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.example.android.weatherapp.domain.Sensor
import com.example.android.weatherapp.domain.SensorData
import com.example.android.weatherapp.utils.DateUtils.Companion.convertDateDOnly
import com.example.android.weatherapp.utils.DateUtils.Companion.convertTimeHOnly
import com.example.android.weatherapp.utils.DateUtils.Companion.localDateTimeToMillis
import com.example.android.weatherapp.utils.DateUtils.Companion.millsToLocalDateTime
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDateTime


class DrawView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    enum class DispType {
        TEMPERATURE, VCC, HUMIDITY, LEAKAGE
    }

    enum class DispPeriod {
        DAY, WEEK, MONTH
    }

    companion object {
        //val TEMP_STEP_10 = 50 // 5 deg
        //val VCC_STEP_1000 = 500 // 0.5v
        val LEFT_BORD = 50 //
        val FONT_SZ = 20
        val FONT_SZ_SMALL = 14
        val TOP_BORD = FONT_SZ
        val BOT_BORD = FONT_SZ
        val CIRCLE_RAD = 5
    }

    var sensHist : List<SensorData> = listOf()
        /*
    set (value) {
        field = value
        prepare()
    }
*/

    var disp : DispType = DispType.TEMPERATURE
        set (value) {
            field = value
            prepare()
        }

    var dispPeriod : DispPeriod = DispPeriod.DAY
        set (value) {
            field = value
            prepare()
        }

    private var anythingToDisplay = false
    private var timeMax : Long = 0
    private var timeMin : Long = 0
    private var timeStart : Long = 0
    private var valMax : Int = 0
    private var valMin : Int = 0
    private var valStep : Int = 1
    private var ncols : Int = 25
    private var periodSec : Long = 25L * 3600
    private var colStepSec : Long = 3600L

    private var paint = Paint()
    private var paintText = Paint()
    private var paintTextSmall = Paint()
    private var paintPath = Paint()
    private val color_green = ContextCompat.getColor(context, android.R.color.holo_green_light)
    private val color_red = ContextCompat.getColor(context, android.R.color.holo_red_light)
    private val color_blue = ContextCompat.getColor(context, android.R.color.holo_blue_light)

    val getTemp = { it: SensorData -> it.temp.toInt() }
    val getVcc = { it: SensorData -> it.vcc.toInt() }
    val getHumidity = { it: SensorData -> it.hum.toInt() }
    var getVal : (SensorData) -> Int = getTemp

    init {
        paint.apply {
            color = color_green
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 4f
            isAntiAlias = true
        }
        paintPath.apply {
            color = color_blue
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 4f
            isAntiAlias = true
        }
        paintText.apply {
            color = color_green
            typeface = Typeface.MONOSPACE
            textSize = resources.displayMetrics.scaledDensity * FONT_SZ
        }
        paintTextSmall.apply {
            color = color_green
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

        Log.d("Chart", "Type $disp, Priod $dispPeriod")

        paint.color=color_green
        paint.strokeWidth = 4f
        //paint.pathEffect = null

        canvas.drawLine(left, top, w, top, paint)
        canvas.drawLine(left, h-bot, w, h-bot, paint)
        canvas.drawLine(left, top, left, h-bot, paint)
        canvas.drawLine(w, top, w, h-bot, paint)

        if(!anythingToDisplay) return

        val nsteps =  (valMax - valMin)/valStep
        if(nsteps==0) return // TODO  - horizontal line!


        val dy = (h-top-bot)/nsteps
        var y = top
        var t = valMax


        //paint.strokeWidth = 1f

        for(row in 0..nsteps) {  // =1
            if(disp==DispType.TEMPERATURE)
                canvas.drawText(if (t>0) "+${t/10}" else if (t<0) "${t/10}" else " 0", 0F, y+td, paintText)
            else
                canvas.drawText("${(t/10).toFloat()/100F}", 0F, y+td, paintText)
            if(row !=0 && row !=nsteps) {
                if(disp==DispType.TEMPERATURE) {
                    paint.color=color_green
                    paint.strokeWidth = if (t == 0) 4F else 1F
                }
                else if(disp==DispType.HUMIDITY) {
                    paint.strokeWidth = 1F
                    paint.color= if(t==900) color_red else color_green
                }
                else { // VCC
                    paint.strokeWidth = if(t== Sensor.VCC_LOW_1000) 4F else 1F
                    paint.color= if(t==Sensor.VCC_LOW_1000) color_red else color_green
                }
                canvas.drawLine(left, y, w, y, paint)
            }
            t -= valStep
            y += dy
        }

        var x = left
        val dx = (w-left)/ncols
        var time = timeStart

        paint.strokeWidth = 1f
        paint.color=color_green

        for(col in 0..ncols) {
            var date = millsToLocalDateTime(time)
            if(col !=0 && col !=ncols) {
                paint.color = if(dispPeriod!=DispPeriod.DAY &&
                        (date.dayOfWeek==DayOfWeek.SUNDAY || date.dayOfWeek==DayOfWeek.MONDAY))
                    color_red else color_green
                canvas.drawLine(x, top, x, h - bot, paint)
            }
            if(dispPeriod==DispPeriod.DAY)
                canvas.drawText("${convertTimeHOnly(time)}", x, h,  paintText)
            else {
                paintText.color = if(date.dayOfWeek==DayOfWeek.SUNDAY) color_red else color_green
                canvas.drawText("${convertDateDOnly(time)}", x, h, paintText)
            }
            x+=dx
            time += colStepSec * 1000
        }

        paint.color=color_green
        //paintText.color = color_blue
        //canvas.drawText("${convertDateTime(timeMin)} .. ${convertDateTime(timeMax)} at ${convertDateTime(timeStart)}", left, h/4, paintText)
        //canvas.drawText("$valMin .. $valMax" , left, h*3/4, paintText)
        paintText.color = color_green

        val chartw = w-left
        val charth = h -top - bot
        val chartw_l = periodSec
        val charth_l = valMax-valMin
        val scale_x = chartw/chartw_l
        val scale_y = charth/charth_l

        val path = Path()

        var prevVal = 0

        for(i in 0..sensHist.size-1) {
            val it = sensHist[i]
            if(it.timestamp > timeStart) {
                val sec_off = (it.timestamp - timeStart)/1000
                val x = left+sec_off*scale_x
                val v = if(dispPeriod == DispPeriod.DAY) getVal(it)
                        else when(i) { // moving 3-average
                            0, sensHist.size-1 ->
                                getVal(it)
                            1, sensHist.size-2 -> // moving average 3
                                (getVal(sensHist[i-1]) + getVal(it) + getVal(sensHist[i+1]))/3
                            2, sensHist.size-3 -> // moving average 5
                                (getVal(sensHist[i-2]) + getVal(sensHist[i-1]) + getVal(it)
                                        + getVal(sensHist[i+1]) + getVal(sensHist[i+2]))/5
                            else -> // moving average 7
                                (getVal(sensHist[i-3]) + getVal(sensHist[i-2]) +
                                        getVal(sensHist[i-1]) + getVal(it)
                                        + getVal(sensHist[i+1]) + getVal(sensHist[i+2])  +
                                        getVal(sensHist[i+3]))/7
                        }
                val deg_off = v - valMin
                val y = h-bot-deg_off*scale_y

                val ignore = (disp==DispType.HUMIDITY && (prevVal==0 || getVal(it)==0))

                if(i==0 || ignore) path.moveTo(x, y)
                else path.lineTo(x, y)

                prevVal = getVal(it)

                // dislay leackage for humidity mode
                if(disp==DispType.HUMIDITY) {
                    if(it.dhum.toInt() == SensorData.DHUM_VALS.LEAK.value) {
                        paint.color = color_red
                        paint.strokeWidth = 4f
                        canvas.drawCircle (x, top+CIRCLE_RAD, CIRCLE_RAD.toFloat(), paint)
                    }
                }

            }
        }

        canvas.drawPath(path, paintPath)
    }

    fun prepare() {
        anythingToDisplay = false
        var offset=0L
        if(dispPeriod == DispPeriod.DAY) {
            offset = 24L * 3600 * 1000
            ncols = 25
            periodSec = 25*3600
            colStepSec = 3600L
            var dateStart = millsToLocalDateTime(System.currentTimeMillis()-offset)
            dateStart = LocalDateTime.of(dateStart.year, dateStart.month, dateStart.dayOfMonth, dateStart.hour, 0)
            timeStart = localDateTimeToMillis(dateStart)
        }
        else if(dispPeriod == DispPeriod.WEEK) {
            offset = 7L * 24L * 3600 * 1000
            ncols = 8
            periodSec = 8L*24*3600
            colStepSec = 24L*3600L
            var dateStart = millsToLocalDateTime(System.currentTimeMillis()-offset)
            dateStart = LocalDateTime.of(dateStart.year, dateStart.month, dateStart.dayOfMonth, 0, 0)
            timeStart = localDateTimeToMillis(dateStart)
        } else  {
            offset = 30L * 24L * 3600 * 1000
            ncols = 31
            periodSec = 31L*24*3600
            colStepSec = 24L*3600L
            var dateStart = millsToLocalDateTime(System.currentTimeMillis()-offset)
            dateStart = LocalDateTime.of(dateStart.year, dateStart.month, dateStart.dayOfMonth, 0, 0)
            timeStart = localDateTimeToMillis(dateStart)
        }

        //Log.d("Chart", "Now ${convertDateTime(System.currentTimeMillis())}")
        //Log.d("Chart", "Start ${convertDateTime(timeStart)}")

        timeMax = System.currentTimeMillis() - 300L * 24 * 3600 * 1000
        timeMin = System.currentTimeMillis() + 300L * 24 * 3600 * 1000
        valMin = Int.MAX_VALUE
        valMax = Int.MIN_VALUE
        var count = 0

        getVal = when(disp) {
            DispType.TEMPERATURE -> getTemp
            DispType.VCC -> getVcc
            else -> getHumidity
        }

        sensHist.forEach {
            if(it.timestamp > timeStart) { // should be in window
                if (it.timestamp > timeMax) timeMax = it.timestamp
                if (it.timestamp < timeMin) timeMin = it.timestamp
                val v = getVal(it)
                if (v > valMax) valMax = v
                if (v < valMin) valMin = v
                count ++
            }
        }

        if(count<2)
            return

        anythingToDisplay = true

        if(disp==DispType.TEMPERATURE) {

            if (valMax < 0 && valMin < 0) {
                valMax = 0
            } else if (valMax > 0 && valMin > 0) {
                valMin = 0
            } else if (valMax == 0 && valMin == 0) {
                valMax = 5
                valMin = -5
            }

            valStep = when {
                valMax - valMin <= 5 -> 10
                valMax - valMin <= 10 -> 20
                else -> 50
            }
        } else if(disp==DispType.VCC) {
            valMin = 0
            valMax = 5000
            valStep = 500
        } else { // HUMIDITY
            valMin = 0
            valMax = 1000
            valStep = 100
        }

        var tmr = (valMax/valStep)  // rounded to 5 grad
        if(tmr * valStep < valMax) tmr++
        valMax = tmr*valStep
        tmr = (valMin/valStep)  // rounded to 5 grad
        if(tmr * valStep > valMin) tmr--
        valMin = tmr*valStep

    }
}