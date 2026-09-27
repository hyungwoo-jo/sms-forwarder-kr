package cn.ppps.forwarder.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class KoreanTimeFormatterTest {
    private val zone = TimeZone.getTimeZone("Asia/Seoul")
    private fun timestamp(value: String) = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREAN).apply {
        timeZone = zone
    }.parse(value)!!.time

    @Test fun relativeUnitsAreKorean() {
        val now = timestamp("2026-09-27 12:00:00")
        assertEquals("방금 전", KoreanTimeFormatter.format(now, now, zone))
        assertEquals("59초 전", KoreanTimeFormatter.format(now - 59_000, now, zone))
        assertEquals("1분 전", KoreanTimeFormatter.format(now - 60_000, now, zone))
        assertEquals("59분 전", KoreanTimeFormatter.format(now - 3_599_000, now, zone))
        assertEquals("오늘 11:00", KoreanTimeFormatter.format(now - 3_600_000, now, zone))
    }

    @Test fun calendarDayBoundaryUsesDeviceTimezone() {
        val now = timestamp("2026-09-27 00:30:00")
        assertEquals("어제 23:00", KoreanTimeFormatter.format(timestamp("2026-09-26 23:00:00"), now, zone))
        assertEquals("2026-09-25 23:00", KoreanTimeFormatter.format(timestamp("2026-09-25 23:00:00"), now, zone))
    }

    @Test fun unsetAndFutureTimesDoNotProduceNegativeRelativeText() {
        val now = timestamp("2026-09-27 12:00:00")
        assertEquals("기록 없음", KoreanTimeFormatter.format(0, now, zone))
        assertEquals("2026-09-27 12:01", KoreanTimeFormatter.format(now + 60_000, now, zone))
    }
}
