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
 * OpenKeychain（OpenPGP API）封装
 * https://github.com/open-keychain/open-keychain
 *
 * 通过 org.openintents.openpgp.IOpenPgpService2 与 OpenKeychain 通信，
 * 密钥全部由 OpenKeychain 管理，本应用不接触任何密钥材料。
 *
 * ⚠️注意：executeApi 可能返回 RESULT_CODE_USER_INTERACTION_REQUIRED（需要用户授权/选择密钥），
 * 该交互只能在前台界面完成（EmailFragment 保存/测试时预检），后台转发时若遇到则直接失败并记录日志。
 */
class OpenKeychainHelper(private val context: Context) {

    companion object {
        private val TAG: String = OpenKeychainHelper::class.java.simpleName

        const val PROVIDER_PACKAGE = "org.sufficientlysecure.keychain"
        private const val BIND_TIMEOUT_SECONDS = 10L

        //检查是否安装了OpenKeychain
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
     * 阻塞式绑定服务（必须在非主线程调用）
     * @throws Exception 未安装OpenKeychain或绑定超时/出错
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
     * 执行 OpenPGP API 操作（阻塞，必须在非主线程调用）
     *
     * @return 结果Intent，RESULT_CODE 可能为 SUCCESS / USER_INTERACTION_REQUIRED / ERROR，由调用方分派
     */
    fun executeApi(data: Intent, input: InputStream?, output: OutputStream?): Intent {
        val api = bindBlocking()
        return api.executeApi(data, input, output)
    }

    /**
     * 加密（可选签名）——供后台发送邮件调用（阻塞，必须在非主线程调用）
     *
     * @param recipientEmails 收件人邮箱（OpenKeychain按User ID自动匹配公钥）
     * @param signKeyId       签名密钥ID，0表示只加密不签名
     * @throws Exception 需要用户交互（后台无法处理）或加密出错
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
                //后台无法弹出授权界面：提示用户去发送通道编辑页重新保存以完成授权
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
