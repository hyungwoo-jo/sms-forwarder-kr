package cn.ppps.forwarder.utils.sender

import cn.ppps.forwarder.database.entity.Rule
import cn.ppps.forwarder.entity.MsgInfo
import cn.ppps.forwarder.entity.result.GotifyResult
import cn.ppps.forwarder.entity.setting.GotifySetting
import cn.ppps.forwarder.utils.Log
import cn.ppps.forwarder.utils.SendUtils
import cn.ppps.forwarder.utils.SettingUtils
import cn.ppps.forwarder.utils.interceptor.BasicAuthInterceptor
import cn.ppps.forwarder.utils.interceptor.LoggingInterceptor
import com.google.gson.Gson
import com.xuexiang.xhttp2.XHttp
import com.xuexiang.xhttp2.callback.SimpleCallBack
import com.xuexiang.xhttp2.exception.ApiException

class GotifyUtils {
    companion object {

        private val TAG: String = GotifyUtils::class.java.simpleName

        fun sendMsg(
            setting: GotifySetting,
            msgInfo: MsgInfo,
            rule: Rule? = null,
            senderIndex: Int = 0,
            logId: Long = 0L,
            msgId: Long = 0L
        ) {
            val title: String = if (rule != null) {
                msgInfo.getTitleForSend(setting.title, rule.regexReplace, rule.title)
            } else {
                msgInfo.getTitleForSend(setting.title)
            }
            val content: String = if (rule != null) {
                msgInfo.getContentForSend(rule.smsTemplate, rule.regexReplace, rule.title)
            } else {
                msgInfo.getContentForSend(SettingUtils.smsTemplate)
            }

            val requestUrl: String = setting.webServer
            Log.i(TAG, "requestUrl:$requestUrl")

            val regex = "^(https?://)([^:]+):([^@]+)@(.+)"
            val matches = Regex(regex, RegexOption.IGNORE_CASE).findAll(requestUrl).toList().flatMap(MatchResult::groupValues)
            Log.i(TAG, "matches = $matches")
            val request = if (matches.isNotEmpty()) {
                XHttp.post(matches[1] + matches[4]).addInterceptor(BasicAuthInterceptor(matches[2], matches[3]))
            } else {
                XHttp.post(requestUrl)
            }

            request.params("title", title)
                .params("message", content)
                .params("priority", setting.priority)
                .keepJson(true)
                .retryCount(SettingUtils.requestRetryTimes)
                .retryDelay(SettingUtils.requestDelayTime * 1000)
                .retryIncreaseDelay(SettingUtils.requestDelayTime * 1000)
                .timeStamp(true)
                .addInterceptor(LoggingInterceptor(logId))
                .execute(object : SimpleCallBack<String>() {

                    override fun onError(e: ApiException) {
                        Log.e(TAG, e.detailMessage)
                        val status = 0
                        SendUtils.updateLogs(logId, status, e.displayMessage)
                        SendUtils.senderLogic(status, msgInfo, rule, senderIndex, msgId)
                    }

                    override fun onSuccess(response: String) {
                        Log.i(TAG, response)

                        val resp = Gson().fromJson(response, GotifyResult::class.java)
                        val status = if (resp?.id != null) 2 else 0
                        SendUtils.updateLogs(logId, status, response)
                        SendUtils.senderLogic(status, msgInfo, rule, senderIndex, msgId)
                    }

                })

        }

    }
}