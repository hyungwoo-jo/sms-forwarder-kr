package cn.ppps.forwarder.utils.mail

import cn.ppps.forwarder.utils.Log
import jakarta.activation.DataHandler
import jakarta.activation.FileDataSource
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import jakarta.mail.internet.MimeUtility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bouncycastle.cert.jcajce.JcaCertStore
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import org.bouncycastle.cms.CMSAlgorithm
import org.bouncycastle.cms.CMSEnvelopedDataGenerator
import org.bouncycastle.cms.CMSProcessableByteArray
import org.bouncycastle.cms.CMSSignedDataGenerator
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.OutputEncryptor
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.PrivateKey
import java.security.Security
import java.security.cert.X509Certificate
import java.util.Date
import java.util.Properties


@Suppress("PrivatePropertyName")
class SmimeUtils(
    private val properties: Properties,
    private val authenticator: Authenticator,
    private val from: String,
    private val fromAlias: String,
    private val nickname: String,
    private val subject: String,
    private val body: String,
    private val attachFiles: MutableList<File> = mutableListOf(),
    private val toAddress: MutableList<String> = mutableListOf(),
    private val ccAddress: MutableList<String> = mutableListOf(),
    private val bccAddress: MutableList<String> = mutableListOf(),
    private val recipientX509Cert: X509Certificate? = null,
    private val senderPrivateKey: PrivateKey? = null,
    private val senderX509Cert: X509Certificate? = null,
) {

    private val TAG: String = SmimeUtils::class.java.simpleName

    init {
        Security.addProvider(BouncyCastleProvider())
    }

    suspend fun sendPlainEmail(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        Log.d(TAG, "sendPlainEmail")
        try {
            val originalMessage = getOriginalMessage()
            Transport.send(originalMessage)
            Pair(true, "Email sent successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "Failed to send email: ${e.message}")
        }
    }

    suspend fun sendSignedEmail(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        Log.d(TAG, "sendSignedEmail")
        try {
            val originalMessage = getOriginalMessage()
            val signedMessage = getSignedMessage(originalMessage)
            Transport.send(signedMessage)
            Pair(true, "Email signed and sent successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "Failed to sign and send email: ${e.message}")
        }
    }

    suspend fun sendEncryptedEmail(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        Log.d(TAG, "sendEncryptedEmail")
        try {
            val originalMessage = getOriginalMessage()
            val encryptedMessage = getEncryptedMessage(originalMessage)
            Transport.send(encryptedMessage)
            Pair(true, "Encrypted email sent successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "Failed to send encrypted email: ${e.message}")
        }
    }

    suspend fun sendSignedAndEncryptedEmail(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        Log.d(TAG, "sendSignedAndEncryptedEmail")
        try {
            val originalMessage = getOriginalMessage()
            val signedMessage = getSignedMessage(originalMessage)
            val encryptedMessage = getEncryptedMessage(signedMessage)
            Transport.send(encryptedMessage)
            Pair(true, "Signed and encrypted email sent successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "Failed to send signed and encrypted email: ${e.message}")
        }
    }

    private fun getOriginalMessage(): MimeMessage {
        val session = Session.getInstance(properties, authenticator)
        session.debug = true
        val message = MimeMessage(session)
        val toAddress = toAddress.map { InternetAddress(it) }.toTypedArray()
        message.setRecipients(Message.RecipientType.TO, toAddress)
        val ccAddress = ccAddress.map { InternetAddress(it) }.toTypedArray()
        message.setRecipients(Message.RecipientType.CC, ccAddress)
        val bccAddress = bccAddress.map { InternetAddress(it) }.toTypedArray()
        message.setRecipients(Message.RecipientType.BCC, bccAddress)
        when {
            nickname.isEmpty() -> message.setFrom(InternetAddress(fromAlias))
            else -> try {
                var name = nickname.replace(":", "-").replace("\n", "-")
                name = MimeUtility.encodeText(name)
                message.setFrom(InternetAddress("$name <$fromAlias>"))
            } catch (e: Exception) {
                e.printStackTrace()
                message.setFrom(InternetAddress(fromAlias))
            }
        }
        try {
            message.subject = MimeUtility.encodeText(subject.replace(":", "-").replace("\n", "-"))
        } catch (e: Exception) {
            e.printStackTrace()
            message.subject = subject
        }

        val contentPart = MimeMultipart("mixed")

        val textBodyPart = MimeBodyPart()
        textBodyPart.setContent(body, "text/html;charset=UTF-8")
        contentPart.addBodyPart(textBodyPart)

        attachFiles.forEach {
            val fileBodyPart = MimeBodyPart()
            val ds = FileDataSource(it)
            val dh = DataHandler(ds)
            fileBodyPart.dataHandler = dh
            fileBodyPart.fileName = MimeUtility.encodeText(dh.name)
            contentPart.addBodyPart(fileBodyPart)
        }

        message.setContent(contentPart)
        message.sentDate = Date()
        message.saveChanges()
        return message
    }

    private fun getSignedMessage(originalMessage: MimeMessage): MimeMessage {
        val contentSigner = JcaContentSignerBuilder("SHA256withRSA").build(senderPrivateKey)
        val certificateHolder = JcaX509CertificateHolder(senderX509Cert)
        val signerInfoGenerator = JcaSignerInfoGeneratorBuilder(
            JcaDigestCalculatorProviderBuilder().setProvider(BouncyCastleProvider()).build()
        ).build(contentSigner, certificateHolder)

        val generator = CMSSignedDataGenerator()
        generator.addSignerInfoGenerator(signerInfoGenerator)
        val certStore = JcaCertStore(listOf(senderX509Cert))
        generator.addCertificates(certStore)

        val outputStream = ByteArrayOutputStream()
        originalMessage.writeTo(outputStream)
        val contentData = CMSProcessableByteArray(outputStream.toByteArray())
        val signedData = generator.generate(contentData, true)

        val signedMessage = MimeMessage(originalMessage.session, ByteArrayInputStream(signedData.encoded))
        /*
        signedMessage.setRecipients(Message.RecipientType.TO, originalMessage.getRecipients(Message.RecipientType.TO))
        signedMessage.setRecipients(Message.RecipientType.CC, originalMessage.getRecipients(Message.RecipientType.CC))
        signedMessage.setRecipients(Message.RecipientType.BCC, originalMessage.getRecipients(Message.RecipientType.BCC))
        signedMessage.addFrom(originalMessage.from)
        signedMessage.subject = originalMessage.subject
        signedMessage.sentDate = originalMessage.sentDate
        */
        signedMessage.setContent(signedData.encoded, "application/pkcs7-mime; name=smime.p7m; smime-type=signed-data")
        signedMessage.saveChanges()

        return signedMessage
    }

    private fun getEncryptedMessage(originalMessage: MimeMessage): MimeMessage {
        val cmsEnvelopedDataGenerator = CMSEnvelopedDataGenerator()
        val recipientInfoGenerator = JceKeyTransRecipientInfoGenerator(recipientX509Cert)
        cmsEnvelopedDataGenerator.addRecipientInfoGenerator(recipientInfoGenerator)

        val outputEncryptor: OutputEncryptor = JceCMSContentEncryptorBuilder(CMSAlgorithm.DES_EDE3_CBC).build()
        val originalContent = ByteArrayOutputStream()
        originalMessage.writeTo(originalContent)
        val inputStream = originalContent.toByteArray()
        val cmsEnvelopedData = cmsEnvelopedDataGenerator.generate(
            CMSProcessableByteArray(inputStream),
            outputEncryptor
        )

        val encryptedMessage = MimeMessage(originalMessage.session)
        encryptedMessage.setRecipients(Message.RecipientType.TO, originalMessage.getRecipients(Message.RecipientType.TO))
        encryptedMessage.setRecipients(Message.RecipientType.CC, originalMessage.getRecipients(Message.RecipientType.CC))
        encryptedMessage.setRecipients(Message.RecipientType.BCC, originalMessage.getRecipients(Message.RecipientType.BCC))
        encryptedMessage.addFrom(originalMessage.from)
        encryptedMessage.subject = originalMessage.subject
        encryptedMessage.sentDate = originalMessage.sentDate
        encryptedMessage.setContent(cmsEnvelopedData.encoded, "application/pkcs7-mime; name=smime.p7m; smime-type=enveloped-data")
        encryptedMessage.setHeader("Content-Type", "application/pkcs7-mime; name=smime.p7m; smime-type=enveloped-data")
        encryptedMessage.setHeader("Content-Disposition", "attachment; filename=smime.p7m")
        encryptedMessage.setHeader("Content-Description", "S/MIME Encrypted Message")
        encryptedMessage.addHeader("Content-Transfer-Encoding", "base64")
        encryptedMessage.saveChanges()

        return encryptedMessage
    }

}
