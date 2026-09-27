package cn.ppps.forwarder.entity.condition

import cn.ppps.forwarder.R
import java.io.Serializable

data class LocationSetting(
    var description: String = "",
    var type: String = "to",
    var calcType: String = "distance",
    var longitude: Double = 0.0,
    var latitude: Double = 0.0,
    var distance: Double = 0.0,
    var address: String = "",
) : Serializable {

    fun getCalcTypeCheckId(): Int {
        return when (calcType) {
            "distance" -> R.id.rb_calc_type_distance
            "address" -> R.id.rb_calc_type_address
            else -> R.id.rb_calc_type_distance
        }
    }

}
