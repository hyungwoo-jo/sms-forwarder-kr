package cn.ppps.forwarder.entity.setting

import cn.ppps.forwarder.R
import java.io.Serializable

data class EmailSetting(
    var mailType: String = "",
    var fromEmail: String = "",
    var pwd: String = "",
    var nickname: String = "",
    var host: String = "",
    var port: String = "",
    var ssl: Boolean = false,
    var startTls: Boolean = false,
    var title: String = "",
    var recipients: MutableMap<String, Pair<String, String>> = mutableMapOf(),
    var toEmail: String = "",
    var keystore: String = "",
    var password: String = "",
    var encryptionProtocol: String = "Plain",
    var fromEmailAlias: String = "",
    var openKeychainSignKeyId: Long = 0L,
    var openKeychainSignKeyDesc: String = "",
) : Serializable {

    fun getEncryptionProtocolCheckId(): Int {
        return when (encryptionProtocol) {
            "S/MIME" -> R.id.rb_encryption_protocol_smime
            "OpenPGP" -> R.id.rb_encryption_protocol_openpgp
            "OpenKeychain" -> R.id.rb_encryption_protocol_openkeychain
            else -> R.id.rb_encryption_protocol_plain
        }
    }
}
