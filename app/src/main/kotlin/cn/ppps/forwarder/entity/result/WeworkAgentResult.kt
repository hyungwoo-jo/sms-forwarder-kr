package cn.ppps.forwarder.entity.result

@Suppress("PropertyName")
data class WeworkAgentResult(
    var errcode: Long,
    var errmsg: String,
    var access_token: String?,
    var expires_in: Long?,
    var msgid: String?,
)