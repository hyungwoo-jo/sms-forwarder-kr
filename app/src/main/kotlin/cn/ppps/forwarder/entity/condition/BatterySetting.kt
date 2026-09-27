package cn.ppps.forwarder.entity.condition

import android.os.BatteryManager
import cn.ppps.forwarder.R
import com.xuexiang.xutil.resource.ResUtils.getString
import java.io.Serializable

data class BatterySetting(
    var description: String = "",
    var status: Int = BatteryManager.BATTERY_STATUS_CHARGING,
    var levelMin: Int = 1,
    var levelMax: Int = 100,
    var keepReminding: Boolean = false,
) : Serializable {

    fun getStatusCheckId(): Int {
        return when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> R.id.rb_battery_charging
            BatteryManager.BATTERY_STATUS_DISCHARGING -> R.id.rb_battery_discharging
            else -> R.id.rb_battery_charging
        }
    }

    fun getMsg(statusNew: Int, levelNew: Int, levelOld: Int, batteryInfo: String): String {

        when (statusNew) {
            BatteryManager.BATTERY_STATUS_CHARGING, BatteryManager.BATTERY_STATUS_FULL -> {
                if (status != BatteryManager.BATTERY_STATUS_CHARGING) return ""
                if (keepReminding && levelOld < levelNew && levelNew >= levelMax) {
                    return String.format(getString(R.string.over_level_max), batteryInfo)
                } else if (!keepReminding && levelOld < levelNew && levelNew == levelMax) {
                    return String.format(getString(R.string.reach_level_max), batteryInfo)
                }
            }

            BatteryManager.BATTERY_STATUS_DISCHARGING, BatteryManager.BATTERY_STATUS_NOT_CHARGING -> {
                if (status != BatteryManager.BATTERY_STATUS_DISCHARGING) return ""
                if (keepReminding && levelOld > levelNew && levelNew <= levelMin) {
                    return String.format(getString(R.string.below_level_min), batteryInfo)
                } else if (!keepReminding && levelOld > levelNew && levelNew == levelMin) {
                    return String.format(getString(R.string.reach_level_min), batteryInfo)
                }
            }
        }

        return ""

    }
}
