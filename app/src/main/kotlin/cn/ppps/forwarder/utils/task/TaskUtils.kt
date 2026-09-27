package cn.ppps.forwarder.utils.task

import android.bluetooth.BluetoothAdapter
import android.os.BatteryManager
import cn.ppps.forwarder.R
import cn.ppps.forwarder.entity.LocationInfo
import cn.ppps.forwarder.utils.SP_BATTERY_HEALTH
import cn.ppps.forwarder.utils.SP_BATTERY_INFO
import cn.ppps.forwarder.utils.SP_BATTERY_LEVEL
import cn.ppps.forwarder.utils.SP_BATTERY_PCT
import cn.ppps.forwarder.utils.SP_BATTERY_PLUGGED
import cn.ppps.forwarder.utils.SP_BATTERY_STATUS
import cn.ppps.forwarder.utils.SP_BATTERY_TEMPERATURE
import cn.ppps.forwarder.utils.SP_BATTERY_VOLTAGE
import cn.ppps.forwarder.utils.SP_BLUETOOTH_STATE
import cn.ppps.forwarder.utils.SP_CONNECTED_DEVICE
import cn.ppps.forwarder.utils.SP_DATA_SIM_SLOT
import cn.ppps.forwarder.utils.SP_DISCOVERED_DEVICES
import cn.ppps.forwarder.utils.SP_IPV4
import cn.ppps.forwarder.utils.SP_IPV6
import cn.ppps.forwarder.utils.SP_IP_LIST
import cn.ppps.forwarder.utils.SP_LOCATION_INFO_NEW
import cn.ppps.forwarder.utils.SP_LOCATION_INFO_OLD
import cn.ppps.forwarder.utils.SP_LOCK_SCREEN_ACTION
import cn.ppps.forwarder.utils.SP_NETWORK_STATE
import cn.ppps.forwarder.utils.SP_SIM_STATE
import cn.ppps.forwarder.utils.SP_WIFI_SSID
import cn.ppps.forwarder.utils.SharedPreference
import cn.ppps.forwarder.utils.TASK_ACTION_ALARM
import cn.ppps.forwarder.utils.TASK_ACTION_CLEANER
import cn.ppps.forwarder.utils.TASK_ACTION_FRPC
import cn.ppps.forwarder.utils.TASK_ACTION_HTTPSERVER
import cn.ppps.forwarder.utils.TASK_ACTION_NOTIFICATION
import cn.ppps.forwarder.utils.TASK_ACTION_RESEND
import cn.ppps.forwarder.utils.TASK_ACTION_RULE
import cn.ppps.forwarder.utils.TASK_ACTION_SENDER
import cn.ppps.forwarder.utils.TASK_ACTION_SENDSMS
import cn.ppps.forwarder.utils.TASK_ACTION_SETTINGS
import cn.ppps.forwarder.utils.TASK_ACTION_TASK
import cn.ppps.forwarder.utils.TASK_CONDITION_APP
import cn.ppps.forwarder.utils.TASK_CONDITION_BATTERY
import cn.ppps.forwarder.utils.TASK_CONDITION_BLUETOOTH
import cn.ppps.forwarder.utils.TASK_CONDITION_CALL
import cn.ppps.forwarder.utils.TASK_CONDITION_CHARGE
import cn.ppps.forwarder.utils.TASK_CONDITION_CRON
import cn.ppps.forwarder.utils.TASK_CONDITION_LEAVE_ADDRESS
import cn.ppps.forwarder.utils.TASK_CONDITION_LOCK_SCREEN
import cn.ppps.forwarder.utils.TASK_CONDITION_NETWORK
import cn.ppps.forwarder.utils.TASK_CONDITION_SIM
import cn.ppps.forwarder.utils.TASK_CONDITION_SMS
import cn.ppps.forwarder.utils.TASK_CONDITION_TO_ADDRESS

/**
 */
class TaskUtils private constructor() {

