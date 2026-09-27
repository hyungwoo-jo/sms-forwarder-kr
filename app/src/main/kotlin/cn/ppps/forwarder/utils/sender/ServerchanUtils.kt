package cn.ppps.forwarder.utils.sender

import android.text.TextUtils
import cn.ppps.forwarder.database.entity.Rule
import cn.ppps.forwarder.entity.MsgInfo
import cn.ppps.forwarder.entity.result.ServerchanResult
import cn.ppps.forwarder.entity.setting.ServerchanSetting
import cn.ppps.forwarder.utils.Log
import cn.ppps.forwarder.utils.SendUtils
import cn.ppps.forwarder.utils.SettingUtils
import cn.ppps.forwarder.utils.interceptor.LoggingInterceptor
import com.google.gson.Gson
import com.xuexiang.xhttp2.XHttp
import com.xuexiang.xhttp2.callback.SimpleCallBack
import com.xuexiang.xhttp2.exception.ApiException

class ServerchanUtils {
    companion object {

        private val TAG: String = ServerchanUtils::class.java.simpleName

        fun sendMsg(
            setting: ServerchanSetting,
            msgInfo: MsgInfo,
            rule: Rule? = null,
            senderIndex: Int = 0,
            logId: Long = 0L,
            msgId: Long = 0L
        ) {
            val title: String = if (rule != null) {
                msgInfo.getTitleForSend(setting.titleTemplate, rule.regexReplace, rule.title)
            } else {
                msgInfo.getTitleForSend(setting.titleTemplate)
            }
            val content: String = if (rule != null) {
                msgInfo.getContentForSend(rule.smsTemplate, rule.regexReplace, rule.title)
            } else {
                msgInfo.getContentForSend(SettingUtils.smsTemplate)
            }

            val matchResult = Regex("^sctp(\\d+)t", RegexOption.IGNORE_CASE).find(setting.sendKey)
            val requestUrl = if (matchResult != null && matchResult.groups[1] != null) {
                "https://${matchResult.groups[1]?.value}.push.ft07.com/send/${setting.sendKey}.send"
            } else {
                String.format("https://sctapi.ftqq.com/%s.send", setting.sendKey)
            }

            Log.i(TAG, "requestUrl:$requestUrl")

            val request = XHttp.post(requestUrl)
                .params("title", title)
                .params("desp", content)

            if (!TextUtils.isEmpty(setting.channel)) request.params("channel", setting.channel)
            if (!TextUtils.isEmpty(setting.openid)) request.params("group", setting.openid)

            request.keepJson(true)
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
                        val resp = Gson().fromJson(response, ServerchanResult::class.java)
                        val status = if (resp?.code == 0L) 2 else 0
                        SendUtils.updateLogs(logId, status, response)
                        SendUtils.senderLogic(status, msgInfo, rule, senderIndex, msgId)
                    }

                })

        }

    }
}
