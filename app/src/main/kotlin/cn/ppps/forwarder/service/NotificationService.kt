package cn.ppps.forwarder.service

import android.annotation.SuppressLint
import android.app.Notification
import android.content.ComponentName
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import cn.ppps.forwarder.core.Core
import cn.ppps.forwarder.database.entity.Rule
import cn.ppps.forwarder.entity.MsgInfo
import cn.ppps.forwarder.utils.Log
import cn.ppps.forwarder.utils.PACKAGE_NAME
import cn.ppps.forwarder.utils.SettingUtils
import cn.ppps.forwarder.utils.Worker
import cn.ppps.forwarder.workers.SendWorker
import com.google.gson.Gson
import com.xuexiang.xrouter.utils.TextUtils
import com.xuexiang.xutil.display.ScreenUtils
import java.util.Date


@Suppress("PrivatePropertyName", "DEPRECATION")
class NotificationService : NotificationListenerService() {

    private val TAG: String = NotificationService::class.java.simpleName

    override fun onListenerConnected() {
        Log.d(TAG, "onListenerConnected")
    }

    override fun onListenerDisconnected() {
        if (SettingUtils.enablePureClientMode) return

        if (!SettingUtils.enableAppNotify) return

        Log.d(TAG, "알림 수신 서비스 연결이 끊어져 재연결을 요청합니다")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestRebind(ComponentName(this, NotificationListenerService::class.java))
        }
    }

    @SuppressLint("DiscouragedPrivateApi")
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        try {
            if (SettingUtils.enablePureClientMode) return

            val notification = sbn?.notification ?: return
            val extras = notification.extras ?: return

            SettingUtils.cancelExtraAppNotify
                .takeIf { it.isNotEmpty() }
                ?.split("\n")
                ?.forEach { app ->
                    if (sbn.packageName == app.trim()) {
                        Log.d(TAG, "추가 앱 알림 자동 지우기: $app")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            cancelNotification(sbn.key)
                        } else {
                            cancelNotification(sbn.packageName, sbn.tag, sbn.id)
                        }
                        return@forEach
                    }
                }


            if (!SettingUtils.enableAppNotify) return

            if (SettingUtils.enableNotUserPresent && !ScreenUtils.isScreenLock()) return

            val from = sbn.packageName
            if (PACKAGE_NAME == sbn.packageName) return
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            var text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
                if (bigText.isNotEmpty()) {
                    text = bigText
                }
            }
            if (text.isEmpty() && notification.tickerText != null) {
                text = notification.tickerText.toString()
            }

            if (TextUtils.isEmpty(title) && TextUtils.isEmpty(text)) return

            if (isInBlacklist(title, text)) {
                Log.d(TAG, "앱 알림이 차단 키워드에 일치하여 전달을 건너뜁니다. title=$title, text=$text")
                return
            }

            val msgInfo = MsgInfo("app", from, text, Date(), title, -1)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.d(TAG, "메시지 UID====>" + sbn.uid)
                msgInfo.uid = sbn.uid
            }
            if (SettingUtils.enableCancelAppNotify) {
                val ruleList: List<Rule> = Core.rule.getRuleList(msgInfo.type, 1, "SIM0")
                for (rule in ruleList) {
                    if (rule.checkMsg(msgInfo)) {
                        Log.d(TAG, "알림 자동 지우기")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            cancelNotification(sbn.key)
                        } else {
                            cancelNotification(sbn.packageName, sbn.tag, sbn.id)
                        }
                        break
                    }
                }
            }

            val request = OneTimeWorkRequestBuilder<SendWorker>().setInputData(
                workDataOf(
                    Worker.SEND_MSG_INFO to Gson().toJson(msgInfo),
                )
            ).build()
            WorkManager.getInstance(applicationContext).enqueue(request)

        } catch (e: Exception) {
            Log.e(TAG, "Parsing Notification failed: " + e.message.toString())
        }

    }

    private fun isInBlacklist(title: String, text: String): Boolean {
        val blacklist = SettingUtils.appNotifyBlacklist
        if (TextUtils.isEmpty(blacklist)) return false

        for (line in blacklist.split("\n")) {
            val keyword = line.trim()
            if (keyword.isEmpty()) continue
            try {
                val regex = Regex(keyword)
                if (regex.containsMatchIn(title) || regex.containsMatchIn(text)) {
                    return true
                }
            } catch (e: Exception) {
                Log.w(TAG, "차단 키워드 정규식이 잘못되어 포함 여부로 검사합니다: $keyword, ${e.message}")
                if (title.contains(keyword) || text.contains(keyword)) {
                    return true
                }
            }
        }
        return false
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        Log.d(TAG, "Removed Package Name : ${sbn?.packageName}")
    }

}