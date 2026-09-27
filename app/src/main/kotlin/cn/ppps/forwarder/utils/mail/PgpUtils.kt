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
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openpgp.PGPPublicKeyRing
import org.bouncycastle.openpgp.PGPSecretKeyRing
import org.pgpainless.PGPainless
import org.pgpainless.algorithm.DocumentSignatureType
import org.pgpainless.algorithm.HashAlgorithm
import org.pgpainless.encryption_signing.EncryptionOptions
import org.pgpainless.encryption_signing.ProducerOptions
import org.pgpainless.encryption_signing.SigningOptions
import org.pgpainless.key.protection.SecretKeyRingProtector
import org.pgpainless.util.Passphrase
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.Security
import java.util.Date
import java.util.Properties

@Suppress("unused", "PrivatePropertyName")
class PgpUtils(
    private val properties: Properties,
    private val session: Session,
    private val from: String,
    private val fromAlias: String,
    private val nickname: String,
    private val subject: String,
    private val body: String,
    private val attachFiles: List<File> = emptyList(),
    private val toAddress: List<String> = emptyList(),
    private val ccAddress: List<String> = emptyList(),
    private val bccAddress: List<String> = emptyList(),
    private val recipientPGPPublicKeyRing: PGPPublicKeyRing? = null,
    private val senderPGPSecretKeyRing: PGPSecretKeyRing? = null,
    private val senderPGPSecretKeyPassword: String = ""
) {
    private val TAG: String = PgpUtils::class.java.simpleName

    init {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    fun sendPlainEmail(): Pair<Boolean, String> = try {
        val message = buildOriginalMessage()
        Transport.send(message)
        Pair(true, "Email sent successfully")
    } catch (e: Exception) {
        e.printStackTrace()
        Pair(false, "Failed to send email: ${e.message ?: e.toString()}")
    }

    fun sendSignedEmail(): Pair<Boolean, String> = try {
        checkPgpSignParams()
        val message = buildOriginalMessage()
        signMessage(message)
        Transport.send(message)
        Pair(true, "Signed email sent successfully")
    } catch (e: Exception) {
        e.printStackTrace()
        Pair(false, "Failed to send signed email: ${e.message ?: e.toString()}")
    }

    fun sendEncryptedEmail(): Pair<Boolean, String> = try {
        checkPgpEncParams()
        val message = buildOriginalMessage()
        encryptMessage(message)
        Transport.send(message)
        Pair(true, "Encrypted email sent successfully")
    } catch (e: Exception) {
        e.printStackTrace()
        Pair(false, "Failed to send encrypted email: ${e.message ?: e.toString()}")
    }

    fun sendSignedAndEncryptedEmail(): Pair<Boolean, String> = try {
        checkPgpSignParams()
        checkPgpEncParams()
        val message = buildOriginalMessage()
        encryptAndSignMessage(message)
        Transport.send(message)
        Pair(true, "Signed and encrypted email sent successfully")
    } catch (e: Exception) {
        e.printStackTrace()
        Pair(false, "Failed to send signed+encrypted email: ${e.message ?: e.toString()}")
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

    private fun signMessage(message: MimeMessage) {
        val protector = SecretKeyRingProtector.unlockAnyKeyWith(Passphrase.fromPassword(senderPGPSecretKeyPassword))
        val secretKeyRing = senderPGPSecretKeyRing!!
        val userId = secretKeyRing.secretKey.publicKey.userIDs.asSequence().firstOrNull()
            ?: throw IllegalArgumentException("PGP 개인 키 모음에서 사용자 ID를 찾을 수 없습니다")

        val signingOptions = SigningOptions().addInlineSignature(
            protector,
            secretKeyRing,
            userId,
            DocumentSignatureType.BINARY_DOCUMENT
        ).overrideHashAlgorithm(HashAlgorithm.SHA256)

        applyPgpToOriginalMessage(message, ProducerOptions.sign(signingOptions))
    }

    private fun encryptMessage(message: MimeMessage) {
        val encryptionOptions = EncryptionOptions.encryptCommunications()
            .addRecipient(recipientPGPPublicKeyRing!!)
        applyPgpToOriginalMessage(message, ProducerOptions.encrypt(encryptionOptions))
    }

    private fun encryptAndSignMessage(message: MimeMessage) {
        val protector = SecretKeyRingProtector.unlockAnyKeyWith(Passphrase.fromPassword(senderPGPSecretKeyPassword))
        val secretKeyRing = senderPGPSecretKeyRing!!
        val userId = secretKeyRing.secretKey.publicKey.userIDs.asSequence().firstOrNull()
            ?: throw IllegalArgumentException("PGP 개인 키 모음에서 사용자 ID를 찾을 수 없습니다")

        val signingOptions = SigningOptions().addInlineSignature(
            protector,
            secretKeyRing,
            userId,
            DocumentSignatureType.BINARY_DOCUMENT
        ).overrideHashAlgorithm(HashAlgorithm.SHA256)

        val encryptionOptions = EncryptionOptions.encryptCommunications()
            .addRecipient(recipientPGPPublicKeyRing!!)

        applyPgpToOriginalMessage(message, ProducerOptions.signAndEncrypt(encryptionOptions, signingOptions))
    }

    /**
     */
    private fun applyPgpToOriginalMessage(message: MimeMessage, options: ProducerOptions) {
        val originalBaos = ByteArrayOutputStream()
        message.writeTo(originalBaos)
        val originalInput = ByteArrayInputStream(originalBaos.toByteArray())

        val pgpBaos = ByteArrayOutputStream()
        val pgpStream = PGPainless.encryptAndOrSign().onOutputStream(pgpBaos).withOptions(options)
        originalInput.copyTo(pgpStream)
        pgpStream.close()

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

    private fun checkPgpSignParams() {
        if (senderPGPSecretKeyRing == null) {
            throw IllegalArgumentException("보내는 사람의 PGP 개인 키 모음(senderPGPSecretKeyRing)을 설정해야 합니다")
        }
        if (senderPGPSecretKeyPassword.isBlank()) {
            throw IllegalArgumentException("보내는 사람의 PGP 개인 키 비밀번호(senderPGPSecretKeyPassword)를 입력해야 합니다")
        }
    }

    private fun checkPgpEncParams() {
        if (recipientPGPPublicKeyRing == null) {
            throw IllegalArgumentException("받는 사람의 PGP 공개 키 모음(recipientPGPPublicKeyRing)을 설정해야 합니다")
        }
    }
}
