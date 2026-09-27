package cn.ppps.forwarder.server.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CallQueryData(
    @SerializedName("type")
    var type: Int = 1,
    @SerializedName("page_num")
    var pageNum: Int = 1,
    @SerializedName("page_size")
    var pageSize: Int = 10,
    @SerializedName("phone_number")
    var phoneNumber: String? = "",
) : Serializable