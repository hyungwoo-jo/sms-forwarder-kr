package cn.ppps.forwarder.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import androidx.core.app.ActivityCompat
import cn.ppps.forwarder.App
import cn.ppps.forwarder.core.Core
import cn.ppps.forwarder.server.model.SmsSendData
import cn.ppps.forwarder.service.HttpServerService
import com.google.gson.Gson
import com.xuexiang.xrouter.utils.TextUtils
import com.xuexiang.xutil.XUtil
import com.xuexiang.xutil.security.CipherUtils
import com.xuexiang.xutil.system.DeviceUtils
import frpclib.Frpclib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

@Suppress("OPT_IN_USAGE", "DeferredResultUnused", "DEPRECATION")
class SmsCommandUtils {

    companion object {

        var TAG = "SmsCommandUtils"

        fun check(smsContent: String): Boolean {
            return smsContent.startsWith("smsf#")
        }

        fun execute(context: Context, smsCommand: String): Boolean {
            val cmdList = smsCommand.split("#", limit = 3)
            Log.d(TAG, "smsCommand = $smsCommand, cmdList = $cmdList")
            if (cmdList.count() < 2) return false

            val function = cmdList[0]
            val action = cmdList[1]
            val param = if (cmdList.count() > 2) cmdList[2] else ""
            when (function) {
                "frpc" -> {
                    if (!App.FrpclibInited) {
                        Log.d(TAG, "Frpc 라이브러리가 설치되지 않았습니다")
                        return false
                    }

                    GlobalScope.async(Dispatchers.IO) {
                        val frpcList = if (param.isEmpty()) {
                            Core.frpc.getAutorun()
                        } else {
                            val uids = param.split(",")
                            Core.frpc.getByUids(uids, param)
                        }

                        if (frpcList.isEmpty()) {
                            Log.d(TAG, "실행할 Frpc 구성이 없습니다")
                            return@async
                        }

                        for (frpc in frpcList) {
                            if (action == "start") {
                                if (!Frpclib.isRunning(frpc.uid)) {
                                    val error = Frpclib.runContent(frpc.uid, frpc.config)
                                    if (!TextUtils.isEmpty(error)) {
                                        Log.e(TAG, error)
                                    }
                                }
                            } else if (action == "stop") {
                                if (Frpclib.isRunning(frpc.uid)) {
                                    Frpclib.close(frpc.uid)
                                }
                            }
                        }
                    }
                }

                "httpserver" -> {
                    Intent(context, HttpServerService::class.java).also {
                        if (action == "start") {
                            context.startService(it)
                        } else if (action == "stop") {
                            context.stopService(it)
                        }
                    }
                }

                "system" -> {
                    if (!DeviceUtils.isDeviceRooted()) return false

                    var duplicateMessagesLimits = SettingUtils.duplicateMessagesLimits * 1000L
                    if (duplicateMessagesLimits > 0L) {
                        duplicateMessagesLimits += 10000L
                        val key = CipherUtils.md5(smsCommand)
                        val timestamp: Long = System.currentTimeMillis()
                        var timestampPrev: Long by HistoryUtils(key, timestamp)
                        Log.d(TAG, "duplicateMessagesLimits=$duplicateMessagesLimits, timestamp=$timestamp, timestampPrev=$timestampPrev, msgInfo=$smsCommand")
                        if (timestampPrev != timestamp && timestamp - timestampPrev <= duplicateMessagesLimits) {
                            Log.e(TAG, "중복 메시지 필터")
                            timestampPrev = timestamp
                            return false
                        }
                        timestampPrev = timestamp
                    }

                    if (action == "reboot") {
                        DeviceUtils.reboot()
                    } else if (action == "shutdown") {
                        DeviceUtils.shutdown()
                    }
                }

                "wifi" -> {
                    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    if (action == "on") {
                        wifiManager.isWifiEnabled = true
                    } else if (action == "off") {
                        wifiManager.isWifiEnabled = false
                    }
                }

                "sms" -> {
                    if (action == "send") {
                        if (TextUtils.isEmpty(param)) return false

                        try {
                            val gson = Gson()
                            val smsSendData = gson.fromJson(param, SmsSendData::class.java)
                            Log.d(TAG, smsSendData.toString())

                            if (App.SimInfoList.isEmpty()) {
                                App.SimInfoList = PhoneUtils.getSimMultiInfo()
                            }
                            Log.d(TAG, App.SimInfoList.toString())

                            val simSlotIndex = smsSendData.simSlot - 1
                            val mSubscriptionId: Int = App.SimInfoList[simSlotIndex]?.mSubscriptionId ?: -1

                            if (ActivityCompat.checkSelfPermission(XUtil.getContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                                return false
                            }
                            PhoneUtils.sendSms(mSubscriptionId, smsSendData.phoneNumbers, smsSendData.msgContent)
                        } catch (e: Exception) {
                            Log.e(TAG, "Parsing SMS failed: " + e.message.toString())
                        }
                    }
                }
            }

            return true
        }
    }
}