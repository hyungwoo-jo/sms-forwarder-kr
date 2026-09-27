package cn.ppps.forwarder.entity

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SimInfo(
    @SerializedName("carrier_name")
    var mCarrierName: String? = null,
    @SerializedName("icc_id")
    var mIccId: String? = null,
    @SerializedName("sim_slot_index")
    var mSimSlotIndex: Int = 0,
    @SerializedName("number")
    var mNumber: String? = null,
    @SerializedName("country_iso")
    var mCountryIso: String? = null,
    @SerializedName("subscription_id")
    var mSubscriptionId: Int = 0,
) : Serializable {
    override fun toString(): String {
        return "SimInfo{" +
                "mCarrierName=" + mCarrierName +
                ", mIccId=" + mIccId +
                ", mSimSlotIndex=" + mSimSlotIndex +
                ", mNumber=" + mNumber +
                ", mCountryIso=" + mCountryIso +
                ", mSubscriptionId=" + mSubscriptionId +
                '}'
    }
}
