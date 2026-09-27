package cn.ppps.forwarder.entity.action

import cn.ppps.forwarder.database.entity.Frpc
import java.io.Serializable

data class FrpcSetting(
    var description: String = "",
    var action: String = "start",
    var frpcList: List<Frpc>,
) : Serializable
