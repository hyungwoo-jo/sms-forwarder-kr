package cn.ppps.forwarder.utils


import android.text.TextUtils
import android.util.Base64
import cn.ppps.forwarder.R
import cn.ppps.forwarder.core.Core
import cn.ppps.forwarder.entity.CloneInfo
import cn.ppps.forwarder.entity.LocationInfo
import cn.ppps.forwarder.server.model.BaseRequest
import com.google.gson.Gson
import com.xuexiang.xutil.resource.ResUtils.getString
import com.yanzhenjie.andserver.error.HttpException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 */
@Suppress("UselessCallOnNotNull")
class HttpServerUtils private constructor() {

    companion object {

        private val VERSION_COMPAT_MAP: Map<Int, IntRange> = mapOf(
            55 to (54..55)
        )

        var enableServerAutorun: Boolean
            get() = false
            set(value) { /* Remote-control server removed from this edition. */ }

        var serverSignKey: String by SharedPreference(SP_SERVER_SIGN_KEY, "")

        var safetyMeasures: Int by SharedPreference(SP_SERVER_SAFETY_MEASURES, if (TextUtils.isEmpty(serverSignKey)) 0 else 1)

        var serverSm4Key: String by SharedPreference(SP_SERVER_SM4_KEY, "")

        var serverPublicKey: String by SharedPreference(SP_SERVER_PUBLIC_KEY, "")

        var serverPrivateKey: String by SharedPreference(SP_SERVER_PRIVATE_KEY, "")

        var timeTolerance: Int by SharedPreference(SP_SERVER_TIME_TOLERANCE, 600)

        var serverWebPath: String by SharedPreference(SP_SERVER_WEB_PATH, "")

        var serverPort: Int by SharedPreference(SP_SERVER_PORT, HTTP_SERVER_PORT)

        var serverAddress: String by SharedPreference(SP_SERVER_ADDRESS, "http://127.0.0.1:$serverPort")

        var serverHistory: String by SharedPreference(SP_SERVER_HISTORY, "")

        var serverConfig: String by SharedPreference(SP_SERVER_CONFIG, "")

        var clientSignKey: String by SharedPreference(SP_CLIENT_SIGN_KEY, "")

        var clientSafetyMeasures: Int by SharedPreference(SP_CLIENT_SAFETY_MEASURES, if (TextUtils.isEmpty(clientSignKey)) 0 else 1)

        var enableApiClone: Boolean by SharedPreference(SP_ENABLE_API_CLONE, true)

        var enableApiSmsSend: Boolean by SharedPreference(SP_ENABLE_API_SMS_SEND, true)

        var enableApiSmsQuery: Boolean by SharedPreference(SP_ENABLE_API_SMS_QUERY, true)

        var enableApiCallQuery: Boolean by SharedPreference(SP_ENABLE_API_CALL_QUERY, true)

        var enableApiContactQuery: Boolean by SharedPreference(SP_ENABLE_API_CONTACT_QUERY, true)

        var enableApiContactAdd: Boolean by SharedPreference(SP_ENABLE_API_CONTACT_ADD, true)

        var enableApiBatteryQuery: Boolean by SharedPreference(SP_ENABLE_API_BATTERY_QUERY, true)

        var enableApiWol: Boolean by SharedPreference(SP_ENABLE_API_WOL, true)

        var enableApiLocation: Boolean by SharedPreference(SP_ENABLE_API_LOCATION, false)

        var apiLocationCache: LocationInfo by SharedPreference(SP_API_LOCATION_CACHE, LocationInfo())

        var wolHistory: String by SharedPreference(SP_WOL_HISTORY, "")

        fun calcSign(timestamp: String, signSecret: String): String {
            val stringToSign = "$timestamp\n" + signSecret
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(signSecret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
            val signData = mac.doFinal(stringToSign.toByteArray(StandardCharsets.UTF_8))
            return URLEncoder.encode(String(Base64.encode(signData, Base64.NO_WRAP)), "UTF-8")
        }

        @Throws(HttpException::class)
        fun checkSign(req: BaseRequest<*>) {
            val signSecret = serverSignKey
            if (TextUtils.isEmpty(signSecret)) return

            if (TextUtils.isEmpty(req.sign)) throw HttpException(500, getString(R.string.sign_required))
            if (req.timestamp == 0L) throw HttpException(500, getString(R.string.timestamp_required))

            val timestamp = System.currentTimeMillis()
            val diffTime = kotlin.math.abs(timestamp - req.timestamp)
            val tolerance = timeTolerance * 1000L
            if (diffTime > tolerance) {
                throw HttpException(500, String.format(getString(R.string.timestamp_verify_failed), timestamp, timeTolerance, diffTime))
            }

            val sign = calcSign(req.timestamp.toString(), signSecret)
            if (sign != req.sign) {
                Log.e("calcSign", sign)
                Log.e("reqSign", req.sign.toString())
                throw HttpException(500, getString(R.string.sign_verify_failed))
            }
        }

        @Throws(HttpException::class)
        fun compareVersion(cloneInfo: CloneInfo) {
            val versionCode = cloneInfo.versionCode
            if (versionCode == 0) throw HttpException(500, getString(R.string.version_code_required))

            val requestVersion = versionCode.toString().substring(1).toInt()
            val localVersion = AppUtils.getAppVersionCode().toString().substring(1).toInt()
            val compatibleRange = VERSION_COMPAT_MAP[localVersion]
            Log.d("HttpServerUtils", "compareVersion: localVersion=$localVersion, requestVersion=$requestVersion, compatibleRange=$compatibleRange")
            val isCompatible = if (compatibleRange != null) {
                requestVersion in compatibleRange
            } else {
                requestVersion == localVersion
            }
            if (!isCompatible) {
                throw HttpException(500, getString(R.string.inconsistent_version))
            }
        }

        fun exportSettings(): CloneInfo {
            val cloneInfo = CloneInfo()
            cloneInfo.versionCode = AppUtils.getAppVersionCode()
            cloneInfo.versionName = AppUtils.getAppVersionName()
            cloneInfo.settings = SharedPreference.exportPreference()
            cloneInfo.senderList = Core.sender.getAllNonCache()
            cloneInfo.ruleList = Core.rule.getAllNonCache()
            cloneInfo.frpcList = Core.frpc.getAllNonCache()
            cloneInfo.taskList = Core.task.getAllNonCache()
            return cloneInfo
        }

        fun restoreSettings(cloneInfo: CloneInfo): Boolean {
            return try {
                val extraDeviceMark = SettingUtils.extraDeviceMark
                val subidSim1 = SettingUtils.subidSim1
                val extraSim1 = SettingUtils.extraSim1
                val subidSim2 = SettingUtils.subidSim2
                val extraSim2 = SettingUtils.extraSim2
                SharedPreference.clearPreference()
                SharedPreference.importPreference(cloneInfo.settings)
                SettingUtils.extraDeviceMark = extraDeviceMark
                SettingUtils.subidSim1 = subidSim1
                SettingUtils.extraSim1 = extraSim1
                SettingUtils.subidSim2 = subidSim2
                SettingUtils.extraSim2 = extraSim2
                Core.logs.deleteAll()
                Core.msg.deleteAll()
                Core.sender.deleteAll()
                if (!cloneInfo.senderList.isNullOrEmpty()) {
                    for (sender in cloneInfo.senderList!!) {
                        Core.sender.insert(sender)
                    }
                }
                Core.rule.deleteAll()
                if (!cloneInfo.ruleList.isNullOrEmpty()) {
                    for (rule in cloneInfo.ruleList!!) {
                        if (rule.title.isNullOrEmpty()) rule.title = ""
                        Core.rule.insert(rule)
                    }
                }
                Core.frpc.deleteAll()
                if (!cloneInfo.frpcList.isNullOrEmpty()) {
                    for (frpc in cloneInfo.frpcList!!) {
                        Core.frpc.insert(frpc)
                    }
                }
                Core.task.deleteAll()
                if (!cloneInfo.taskList.isNullOrEmpty()) {
                    for (task in cloneInfo.taskList!!) {
                        Core.task.insert(task)
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("restoreSettings", e.message.toString())
                throw HttpException(500, e.message)
                //false
            }
        }

        fun response(output: Any?): String {
            val resp: MutableMap<String, Any> = mutableMapOf()
            val timestamp = System.currentTimeMillis()
            resp["timestamp"] = timestamp
            if (output is String && output != "success") {
                resp["code"] = HTTP_FAILURE_CODE
                resp["msg"] = output
            } else {
                resp["code"] = HTTP_SUCCESS_CODE
                resp["msg"] = "success"
                if (output != null) {
                    resp["data"] = output
                }
                if (safetyMeasures == 1) {
                    resp["sign"] = calcSign(timestamp.toString(), serverSignKey)
                }
            }

            return Gson().toJson(resp)
        }
    }
}