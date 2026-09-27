package cn.ppps.forwarder.entity

import cn.ppps.forwarder.R
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CallInfo(
    var name: String = "",
    var number: String = "",
    var dateLong: Long = 0L,
    var duration: Int = 0,
    var type: Int = 1,
    @SerializedName("via_number")
    var viaNumber: String = "",
    @SerializedName("sim_id")
    var simId: Int = -1,
    @SerializedName("sub_id")
    var subId: Int = 0,
    @SerializedName("is_forwarded")
    var isForwarded: Boolean = false
) : Serializable {

    val typeImageId: Int
        get() {
            return when (type) {
                1 -> R.drawable.ic_phone_in
                2 -> R.drawable.ic_phone_out
                else -> R.drawable.ic_phone_missed
            }
        }

    val simImageId: Int
        get() {
            return when (simId) {
                0 -> R.drawable.ic_sim1
                1 -> R.drawable.ic_sim2
                else -> R.drawable.ic_sim
            }
        }

    override fun toString(): String {
        return "CallInfo{" +
                "name='" + name + '\'' +
                ", number='" + number + '\'' +
                ", dateLong=" + dateLong +
                ", duration=" + duration +
                ", type=" + type +
                ", viaNumber=" + viaNumber +
                ", simId=" + simId +
                '}'
    }
}