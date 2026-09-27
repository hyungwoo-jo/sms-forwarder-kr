package cn.ppps.forwarder.utils

import android.location.Criteria
import cn.ppps.forwarder.R
import com.xuexiang.xutil.resource.ResUtils.getString

class SettingUtils private constructor() {
    companion object {

        var isAgreePrivacy: Boolean by SharedPreference(IS_AGREE_PRIVACY_KEY, false)

        var enableSms: Boolean by SharedPreference(SP_ENABLE_SMS, false)

        var enablePhone: Boolean by SharedPreference(SP_ENABLE_PHONE, false)

        var enableContactNames: Boolean by SharedPreference("enable_contact_names", false)
        var enableAutomation: Boolean by SharedPreference("enable_safe_automation", false)

        var enableCallType1: Boolean by SharedPreference(SP_ENABLE_CALL_TYPE_1, false)

        var enableCallType2: Boolean by SharedPreference(SP_ENABLE_CALL_TYPE_2, false)

        var enableCallType3: Boolean by SharedPreference(SP_ENABLE_CALL_TYPE_3, false)

        var enableCallType4: Boolean by SharedPreference(SP_ENABLE_CALL_TYPE_4, false)

        var enableCallType5: Boolean by SharedPreference(SP_ENABLE_CALL_TYPE_5, false)

        var enableCallType6: Boolean by SharedPreference(SP_ENABLE_CALL_TYPE_6, false)

        var enableAppNotify: Boolean by SharedPreference(SP_ENABLE_APP_NOTIFY, false)

        var enableSmsCommand: Boolean
            get() = false
            set(value) { /* Removed feature: old backups cannot re-enable it. */ }
        var smsCommandSafePhone: String by SharedPreference(SP_SMS_COMMAND_SAFE_PHONE, "")

        var enableCloseToEarpieceTurnOffScreen: Boolean by SharedPreference(SP_ENABLE_CLOSE_TO_EARPIECE_TURN_OFF_SCREEN, false)

        var enableCancelAppNotify: Boolean by SharedPreference(SP_ENABLE_CANCEL_APP_NOTIFY, false)

        var cancelExtraAppNotify: String by SharedPreference(SP_CANCEL_EXTRA_APP_NOTIFY, "")

        var appNotifyBlacklist: String by SharedPreference(SP_APP_NOTIFY_BLACKLIST, "")

        var enableNotUserPresent: Boolean by SharedPreference(SP_ENABLE_NOT_USER_PRESENT, false)

        var enableLoadAppList: Boolean by SharedPreference(ENABLE_LOAD_APP_LIST, true)

        var enableLoadUserAppList: Boolean by SharedPreference(ENABLE_LOAD_USER_APP_LIST, true)

        var enableLoadSystemAppList: Boolean by SharedPreference(ENABLE_LOAD_SYSTEM_APP_LIST, false)

        var duplicateMessagesLimits: Int by SharedPreference(SP_DUPLICATE_MESSAGES_LIMITS, 0)

        var silentPeriodStart: Int by SharedPreference(SP_SILENT_PERIOD_START, 0)

        var silentPeriodEnd: Int by SharedPreference(SP_SILENT_PERIOD_END, 0)

        var enableSilentPeriodLogs: Boolean by SharedPreference(SP_ENABLE_SILENT_PERIOD_LOGS, false)

        var enableExcludeFromRecents: Boolean by SharedPreference(SP_ENABLE_EXCLUDE_FROM_RECENTS, false)

        var enableCactus: Boolean by SharedPreference(SP_ENABLE_CACTUS, false)

        // Phone-number region lookup contacts a third-party service and is off by default.
        var enablePhoneAreaLookup: Boolean
            get() = false
            set(value) { /* Removed feature: old backups cannot re-enable it. */ }

        var enablePlaySilenceMusic: Boolean by SharedPreference(SP_ENABLE_PLAY_SILENCE_MUSIC, false)

        var enableOnePixelActivity: Boolean by SharedPreference(SP_ENABLE_ONE_PIXEL_ACTIVITY, false)

        var musicInterval: Int by SharedPreference(SP_MUSIC_INTERVAL, 10)

        var requestRetryTimes: Int by SharedPreference(SP_REQUEST_RETRY_TIMES, 0)

        var requestDelayTime: Int by SharedPreference(SP_REQUEST_DELAY_TIME, 1)

        var requestTimeout: Int by SharedPreference(SP_REQUEST_TIMEOUT, 10)

        var notifyContent: String by SharedPreference(SP_NOTIFY_CONTENT, getString(R.string.notification_content))

        var extraDeviceMark: String by SharedPreference(SP_EXTRA_DEVICE_MARK, "")

        var subidSim1: Int by SharedPreference(SP_SUBID_SIM1, 0)

        var subidSim2: Int by SharedPreference(SP_SUBID_SIM2, 0)

        var extraSim1: String by SharedPreference(SP_EXTRA_SIM1, "")

        var extraSim2: String by SharedPreference(SP_EXTRA_SIM2, "")

        var enableSmsTemplate: Boolean by SharedPreference(SP_ENABLE_SMS_TEMPLATE, false)

        var smsTemplate: String by SharedPreference(SP_SMS_TEMPLATE, "")

        var enablePureClientMode: Boolean
            get() = false
            set(value) { /* Removed feature: old backups cannot re-enable it. */ }

        var enablePureTaskMode: Boolean by SharedPreference(SP_PURE_TASK_MODE, false)

        var enableDebugMode: Boolean by SharedPreference(SP_DEBUG_MODE, false)

        var enableLocation: Boolean
            get() = false
            set(value) { /* Removed feature: old backups cannot re-enable it. */ }

        var locationAccuracy: Int by SharedPreference(SP_LOCATION_ACCURACY, Criteria.ACCURACY_FINE)

        var locationPowerRequirement: Int by SharedPreference(SP_LOCATION_POWER_REQUIREMENT, Criteria.POWER_LOW)

        var locationMinInterval: Long by SharedPreference(SP_LOCATION_MIN_INTERVAL, 10000L)

        var locationMinDistance: Int by SharedPreference(SP_LOCATION_MIN_DISTANCE, 0)

        //var isFlowSystemLanguage: Boolean by SharedPreference(SP_IS_FLOW_SYSTEM_LANGUAGE, false)

        var enableBluetooth: Boolean
            get() = false
            set(value) { /* Removed feature: old backups cannot re-enable it. */ }

        var bluetoothScanInterval: Long by SharedPreference(SP_BLUETOOTH_SCAN_INTERVAL, 10000L)

        var bluetoothIgnoreAnonymous: Boolean by SharedPreference(SP_BLUETOOTH_IGNORE_ANONYMOUS, true)
    }

    init {
        throw UnsupportedOperationException("u can't instantiate me...")
    }
}
