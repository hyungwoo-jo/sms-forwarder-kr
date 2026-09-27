package cn.ppps.forwarder.entity.result

data class GotifyResult(
    var errorCode: Long?,
    var error: String?,
    var errorDescription: String?,
    var id: Long?,
    var appid: Long?,
    var title: String?,
    var message: String?,
    var priority: Long?,
    var date: String?,
)