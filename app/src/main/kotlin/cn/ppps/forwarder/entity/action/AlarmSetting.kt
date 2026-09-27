package cn.ppps.forwarder.entity.action

import java.io.Serializable

data class AlarmSetting(
    var description: String = "",
    var action: String = "stop",
    var volume: Int = 80,
    var playTimes: Int = 1,
    var music: String = "",
    var repeatTimes: Int = 5,
    var vibrate: String = "---___===___",
    var flashTimes: Int = 5,
    var flash: String = "XXOOXXOO",
) : Serializable
