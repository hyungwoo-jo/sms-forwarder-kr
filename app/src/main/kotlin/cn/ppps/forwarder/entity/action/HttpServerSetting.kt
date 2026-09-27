package cn.ppps.forwarder.entity.action

import cn.ppps.forwarder.utils.HttpServerUtils
import java.io.Serializable

data class HttpServerSetting(
    var description: String = "",
    var action: String = "start",
    var enableApiClone: Boolean = HttpServerUtils.enableApiClone,
    var enableApiSmsSend: Boolean = HttpServerUtils.enableApiSmsSend,
    var enableApiSmsQuery: Boolean = HttpServerUtils.enableApiSmsQuery,
    var enableApiCallQuery: Boolean = HttpServerUtils.enableApiCallQuery,
    var enableApiContactQuery: Boolean = HttpServerUtils.enableApiContactQuery,
    var enableApiContactAdd: Boolean = HttpServerUtils.enableApiContactAdd,
    var enableApiWol: Boolean = HttpServerUtils.enableApiWol,
    var enableApiLocation: Boolean = HttpServerUtils.enableApiLocation,
    var enableApiBatteryQuery: Boolean = HttpServerUtils.enableApiBatteryQuery,
) : Serializable
