package cn.ppps.forwarder.entity.setting

import cn.ppps.forwarder.R
import java.io.Serializable

data class SocketSetting(
    val method: String = "MQTT",
    var address: String = "",
    val port: Int = 0,
    val msgTemplate: String = "",
    val secret: String = "",
    val response: String = "",
    val username: String = "",
    val password: String = "",
    val inCharset: String = "",
    val outCharset: String = "",
    val inMessageTopic: String = "",
    val outMessageTopic: String = "",
    val uriType: String = "tcp",
    val path: String = "",
    val clientId: String = "",
    val qos: Int = 0,
    val retained: Boolean = false,
) : Serializable {

    fun getMethodCheckId(): Int {
        return when (method) {
            "MQTT" -> R.id.rb_method_mqtt
            "TCP" -> R.id.rb_method_tcp
            "UDP" -> R.id.rb_method_udp
            else -> R.id.rb_method_mqtt
        }
    }

    fun getUriTypeCheckId(): Int {
        return when (uriType) {
            "ssl" -> R.id.rb_uriType_ssl
            else -> R.id.rb_uriType_tcp
        }
    }

    fun getQosCheckId(): Int {
        return when (qos) {
            1 -> R.id.rb_qos_1
            2 -> R.id.rb_qos_2
            else -> R.id.rb_qos_0
        }
    }

}