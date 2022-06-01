package tradly.social.common.base

import android.content.Context
import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

class DateTimeHelper {
    companion object {
        val FORMAT_EE_DD = "EE dd"
        val FORMAT_TIME_AM_PM = "hh:mm a"
        val FORMAT_DATE_DD_MM_YYYY = "dd/MM/yyyy"
        val FORMAT_DATE_D_MMM_YYYY = "dd MMM yyyy"
        val FORMAT_DATE_MMM_YYYY = "MMM,yyyy"
        val FORMAT_DATE_MMM_DD_YYYY = "MMM dd,yyyy"
        val FORMAT_DATE_D_MMM_YYYY_HH_MM = "dd, MMM yyyy h:mm a"
        val FORMAT_DATE_DD_MM_YYYY_HH_MM = "dd-MM-yyyy HH:mm a"
        val FORMAT_DATE_TIME_STANDARD = "dd-MM-yyyy HH:mm:ss a"
        val FORMAT_DATE_YYYY_MM_DD = "yyyy-MM-dd"
        val FORMAT_DATE_DD_MMMM_YYYY = "dd MMMM yyyy"
        val FORMAT_DATE_DD_MMM_YYYY = "dd MMM yyyy"
        val FORMAT_DATE_EEE_D_MMM_YYYY = "EEE, d MMM yyyy"
        val FORMAT_DATE_DD_MMM_YYYY_HH_A = "dd/MM/yyyy : HHa"
        val FORMAT_DATE_YYYY_MM_DD_HH_MM_SS_Z = "yyyy-MM-dd'T'HH:mm:ss'Z'"

        fun getDateFromTimeMillis(milliseconds: String, format: String): String {
            val time = milliseconds.toLong()
            val d = java.util.Date(time)
            return SimpleDateFormat(format,Locale.ENGLISH).format(d)
        }

        fun getDateFromTimeMillis(milliseconds: Long, format: String): String {
            val d = java.util.Date(milliseconds)
            return SimpleDateFormat(format,Locale.ENGLISH).format(d)
        }

        fun getDateString(date: Date, format: String): String {
            return SimpleDateFormat(format,Locale.ENGLISH).format(date)
        }

        fun getDateFromTimeMillis(locale:Locale,milliseconds: Long, format: String): String {
            val d = java.util.Date(milliseconds)
            return SimpleDateFormat(format,locale).format(d)
        }

        fun getDate(calendar: Calendar,timeZone:String = AppConstant.EMPTY, includeTime:Boolean = true, format: String, nextOffset:Int = 0):String{
            calendar.timeZone = if (timeZone.isNotEmpty())java.util.TimeZone.getTimeZone(timeZone) else java.util.TimeZone.getDefault()
            if (!includeTime){
                calendar.set(Calendar.HOUR_OF_DAY,0)
                calendar.set(Calendar.MINUTE,0)
                calendar.set(Calendar.SECOND,0)
                calendar.set(Calendar.MILLISECOND,0)
            }
            if (nextOffset!=0){
                calendar.add(Calendar.DAY_OF_YEAR,nextOffset)
            }
            return getDateFromTimeMillis(calendar.timeInMillis,format)
        }

        fun getTimeFromMinutes(value: Int):String{
            val hours = value/60
            val minutes = value%60
            return String.format("%02d:%02d",hours,minutes)
        }


        fun getMinuteHourPairFromMinutes(value: Int):Pair<Int,Int>{
            val hours = value/60
            val minutes = value%60
            return Pair(hours,minutes)
        }

        fun getMillisFromDateString(dateString: String,format: String):Long{
            return try{
                val sf = SimpleDateFormat(format,Locale.ENGLISH)
                val date = sf.parse(dateString)
                date!!.time
            }catch (ex:Exception){
                0L
            }
        }

        fun getTimeRange(range: String): Pair<String, String> = when (range) {
            TimeRangeType.LAST_WEEK -> {
                val endMillis = Calendar.getInstance().timeInMillis
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                Pair(
                    getDateFromTimeMillis(calendar.timeInMillis, FORMAT_DATE_YYYY_MM_DD),
                    getDateFromTimeMillis(endMillis, FORMAT_DATE_YYYY_MM_DD)
                )
            }
            TimeRangeType.LAST_MONTH -> {
                val endMillis = Calendar.getInstance().timeInMillis
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.MONTH, -1)
                Pair(
                    getDateFromTimeMillis(calendar.timeInMillis, FORMAT_DATE_YYYY_MM_DD),
                    getDateFromTimeMillis(endMillis, FORMAT_DATE_YYYY_MM_DD)
                )
            }
            TimeRangeType.LAST_YEAR -> {
                val endMillis = Calendar.getInstance().timeInMillis
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.YEAR, -1)
                Pair(
                    getDateFromTimeMillis(calendar.timeInMillis, FORMAT_DATE_YYYY_MM_DD),
                    getDateFromTimeMillis(endMillis, FORMAT_DATE_YYYY_MM_DD)
                )
            }
            else -> Pair("", "")
        }
    }

    object TimeZone{
        const val UTC = "UTC"
    }

    object TimeRangeType {
        const val ALL = "all"
        const val LAST_WEEK = "last_week"
        const val LAST_MONTH = "last_month"
        const val LAST_YEAR = "last_year"
    }

    object TimeAgo {
        private val SECOND_MILLIS = 1000
        private val MINUTE_MILLIS = 60 * SECOND_MILLIS
        private val HOUR_MILLIS = 60 * MINUTE_MILLIS
        private val DAY_MILLIS = 24 * HOUR_MILLIS

        fun getHours(fromMillis:Long):Long{
            var time = fromMillis
            if (time < 1000000000000L) {
                time *= 1000
            }
            val now = System.currentTimeMillis()
            val diff = now - time
            return (diff / HOUR_MILLIS)
        }

        fun getTimeAgo(context: Context, milliseconds: String): String? {

            var time = java.lang.Long.parseLong(milliseconds)
            if (time < 1000000000000L) {
                time *= 1000
            }

            val now = System.currentTimeMillis()
            if (time > now || time <= 0) {
                return null
            }
            val diff = now - time
            return if (diff < MINUTE_MILLIS) {
                context.getString(R.string.justNow)
            } else if (diff < 2 * MINUTE_MILLIS) {
                context.getString(R.string.a_minute_ago)
            } else if (diff < 50 * MINUTE_MILLIS) {
                (diff / MINUTE_MILLIS).toString() + " " + context.getString(R.string.minutesAgo)
            } else if (diff < 90 * MINUTE_MILLIS) {
                context.getString(R.string.an_hour_ago)
            } else if (diff < 24 * HOUR_MILLIS) {
                (diff / HOUR_MILLIS).toString() + " " + context.getString(R.string.hours_ago)
            } else if (diff < 48 * HOUR_MILLIS) {
                context.getString(R.string.yesterday)
            } else {
                getDateFromTimeMillis(time, FORMAT_DATE_DD_MMMM_YYYY)
            }
        }

        fun getDate(milliseconds: String): String {
            var time = java.lang.Long.parseLong(milliseconds)
            if (time < 1000000000000L) {
                time *= 1000
            }
            return if (DateUtils.isToday(time)) {
                getDateFromTimeMillis(time, FORMAT_TIME_AM_PM)
            } else
                getDateFromTimeMillis(time, FORMAT_DATE_D_MMM_YYYY)
        }
    }
}