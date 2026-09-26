package cn.ppps.forwarder.utils.sender

import android.text.TextUtils
import cn.ppps.forwarder.database.entity.Rule
import cn.ppps.forwarder.entity.MsgInfo
import cn.ppps.forwarder.entity.result.TelegramResult
import cn.ppps.forwarder.entity.setting.TelegramSetting
import cn.ppps.forwarder.utils.Log
import cn.ppps.forwarder.utils.SendUtils
import cn.ppps.forwarder.utils.SettingUtils
import cn.ppps.forwarder.utils.interceptor.LoggingInterceptor
import cn.ppps.forwarder.utils.interceptor.SensitiveLogRedactor
import com.google.gson.Gson
import com.xuexiang.xhttp2.XHttp
import com.xuexiang.xhttp2.callback.SimpleCallBack
import com.xuexiang.xhttp2.exception.ApiException
import okhttp3.Credentials
import okhttp3.Response
import okhttp3.Route
import java.net.Authenticator
import java.net.InetSocketAddress
import java.net.PasswordAuthentication
import java.net.Proxy

class TelegramUtils private constructor() {
    companion object {

        private val TAG: String = TelegramUtils::class.java.simpleName

        fun sendMsg(
            setting: TelegramSetting,
            msgInfo: MsgInfo,
            rule: Rule? = null,
            senderIndex: Int = 0,
            logId: Long = 0L,
            msgId: Long = 0L
        ) {
            val formattedMsgInfo = if (setting.method == "POST" && setting.parseMode == "HTML") {
                msgInfo.copy(content = htmlEncode(msgInfo.content), simInfo = htmlEncode(msgInfo.simInfo))
            } else msgInfo

            val content: String = if (rule != null) {
                formattedMsgInfo.getContentForSend(rule.smsTemplate, rule.regexReplace, rule.title)
            } else {
                formattedMsgInfo.getContentForSend(SettingUtils.smsTemplate)
            }

            val spec = TelegramRequestBuilder.build(setting.method, setting.apiToken, setting.chatId, setting.messageThreadId, setting.parseMode, content)
            val request = if (spec.method == "GET") XHttp.get(spec.url) else XHttp.post(spec.url).upJson(spec.jsonBody!!)

            //设置代理
            if ((setting.proxyType == Proxy.Type.HTTP || setting.proxyType == Proxy.Type.SOCKS)
                && !TextUtils.isEmpty(setting.proxyHost) && !TextUtils.isEmpty(setting.proxyPort)
            ) {
                //代理服务器的IP和端口号
                val proxyPort = setting.proxyPort.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid proxy port")

                Log.d(TAG, "proxyHost = ${setting.proxyHost}, proxyPort = $proxyPort")
                request.okproxy(Proxy(setting.proxyType, InetSocketAddress(setting.proxyHost, proxyPort)))

                //代理的鉴权账号密码
                if (setting.proxyAuthenticator && (!TextUtils.isEmpty(setting.proxyUsername) || !TextUtils.isEmpty(setting.proxyPassword))
                ) {
                    if (setting.proxyType == Proxy.Type.HTTP) {
                        request.okproxyAuthenticator { _: Route?, response: Response ->
                            //设置代理服务器账号密码
                            val credential = Credentials.basic(setting.proxyUsername, setting.proxyPassword)
                            response.request().newBuilder()
                                .header("Proxy-Authorization", credential)
                                .build()
                        }
                    } else {
                        Authenticator.setDefault(object : Authenticator() {
                            override fun getPasswordAuthentication(): PasswordAuthentication {
                                return PasswordAuthentication(setting.proxyUsername, setting.proxyPassword.toCharArray())
                            }
                        })
                    }
                }
            }

            request.keepJson(true)
                .retryCount(SettingUtils.requestRetryTimes) //超时重试的次数
                .retryDelay(SettingUtils.requestDelayTime * 1000) //超时重试的延迟时间
                .retryIncreaseDelay(SettingUtils.requestDelayTime * 1000) //超时重试叠加延时
                .timeStamp(true) //url自动追加时间戳，避免缓存
                .addInterceptor(LoggingInterceptor(logId)) //增加一个log拦截器, 记录请求日志
                .execute(object : SimpleCallBack<String>() {

                    override fun onError(e: ApiException) {
                        Log.e(TAG, e.detailMessage)
                        val status = 0
                        SendUtils.updateLogs(logId, status, e.displayMessage)
                        SendUtils.senderLogic(status, msgInfo, rule, senderIndex, msgId)
                    }

                    override fun onSuccess(response: String) {
                        Log.i(TAG, SensitiveLogRedactor.redact(response))

                        val resp = Gson().fromJson(response, TelegramResult::class.java)
                        val status = if (resp?.ok == true) 2 else 0
                        SendUtils.updateLogs(logId, status, response)
                        SendUtils.senderLogic(status, msgInfo, rule, senderIndex, msgId)
                    }

                })

        }

        private fun htmlEncode(source: String?): String {
            if (source == null) {
                return ""
            }
            val buffer = StringBuffer()
            for (element in source) {
                when (element) {
                    '<' -> buffer.append("&lt;")
                    '>' -> buffer.append("&gt;")
                    '&' -> buffer.append("&amp;")
                    '"' -> buffer.append("&quot;")
                    //10, 13 -> buffer.append("\n")
                    else -> buffer.append(element)
                }
            }
            return buffer.toString()
        }

        // 用于转义 MarkdownV2 特殊字符的方法
        private fun escapeMarkdownV2(text: String): String {
            // TODO: MarkdownV2 要求转义以下字符，实测不能全部转义（丢失格式）
            //val specialChars = listOf('_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!')
            val specialChars = listOf('-')
            var escapedText = text
            for (char in specialChars) {
                // 将每个字符替换为带反斜杠的形式
                escapedText = escapedText.replace(char.toString(), "\\$char")
            }
            return escapedText
        }
    }
}
