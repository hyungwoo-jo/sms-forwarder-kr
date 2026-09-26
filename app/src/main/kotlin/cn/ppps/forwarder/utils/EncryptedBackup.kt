package cn.ppps.forwarder.utils

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/** Versioned authenticated encryption; no passwords are persisted. */
object EncryptedBackup {
    const val MAX_BYTES = 4 * 1024 * 1024
    private val magic = "SMSKR001".toByteArray(Charsets.US_ASCII)
    private fun key(password: CharArray, salt: ByteArray): SecretKeySpec {
        require(password.size >= 8) { "비밀번호를 8자 이상 입력하세요." }
        val spec = PBEKeySpec(password, salt, 100000, 256)
        return try { SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded, "AES") }
        finally { spec.clearPassword() }
    }
    fun encrypt(plain: ByteArray, password: CharArray): ByteArray {
        require(plain.size <= MAX_BYTES - 52) { "백업이 너무 큽니다." }
        val random = SecureRandom()
        val salt = ByteArray(16).also { random.nextBytes(it) }
        val iv = ByteArray(12).also { random.nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key(password, salt), GCMParameterSpec(128, iv))
        cipher.updateAAD(magic)
        return magic + salt + iv + cipher.doFinal(plain)
    }
    fun decrypt(bytes: ByteArray, password: CharArray): ByteArray {
        require(bytes.size in 52..MAX_BYTES && bytes.copyOfRange(0, 8).contentEquals(magic)) { "지원하는 암호화 백업 파일이 아닙니다." }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(password, bytes.copyOfRange(8, 24)), GCMParameterSpec(128, bytes.copyOfRange(24, 36)))
        cipher.updateAAD(magic)
        return cipher.doFinal(bytes, 36, bytes.size - 36)
    }
}
