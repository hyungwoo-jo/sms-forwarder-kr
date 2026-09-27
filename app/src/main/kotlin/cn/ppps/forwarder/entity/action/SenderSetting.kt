package cn.ppps.forwarder.entity.action

import cn.ppps.forwarder.database.entity.Sender
import java.io.Serializable

data class SenderSetting(
    var description: String = "",
    var status: Int = 1,
    var senderList: List<Sender>,
) : Serializable
