package cn.ppps.forwarder.utils.mail

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import cn.ppps.forwarder.utils.Log
import org.openintents.openpgp.OpenPgpError
import org.openintents.openpgp.util.OpenPgpApi
import org.openintents.openpgp.util.OpenPgpServiceConnection
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * https://github.com/open-keychain/open-keychain
 *
 *
 */
class OpenKeychainHelper(private val context: Context) {

    companion object {
        private val TAG: String = OpenKeychainHelper::class.java.simpleName

        const val PROVIDER_PACKAGE = "org.sufficientlysecure.keychain"
        private const val BIND_TIMEOUT_SECONDS = 10L

        fun isProviderInstalled(context: Context): Boolean {
            return try {
                context.packageManager.getPackageInfo(PROVIDER_PACKAGE, PackageManager.GET_SERVICES)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    private var serviceConnection: OpenPgpServiceConnection? = null

    /**
     */
    @Synchronized
    private fun bindBlocking(): OpenPgpApi {
        if (!isProviderInstalled(context)) {
            throw Exception("OpenKeychain이 설치되지 않았습니다: $PROVIDER_PACKAGE")
        }
        serviceConnection?.let { conn ->
            if (conn.isBound) return OpenPgpApi(context, conn.service)
        }

        val latch = CountDownLatch(1)
        var bindError: Exception? = null
        val connection = OpenPgpServiceConnection(context.applicationContext, PROVIDER_PACKAGE, object : OpenPgpServiceConnection.OnBound {
            override fun onBound(service: org.openintents.openpgp.IOpenPgpService2) {
                latch.countDown()
            }

            override fun onError(e: Exception) {
                bindError = e
                latch.countDown()
            }
        })
        connection.bindToService()
        if (!latch.await(BIND_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            connection.unbindFromService()
            throw Exception("OpenKeychain 서비스 연결 시간이 초과되었습니다")
        }
        bindError?.let { throw Exception("OpenKeychain 서비스 연결에 실패했습니다: ${it.message}", it) }

        serviceConnection = connection
        return OpenPgpApi(context, connection.service)
    }

    /**
     *
     */
    fun executeApi(data: Intent, input: InputStream?, output: OutputStream?): Intent {
        val api = bindBlocking()
        return api.executeApi(data, input, output)
    }

    /**
     *
     */
    fun encrypt(recipientEmails: Array<String>, signKeyId: Long, input: InputStream, output: OutputStream) {
        val data = Intent().apply {
            action = if (signKeyId != 0L) OpenPgpApi.ACTION_SIGN_AND_ENCRYPT else OpenPgpApi.ACTION_ENCRYPT
            putExtra(OpenPgpApi.EXTRA_USER_IDS, recipientEmails)
            if (signKeyId != 0L) putExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, signKeyId)
            putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true)
        }
        val result = executeApi(data, input, output)
        when (result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR)) {
            OpenPgpApi.RESULT_CODE_SUCCESS -> Log.d(TAG, "OpenKeychain 암호화 성공, 받는 사람=${recipientEmails.joinToString()}")

            OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED -> {
                throw Exception("OpenKeychain에서 키 선택 또는 권한 승인이 필요합니다. 이메일 전송 채널 설정을 다시 저장하여 승인해 주세요.")
            }

            else -> {
                val error: OpenPgpError? = result.getParcelableExtra(OpenPgpApi.RESULT_ERROR)
                throw Exception("OpenKeychain 암호화에 실패했습니다: ${error?.message ?: "알 수 없는 오류"}")
            }
        }
    }

    fun unbind() {
        serviceConnection?.unbindFromService()
        serviceConnection = null
    }
}
