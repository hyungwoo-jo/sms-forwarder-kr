package cn.ppps.forwarder.entity.action

import java.io.Serializable

data class SmsSetting(
    var description: String = "",
    var simSlot: Int = 1,
    var phoneNumbers: String = "",
    var msgContent: String = "",
) : Serializable
