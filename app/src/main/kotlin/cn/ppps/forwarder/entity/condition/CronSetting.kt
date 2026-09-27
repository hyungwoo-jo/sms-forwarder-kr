package cn.ppps.forwarder.entity.condition

import java.io.Serializable

data class CronSetting(
    var description: String = "",
    var expression: String = "",
) : Serializable
