package cn.ppps.forwarder.utils.mail

import jakarta.activation.DataHandler
import jakarta.activation.FileDataSource
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import jakarta.mail.internet.MimeUtility
import jakarta.mail.util.ByteArrayDataSource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Date

/**
 *
 */
@Suppress("PrivatePropertyName")
class OpenKeychainMailUtils(
    private val session: Session,
    private val fromAlias: String,
    private val nickname: String,
    private val subject: String,
    private val body: String,
    private val attachFiles: List<File> = emptyList(),
    private val toAddress: List<String> = emptyList(),
    private val ccAddress: List<String> = emptyList(),
    private val bccAddress: List<String> = emptyList(),
    // OpenKeychain
    private val openKeychainHelper: OpenKeychainHelper,
    private val signKeyId: Long = 0L,
) {
    private val TAG: String = OpenKeychainMailUtils::class.java.simpleName

    fun sendEncryptedEmail(): Pair<Boolean, String> = try {
        if (toAddress.isEmpty()) throw IllegalArgumentException("받는 사람 이메일 주소(toAddress)를 입력해야 합니다")
        val message = buildOriginalMessage()
        applyOpenKeychainToOriginalMessage(message)
        Transport.send(message)
        val signed = if (signKeyId != 0L) "signed and " else ""
        Pair(true, "OpenKeychain ${signed}encrypted email sent successfully")
    } catch (e: Exception) {
        e.printStackTrace()
        Pair(false, "Failed to send OpenKeychain encrypted email: ${e.message ?: e.toString()}")
    }

    private fun buildOriginalMessage(): MimeMessage {
        val message = MimeMessage(session)
        message.setFrom(InternetAddress(fromAlias, nickname, "UTF-8"))
        if (toAddress.isNotEmpty()) {
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress.joinToString(",")))
        }
        if (ccAddress.isNotEmpty()) {
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccAddress.joinToString(",")))
        }
        if (bccAddress.isNotEmpty()) {
            message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bccAddress.joinToString(",")))
        }
        message.subject = MimeUtility.encodeText(subject, "UTF-8", null)
        message.sentDate = Date()

        val multipart = MimeMultipart("mixed")

        val bodyPart = MimeBodyPart()
        bodyPart.setContent(body, "text/html;charset=UTF-8")
        multipart.addBodyPart(bodyPart)

        attachFiles.forEach { file ->
            val filePart = MimeBodyPart()
            val fileDs = FileDataSource(file)
            filePart.dataHandler = DataHandler(fileDs)
            filePart.fileName = MimeUtility.encodeText(file.name, "UTF-8", "B")
            multipart.addBodyPart(filePart)
        }

        message.setContent(multipart)
        message.saveChanges()
        return message
    }

    /**
     */
    private fun applyOpenKeychainToOriginalMessage(message: MimeMessage) {
        val originalBaos = ByteArrayOutputStream()
        message.writeTo(originalBaos)
        val originalInput = ByteArrayInputStream(originalBaos.toByteArray())

        val pgpBaos = ByteArrayOutputStream()
        openKeychainHelper.encrypt(toAddress.toTypedArray(), signKeyId, originalInput, pgpBaos)

        val pgpMultipart = MimeMultipart("encrypted; protocol=\"application/pgp-encrypted\"")

        val versionPart = MimeBodyPart().apply {
            setText("Version: 1")
            setHeader("Content-Type", "application/pgp-encrypted")
            setHeader("Content-Description", "PGP/MIME version identification")
        }

        val contentPart = MimeBodyPart().apply {
            val dataSource = ByteArrayDataSource(pgpBaos.toByteArray(), "application/octet-stream")
            dataHandler = DataHandler(dataSource)
            setHeader("Content-Type", "application/octet-stream; name=\"encrypted.asc\"")
            setHeader("Content-Description", "OpenPGP encrypted/signed message")
            setHeader("Content-Disposition", "inline; filename=\"encrypted.asc\"")
        }

        pgpMultipart.addBodyPart(versionPart)
        pgpMultipart.addBodyPart(contentPart)

        message.setContent(pgpMultipart)
        message.saveChanges()
    }
}
