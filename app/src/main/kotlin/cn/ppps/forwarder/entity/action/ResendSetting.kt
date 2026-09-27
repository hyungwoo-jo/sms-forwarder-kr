package cn.ppps.forwarder.entity.action

import java.io.Serializable

data class ResendSetting(
    var description: String = "",
    var hours: Int = 1,
    var statusList: List<Int> = listOf(0),
) : Serializable
