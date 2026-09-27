package cn.ppps.forwarder.utils

import android.annotation.SuppressLint
import cn.ppps.forwarder.entity.CallInfo
import cn.ppps.forwarder.entity.ContactInfo
import cn.ppps.forwarder.entity.SmsInfo
import com.xuexiang.xaop.annotation.MemoryCache
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

@Suppress("SameParameterValue")
object DataProvider {

    @JvmStatic
    @get:MemoryCache
    val emptySmsInfo: List<SmsInfo>
        get() {
            val list: MutableList<SmsInfo> = ArrayList()
            for (i in 0..5) {
                list.add(SmsInfo())
            }
            return list
        }

    @JvmStatic
    @get:MemoryCache
    val emptyCallInfo: List<CallInfo>
        get() {
            val list: MutableList<CallInfo> = ArrayList()
            for (i in 0..5) {
                list.add(CallInfo())
            }
            return list
        }

    @JvmStatic
    @get:MemoryCache
    val emptyContactInfo: List<ContactInfo>
        get() {
            val list: MutableList<ContactInfo> = ArrayList()
            for (i in 0..5) {
                list.add(ContactInfo())
            }
            return list
        }

    @JvmStatic
    @get:MemoryCache
    val timePeriodOption: List<String>
        get() {
            return getTimePeriod(24, 10)
        }

    /**
     *
     * @return
     */
    private fun getTimePeriod(totalHour: Int, interval: Int): List<String> {
        val list: MutableList<String> = ArrayList()
        var point: Int
        var hour: Int
        var min: Int
        for (i in 0..totalHour * 60 / interval) {
            point = i * interval
            hour = point / 60
            min = point - hour * 60
            list.add((if (hour <= 9) "0$hour" else "" + hour) + ":" + if (min <= 9) "0$min" else "" + min)
        }
        return list
    }

    /**
     */
    @SuppressLint("SimpleDateFormat")
    fun isCurrentTimeInPeriod(periodStartIndex: Int, periodEndIndex: Int): Boolean {
        val periodStartStr = timePeriodOption[periodStartIndex]
        val periodEndStr = timePeriodOption[periodEndIndex]

        val formatter = SimpleDateFormat("HH:mm")

        val periodStart = Calendar.getInstance().apply {
            time = formatter.parse(periodStartStr) as Date
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val periodEnd = Calendar.getInstance().apply {
            time = formatter.parse(periodEndStr) as Date
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val currentTime = Calendar.getInstance()
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
        val currentMinute = currentTime.get(Calendar.MINUTE)

        return if (periodEnd.before(periodStart)) {
            (currentHour > periodStart.get(Calendar.HOUR_OF_DAY) || (currentHour == periodStart.get(Calendar.HOUR_OF_DAY) && currentMinute >= periodStart.get(Calendar.MINUTE))) ||
                    (currentHour < periodEnd.get(Calendar.HOUR_OF_DAY) || (currentHour == periodEnd.get(Calendar.HOUR_OF_DAY) && currentMinute < periodEnd.get(Calendar.MINUTE)))
        } else {
            (currentHour > periodStart.get(Calendar.HOUR_OF_DAY) || (currentHour == periodStart.get(Calendar.HOUR_OF_DAY) && currentMinute >= periodStart.get(Calendar.MINUTE))) &&
                    (currentHour < periodEnd.get(Calendar.HOUR_OF_DAY) || (currentHour == periodEnd.get(Calendar.HOUR_OF_DAY) && currentMinute < periodEnd.get(Calendar.MINUTE)))
        }
    }

}