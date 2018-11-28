package com.boar.smartserver.UI

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class DateUtils {
    companion object {
        private val df_time = SimpleDateFormat("HH:mm:ss")
        private val df_time_short = SimpleDateFormat("HH:mm")
        private val df_date = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
        private var df_dt = DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT, Locale.GERMAN)
        // Note: formatters are not threadsafe bit it's no issue since called from UIThread only
        fun convertDateTime(date: Long = System.currentTimeMillis()) : String {
            return df_dt.format(date)
        }
        fun convertTime(date: Long): String {
            return df_time.format(date)
        }
        fun convertTimeShort(date: Long): String {
            return df_time_short.format(date)
        }
        fun convertDate(date: Long): String {
            return df_date.format(date)
        }
    }

}