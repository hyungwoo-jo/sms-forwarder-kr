package cn.ppps.forwarder.utils

import java.io.ByteArrayOutputStream
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

/**
 */
object RSACrypt {

    private const val TRANSFORMATION = "RSA"
    private const val ENCRYPT_MAX_SIZE = 245
    private const val DECRYPT_MAX_SIZE = 256

    /**
     */
    fun encryptByPrivateKey(input: String, privateKey: PrivateKey): String {

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, privateKey)

        val byteArray = input.toByteArray()

        var temp: ByteArray?
        var offset = 0

        val outputStream = ByteArrayOutputStream()

        while (byteArray.size - offset > 0) {
            if (byteArray.size - offset >= ENCRYPT_MAX_SIZE) {
                temp = cipher.doFinal(byteArray, offset, ENCRYPT_MAX_SIZE)
                offset += ENCRYPT_MAX_SIZE
            } else {
                temp = cipher.doFinal(byteArray, offset, byteArray.size - offset)
                offset = byteArray.size
            }
            outputStream.write(temp)
        }
        outputStream.close()

        return Base64.encode(outputStream.toByteArray())

    }

    /**
     */
    fun encryptByPublicKey(input: String, publicKey: PublicKey): String {

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        val byteArray = input.toByteArray()

        var temp: ByteArray?
        var offset = 0

        val outputStream = ByteArrayOutputStream()

        while (byteArray.size - offset > 0) {
            if (byteArray.size - offset >= ENCRYPT_MAX_SIZE) {
                temp = cipher.doFinal(byteArray, offset, ENCRYPT_MAX_SIZE)
                offset += ENCRYPT_MAX_SIZE
            } else {
                temp = cipher.doFinal(byteArray, offset, byteArray.size - offset)
                offset = byteArray.size
            }
            outputStream.write(temp)
        }
        outputStream.close()

        return Base64.encode(outputStream.toByteArray())

    }

    /**
     */
    fun decryptByPrivateKey(input: String, privateKey: PrivateKey): String {

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)

        val byteArray = Base64.decode(input)

        var temp: ByteArray?
        var offset = 0

        val outputStream = ByteArrayOutputStream()

        while (byteArray.size - offset > 0) {
            if (byteArray.size - offset >= DECRYPT_MAX_SIZE) {

                temp = cipher.doFinal(byteArray, offset, DECRYPT_MAX_SIZE)
                offset += DECRYPT_MAX_SIZE
            } else {
                temp = cipher.doFinal(byteArray, offset, byteArray.size - offset)
                offset = byteArray.size
            }
            outputStream.write(temp)
        }
        outputStream.close()

        return String(outputStream.toByteArray())

    }

    /**
     */
    fun decryptByPublicKey(input: String, publicKey: PublicKey): String {

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, publicKey)

        val byteArray = Base64.decode(input)

        var temp: ByteArray?
        var offset = 0

        val outputStream = ByteArrayOutputStream()

        while (byteArray.size - offset > 0) {
            if (byteArray.size - offset >= DECRYPT_MAX_SIZE) {

                temp = cipher.doFinal(byteArray, offset, DECRYPT_MAX_SIZE)
                offset += DECRYPT_MAX_SIZE
            } else {
                temp = cipher.doFinal(byteArray, offset, byteArray.size - offset)
                offset = byteArray.size
            }
            outputStream.write(temp)
        }
        outputStream.close()

        return String(outputStream.toByteArray())

    }

    fun getPrivateKey(privateKeyStr: String): PrivateKey {
        val generator = KeyFactory.getInstance("RSA")
        return generator.generatePrivate(PKCS8EncodedKeySpec(Base64.decode(privateKeyStr)))
    }

    fun getPublicKey(publicKeyStr: String): PublicKey {
        val kf = KeyFactory.getInstance("RSA")
        return kf.generatePublic(X509EncodedKeySpec(Base64.decode(publicKeyStr)))
    }

}