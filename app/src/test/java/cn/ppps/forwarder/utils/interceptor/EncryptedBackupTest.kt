package cn.ppps.forwarder.utils.interceptor

import cn.ppps.forwarder.utils.EncryptedBackup
import org.junit.Assert.*
import org.junit.Test

class EncryptedBackupTest {
    @Test fun roundTripAndRandomizedCiphertext() {
        val data = "WORKS 규칙과 token-test".toByteArray(Charsets.UTF_8)
        val password = "test-password".toCharArray()
        val first = EncryptedBackup.encrypt(data, password)
        assertArrayEquals(data, EncryptedBackup.decrypt(first, password))
        assertFalse(first.contentEquals(EncryptedBackup.encrypt(data, password)))
        assertFalse(String(first, Charsets.ISO_8859_1).contains("token-test"))
    }
    @Test fun wrongPasswordAndTamperingCannotRestore() {
        val bytes = EncryptedBackup.encrypt("data".toByteArray(), "test-password".toCharArray())
        for ((file, pass) in listOf(bytes to "wrong-password", bytes.copyOf().apply { this[lastIndex] = (this[lastIndex].toInt() xor 1).toByte() } to "test-password")) {
            try { EncryptedBackup.decrypt(file, pass.toCharArray()); fail("Invalid backup accepted") }
            catch (expected: java.security.GeneralSecurityException) { }
        }
    }
}
