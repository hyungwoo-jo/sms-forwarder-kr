package cn.ppps.forwarder.server.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SmsQueryData(
    var type: Int = 1,
    @SerializedName("page_num")
    var pageNum: Int = 1,
    @SerializedName("page_size")
    var pageSize: Int = 10,
    var keyword: String = "",
) : Serializable