package cn.ppps.forwarder.entity.action

import java.io.Serializable

data class CleanerSetting(
    var description: String = "",
    var days: Int = 0,
) : Serializable
