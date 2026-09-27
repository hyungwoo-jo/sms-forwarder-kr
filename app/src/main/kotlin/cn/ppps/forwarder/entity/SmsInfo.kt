package cn.ppps.forwarder.entity

import com.google.gson.annotations.SerializedName
import cn.ppps.forwarder.R
import java.io.Serializable

data class SmsInfo(
    var name: String = "",
    var number: String = "",
    var content: String = "",
    var date: Long = 0L,
    var type: Int = 1,
    @SerializedName("sim_id")
    var simId: Int = -1,
    @SerializedName("sub_id")
    var subId: Int = 0,
) : Serializable {

    val typeImageId: Int = R.drawable.ic_sms

    val simImageId: Int
        get() {
            return when (simId) {
                0 -> R.drawable.ic_sim1
                1 -> R.drawable.ic_sim2
                else -> R.drawable.ic_sim
            }
        }
}
