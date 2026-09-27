package cn.ppps.forwarder.utils

object CactusSave {
    var timer: Long by SharedPreference(CACTUS_TIMER, 0L)

    var lastTimer: Long by SharedPreference(CACTUS_LAST_TIMER, 0L)

    var date: String by SharedPreference(CACTUS_DATE, "0000-01-01 00:00:00")

    var endDate: String by SharedPreference(CACTUS_END_DATE, "0000-01-01 00:00:00")
}