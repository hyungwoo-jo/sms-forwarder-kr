package cn.ppps.forwarder.entity.setting

import java.io.Serializable

data class BarkSetting(
    var server: String = "",
    val group: String = "",
    val icon: String = "",
    val sound: String = "",
    val badge: String = "",
    val url: String = "",
    val level: String = "active",
    val title: String = "",
    val transformation: String = "none",
    val key: String = "",
    var iv: String = "",
    val call: String = "",
    val autoCopy: String = "",
) : Serializable
