package cn.ppps.forwarder.entity.result

@Suppress("PropertyName")
data class FeishuAppResult(
    var code: Long,
    var msg: String,
    var tenant_access_token: String?,
    var expire: Long?,
    var content: String?,
)