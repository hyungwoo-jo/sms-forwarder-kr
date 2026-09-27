package cn.ppps.forwarder.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Korean display text independent of the third-party Chinese date formatter. */
object KoreanTimeFormatter {
    fun format(time: Long, now: Long = System.currentTimeMillis(), zone: TimeZone = TimeZone.getDefault()): String {
        if (time <= 0L) return "기록 없음"
        val elapsed = now - time
        fun date(pattern: String): String = SimpleDateFormat(pattern, Locale.KOREAN).apply {
            timeZone = zone
        }.format(Date(time))
        if (elapsed < 0L) return date("yyyy-MM-dd HH:mm")
        if (elapsed < 1_000L) return "방금 전"
        if (elapsed < 60_000L) return "${elapsed / 1_000L}초 전"
        if (elapsed < 3_600_000L) return "${elapsed / 60_000L}분 전"
        val midnight = Calendar.getInstance(zone).apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (time >= midnight.timeInMillis) return "오늘 ${date("HH:mm")}"
        midnight.add(Calendar.DAY_OF_MONTH, -1)
        if (time >= midnight.timeInMillis) return "어제 ${date("HH:mm")}"
        return date("yyyy-MM-dd HH:mm")
    }
}
