package cn.ppps.forwarder.utils.mail

import android.text.Html
import android.text.Spanned
import cn.ppps.forwarder.utils.Log
import com.sun.mail.util.MailSSLSocketFactory
import jakarta.mail.Authenticator
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import org.bouncycastle.openpgp.PGPPublicKeyRing
import org.bouncycastle.openpgp.PGPSecretKeyRing
import java.io.File
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.Properties


@Suppress("PrivatePropertyName", "DEPRECATION")
class EmailSender(
    private val host: String,
    private val port: String,
    private val from: String,
    private val password: String,
    private val fromAlias: String,
    private val nickname: String,
    private val subject: String,
    private val body: CharSequence,
    private val attachFiles: MutableList<File> = mutableListOf(),
    private val toAddress: MutableList<String> = mutableListOf(),
    private val ccAddress: MutableList<String> = mutableListOf(),
    private val bccAddress: MutableList<String> = mutableListOf(),
    private val listener: EmailTaskListener? = null,
    private val openSSL: Boolean = false,
    private val startTls: Boolean = false,
    private val encryptionProtocol: String = "S/MIME",
    private val recipientX509Cert: X509Certificate? = null,
    private val senderPrivateKey: PrivateKey? = null,
    private val senderX509Cert: X509Certificate? = null,
    private var recipientPGPPublicKeyRing: PGPPublicKeyRing? = null,
    private var senderPGPSecretKeyRing: PGPSecretKeyRing? = null,
    private val senderPGPSecretKeyPassword: String = "",
    private val openKeychainHelper: OpenKeychainHelper? = null,
    private val openKeychainSignKeyId: Long = 0L,
) {

    private val TAG: String = EmailSender::class.java.simpleName

    private val properties: Properties = Properties().apply {
        put("mail.smtp.host", host)
        put("mail.smtp.port", port)
        put("mail.smtp.auth", "true")
        if (openSSL) {
            put("mail.smtp.ssl.enable", "true")
            val sf = MailSSLSocketFactory("TLSv1.2")
            sf.setTrustedHosts("*")
            put("mail.smtp.ssl.socketFactory", sf)
            put("mail.smtp.ssl.protocols", "TLSv1.2")
        }
        if (startTls) {
            put("mail.smtp.starttls.enable", "true")
        }
    }

    suspend fun sendEmail() {
        try {
            val authenticator = MailAuthenticator(from, password)
            val html = try {
                if (body is Spanned) Html.toHtml(body) else body.toString()
            } catch (e: Exception) {
                body.toString()
            }

            when (encryptionProtocol) {
                "S/MIME" -> {
                    val smimeUtils = SmimeUtils(
                        properties,
                        authenticator,
                        from,
                        fromAlias,
                        nickname,
                        subject,
                        html,
                        attachFiles,
                        toAddress,
                        ccAddress,
                        bccAddress,
                        recipientX509Cert,
                        senderPrivateKey,
                        senderX509Cert,
                    )
                    val isEncrypt: Boolean = recipientX509Cert != null
                    val isSign: Boolean = senderX509Cert != null && senderPrivateKey != null
                    Log.d(TAG, "isEncrypt=$isEncrypt, isSign=$isSign")
                    val result = when {
                        isEncrypt && isSign -> smimeUtils.sendSignedAndEncryptedEmail()
                        isEncrypt -> smimeUtils.sendEncryptedEmail()
                        isSign -> smimeUtils.sendSignedEmail()
                        else -> smimeUtils.sendPlainEmail()
                    }
                    listener?.onEmailSent(result.first, result.second)
                }

                "OpenPGP" -> {
                    val pgpEmail = PgpUtils(
                        properties = properties,
                        session = Session.getInstance(properties, authenticator),
                        from = from,
                        fromAlias = fromAlias,
                        nickname = nickname,
                        subject = subject,
                        body = html,
                        attachFiles = attachFiles,
                        toAddress = toAddress,
                        ccAddress = ccAddress,
                        bccAddress = bccAddress,
                        recipientPGPPublicKeyRing = recipientPGPPublicKeyRing,
                        senderPGPSecretKeyRing = senderPGPSecretKeyRing,
                        senderPGPSecretKeyPassword = senderPGPSecretKeyPassword
                    )

                    val isEncrypt = recipientPGPPublicKeyRing != null
                    val isSign = senderPGPSecretKeyRing != null
                    val result = when {
                        isEncrypt && isSign -> pgpEmail.sendSignedAndEncryptedEmail()
                        isEncrypt -> pgpEmail.sendEncryptedEmail()
                        isSign -> pgpEmail.sendSignedEmail()
                        else -> pgpEmail.sendPlainEmail()
                    }
                    listener?.onEmailSent(result.first, result.second)
                }

                "OpenKeychain" -> {
                    if (openKeychainHelper == null) {
                        listener?.onEmailSent(false, "OpenKeychainHelper is null")
                        return
                    }
                    val openKeychainEmail = OpenKeychainMailUtils(
                        session = Session.getInstance(properties, authenticator),
                        fromAlias = fromAlias,
                        nickname = nickname,
                        subject = subject,
                        body = html,
                        attachFiles = attachFiles,
                        toAddress = toAddress,
                        ccAddress = ccAddress,
                        bccAddress = bccAddress,
                        openKeychainHelper = openKeychainHelper,
                        signKeyId = openKeychainSignKeyId,
                    )
                    val result = openKeychainEmail.sendEncryptedEmail()
                    listener?.onEmailSent(result.first, result.second)
                }

                else -> {
                    val simpleEmail = SmimeUtils(
                        properties,
                        authenticator,
                        from,
                        fromAlias,
                        nickname,
                        subject,
                        html,
                        attachFiles,
                        toAddress,
                        ccAddress,
                        bccAddress,
                    )
                    val result = simpleEmail.sendPlainEmail()
                    listener?.onEmailSent(result.first, result.second)
                }
            }
        } catch (e: Exception) {
            listener?.onEmailSent(false, "Error sending email: ${e.message}")
        }
    }

    interface EmailTaskListener {
        fun onEmailSent(success: Boolean, message: String)
    }

    /**
     */
    private class MailAuthenticator(username: String, private var password: String) : Authenticator() {
        private var userName: String? = username
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(userName, password)
        }
    }
}