    companion object {

        fun getTypeImageId(type: Int): Int {
            return when (type) {
                TASK_CONDITION_CRON -> R.drawable.auto_task_icon_custom_time
                TASK_CONDITION_TO_ADDRESS -> R.drawable.auto_task_icon_to_address
                TASK_CONDITION_LEAVE_ADDRESS -> R.drawable.auto_task_icon_leave_address
                TASK_CONDITION_NETWORK -> R.drawable.auto_task_icon_network
                TASK_CONDITION_SIM -> R.drawable.auto_task_icon_sim
                TASK_CONDITION_BATTERY -> R.drawable.auto_task_icon_battery
                TASK_CONDITION_CHARGE -> R.drawable.auto_task_icon_charge
                TASK_CONDITION_LOCK_SCREEN -> R.drawable.auto_task_icon_lock_screen
                TASK_CONDITION_SMS -> R.drawable.auto_task_icon_sms
                TASK_CONDITION_CALL -> R.drawable.auto_task_icon_incall
                TASK_CONDITION_APP -> R.drawable.auto_task_icon_start_activity
                TASK_CONDITION_BLUETOOTH -> R.drawable.auto_task_icon_bluetooth
                TASK_ACTION_SENDSMS -> R.drawable.auto_task_icon_sms
                TASK_ACTION_NOTIFICATION -> R.drawable.auto_task_icon_notification
                TASK_ACTION_CLEANER -> R.drawable.auto_task_icon_cleaner
                TASK_ACTION_SETTINGS -> R.drawable.auto_task_icon_settings
                TASK_ACTION_FRPC -> R.drawable.auto_task_icon_frpc
                TASK_ACTION_HTTPSERVER -> R.drawable.auto_task_icon_http_server
                TASK_ACTION_RULE -> R.drawable.auto_task_icon_rule
                TASK_ACTION_SENDER -> R.drawable.auto_task_icon_sender
                TASK_ACTION_ALARM -> R.drawable.auto_task_icon_alarm
                TASK_ACTION_RESEND -> R.drawable.auto_task_icon_resend
                TASK_ACTION_TASK -> R.drawable.auto_task_icon_task
                else -> R.drawable.auto_task_icon_custom_time
            }
        }

        fun getTypeGreyImageId(type: Int): Int {
            return when (type) {
                TASK_CONDITION_CRON -> R.drawable.auto_task_icon_custom_time_grey
                TASK_CONDITION_TO_ADDRESS -> R.drawable.auto_task_icon_to_address_grey
                TASK_CONDITION_LEAVE_ADDRESS -> R.drawable.auto_task_icon_leave_address_grey
                TASK_CONDITION_NETWORK -> R.drawable.auto_task_icon_network_grey
                TASK_CONDITION_SIM -> R.drawable.auto_task_icon_sim_grey
                TASK_CONDITION_BATTERY -> R.drawable.auto_task_icon_battery_grey
                TASK_CONDITION_CHARGE -> R.drawable.auto_task_icon_charge_grey
                TASK_CONDITION_LOCK_SCREEN -> R.drawable.auto_task_icon_lock_screen_grey
                TASK_CONDITION_SMS -> R.drawable.auto_task_icon_sms_grey
                TASK_CONDITION_CALL -> R.drawable.auto_task_icon_incall_grey
                TASK_CONDITION_APP -> R.drawable.auto_task_icon_start_activity_grey
                TASK_CONDITION_BLUETOOTH -> R.drawable.auto_task_icon_bluetooth_grey
                TASK_ACTION_SENDSMS -> R.drawable.auto_task_icon_sms_grey
                TASK_ACTION_NOTIFICATION -> R.drawable.auto_task_icon_notification_grey
                TASK_ACTION_CLEANER -> R.drawable.auto_task_icon_cleaner_grey
                TASK_ACTION_SETTINGS -> R.drawable.auto_task_icon_settings_grey
                TASK_ACTION_FRPC -> R.drawable.auto_task_icon_frpc_grey
                TASK_ACTION_HTTPSERVER -> R.drawable.auto_task_icon_http_server_grey
                TASK_ACTION_RULE -> R.drawable.auto_task_icon_rule_grey
                TASK_ACTION_SENDER -> R.drawable.auto_task_icon_sender_grey
                TASK_ACTION_ALARM -> R.drawable.auto_task_icon_alarm_grey
                TASK_ACTION_RESEND -> R.drawable.auto_task_icon_resend_grey
                TASK_ACTION_TASK -> R.drawable.auto_task_icon_task_grey
                else -> R.drawable.auto_task_icon_custom_time_grey
            }
        }

        var batteryInfo: String by SharedPreference(SP_BATTERY_INFO, "")

        var batteryLevel: Int by SharedPreference(SP_BATTERY_LEVEL, 0)

        var batteryPct: Float by SharedPreference(SP_BATTERY_PCT, 0.00F)

        var batteryStatus: Int by SharedPreference(SP_BATTERY_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)

        var batteryPlugged: Int by SharedPreference(SP_BATTERY_PLUGGED, BatteryManager.BATTERY_PLUGGED_AC)

        var batteryVoltage: Int by SharedPreference(SP_BATTERY_VOLTAGE, 0)

        var batteryHealth: Int by SharedPreference(SP_BATTERY_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)

        var batteryTemperature: Int by SharedPreference(SP_BATTERY_TEMPERATURE, 0)

        var networkState: Int by SharedPreference(SP_NETWORK_STATE, 0)

        var dataSimSlot: Int by SharedPreference(SP_DATA_SIM_SLOT, 0)

        var wifiSsid: String by SharedPreference(SP_WIFI_SSID, "")

        var ipv4: String by SharedPreference(SP_IPV4, "")

        var ipv6: String by SharedPreference(SP_IPV6, "")

        var ipList: String by SharedPreference(SP_IP_LIST, "")

        var simState: Int by SharedPreference(SP_SIM_STATE, 0)

        var locationInfoOld: LocationInfo by SharedPreference(SP_LOCATION_INFO_OLD, LocationInfo())

        var locationInfoNew: LocationInfo by SharedPreference(SP_LOCATION_INFO_NEW, LocationInfo())

        var lockScreenAction: String by SharedPreference(SP_LOCK_SCREEN_ACTION, "")

        var discoveredDevices: MutableMap<String, String> by SharedPreference(SP_DISCOVERED_DEVICES, mutableMapOf())

        var connectedDevices: MutableMap<String, String> by SharedPreference(SP_CONNECTED_DEVICE, mutableMapOf())

        var bluetoothState: Int by SharedPreference(SP_BLUETOOTH_STATE, BluetoothAdapter.STATE_ON)

    }
}