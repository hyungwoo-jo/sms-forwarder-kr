package cn.ppps.forwarder.entity.action

import cn.ppps.forwarder.utils.SettingUtils
import java.io.Serializable

data class SettingsSetting(
    var description: String = "",
    var enableSms: Boolean = SettingUtils.enableSms,

    var enablePhone: Boolean = SettingUtils.enablePhone,
    var enableCallType1: Boolean = SettingUtils.enableCallType1,
    var enableCallType2: Boolean = SettingUtils.enableCallType2,
    var enableCallType3: Boolean = SettingUtils.enableCallType3,
    var enableCallType4: Boolean = SettingUtils.enableCallType4,
    var enableCallType5: Boolean = SettingUtils.enableCallType5,
    var enableCallType6: Boolean = SettingUtils.enableCallType6,

    var enableAppNotify: Boolean = SettingUtils.enableAppNotify,
    var enableCancelAppNotify: Boolean = SettingUtils.enableCancelAppNotify,
    var enableNotUserPresent: Boolean = SettingUtils.enableNotUserPresent,

    var enableLocation: Boolean = SettingUtils.enableLocation,
    var locationAccuracy: Int = SettingUtils.locationAccuracy,
    var locationPowerRequirement: Int = SettingUtils.locationPowerRequirement,
    var locationMinInterval: Long = SettingUtils.locationMinInterval,
    var locationMinDistance: Int = SettingUtils.locationMinDistance,

    var enableSmsCommand: Boolean = SettingUtils.enableSmsCommand,
    var smsCommandSafePhone: String = SettingUtils.smsCommandSafePhone,

    var enableLoadAppList: Boolean = SettingUtils.enableLoadAppList,
    var enableLoadUserAppList: Boolean = SettingUtils.enableLoadUserAppList,
    var enableLoadSystemAppList: Boolean = SettingUtils.enableLoadSystemAppList,

    var cancelExtraAppNotify: String = SettingUtils.cancelExtraAppNotify,

    var duplicateMessagesLimits: Int = SettingUtils.duplicateMessagesLimits,
) : Serializable
