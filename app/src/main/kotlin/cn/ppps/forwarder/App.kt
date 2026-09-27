package cn.ppps.forwarder

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.multidex.MultiDex
import androidx.work.Configuration
import androidx.work.WorkManager
import cn.ppps.forwarder.activity.MainActivity
import cn.ppps.forwarder.core.Core
import cn.ppps.forwarder.database.AppDatabase
import cn.ppps.forwarder.database.repository.FrpcRepository
import cn.ppps.forwarder.database.repository.LogsRepository
import cn.ppps.forwarder.database.repository.MsgRepository
import cn.ppps.forwarder.database.repository.RuleRepository
import cn.ppps.forwarder.database.repository.SenderRepository
import cn.ppps.forwarder.database.repository.TaskRepository
import cn.ppps.forwarder.entity.SimInfo
import cn.ppps.forwarder.receiver.BatteryReceiver
import cn.ppps.forwarder.receiver.BluetoothReceiver
import cn.ppps.forwarder.receiver.CactusReceiver
import cn.ppps.forwarder.receiver.LockScreenReceiver
import cn.ppps.forwarder.receiver.NetworkChangeReceiver
import cn.ppps.forwarder.service.BluetoothScanService
import cn.ppps.forwarder.service.ForegroundService
import cn.ppps.forwarder.service.HttpServerService
import cn.ppps.forwarder.service.LocationService
import cn.ppps.forwarder.utils.ACTION_START
import cn.ppps.forwarder.utils.AppInfo
import cn.ppps.forwarder.utils.CactusSave
import cn.ppps.forwarder.utils.FRONT_CHANNEL_ID
import cn.ppps.forwarder.utils.FRONT_CHANNEL_NAME
import cn.ppps.forwarder.utils.FRONT_NOTIFY_ID
import cn.ppps.forwarder.utils.FRPC_LIB_VERSION
import cn.ppps.forwarder.utils.HistoryUtils
import cn.ppps.forwarder.utils.HttpServerUtils
import cn.ppps.forwarder.utils.Log
import cn.ppps.forwarder.utils.ProximitySensorScreenHelper
import cn.ppps.forwarder.utils.SettingUtils
import cn.ppps.forwarder.utils.SharedPreference
import cn.ppps.forwarder.utils.sdkinit.XBasicLibInit
import com.gyf.cactus.Cactus
import com.gyf.cactus.callback.CactusCallback
import com.gyf.cactus.ext.cactus
import com.hjq.language.MultiLanguages
import com.hjq.language.OnLanguageListener
import com.king.location.LocationClient
import frpclib.Frpclib
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
class App : Application(), CactusCallback, Configuration.Provider by Core {

    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { AppDatabase.getInstance(this) }
    val frpcRepository by lazy { FrpcRepository(database.frpcDao()) }
    val msgRepository by lazy { MsgRepository(database.msgDao()) }
    val logsRepository by lazy { LogsRepository(database.logsDao()) }
    val ruleRepository by lazy { RuleRepository(database.ruleDao()) }
    val senderRepository by lazy { SenderRepository(database.senderDao()) }
    val taskRepository by lazy { TaskRepository(database.taskDao()) }

    companion object {
        const val TAG: String = "SmsForwarder"

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        var COMMON_TAG_MAP: MutableMap<String, String> = mutableMapOf()
        var SMS_TAG_MAP: MutableMap<String, String> = mutableMapOf()
        var CALL_TAG_MAP: MutableMap<String, String> = mutableMapOf()
        var APP_TAG_MAP: MutableMap<String, String> = mutableMapOf()
        var LOCATION_TAG_MAP: MutableMap<String, String> = mutableMapOf()
        var BATTERY_TAG_MAP: MutableMap<String, String> = mutableMapOf()
        var NETWORK_TAG_MAP: MutableMap<String, String> = mutableMapOf()

        var CALL_TYPE_MAP: MutableMap<String, String> = mutableMapOf()
        var FILED_MAP: MutableMap<String, String> = mutableMapOf()
        var CHECK_MAP: MutableMap<String, String> = mutableMapOf()
        var SIM_SLOT_MAP: MutableMap<String, String> = mutableMapOf()
        var FORWARD_STATUS_MAP: MutableMap<Int, String> = mutableMapOf()
        var BARK_LEVEL_MAP: MutableMap<String, String> = mutableMapOf()
        var BARK_ENCRYPTION_ALGORITHM_MAP: MutableMap<String, String> = mutableMapOf()

        var SimInfoList: MutableMap<Int, SimInfo> = mutableMapOf()

        var LoadingAppList = false
        var UserAppList: MutableList<AppInfo> = mutableListOf()
        var SystemAppList: MutableList<AppInfo> = mutableListOf()

        /**
         */
        var isDebug: Boolean = BuildConfig.DEBUG

        val mEndDate = MutableLiveData<String>()
        val mLastTimer = MutableLiveData<String>()
        val mTimer = MutableLiveData<String>()
        val mStatus = MutableLiveData<Boolean>().apply { value = true }
        var mDisposable: Disposable? = null

        val LocationClient by lazy { LocationClient(context) }
        val DateFormat by lazy { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

        var FrpclibInited = false

        var isNeedSpaceBetweenWords = false
    }

    override fun attachBaseContext(base: Context) {
        //super.attachBaseContext(base)
        super.attachBaseContext(MultiLanguages.attach(base))
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            throwable.printStackTrace()
            try {
                val logPath = this.cacheDir.absolutePath + "/logs"
                val logDir = File(logPath)
                if (!logDir.exists()) logDir.mkdirs()
                val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                val currentDateTime = dateFormat.format(Date())
                val logFile = File(logPath, "crash_$currentDateTime.txt")
                BufferedWriter(FileWriter(logFile, true)).use { writer ->
                    writer.append("Application failure")
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }

        try {
            context = applicationContext
            initLibs()

            if (SettingUtils.enablePureClientMode) return

            WorkManager.initialize(this, Configuration.Builder().build())

            FrpclibInited = false

            val foregroundServiceIntent = Intent(this, ForegroundService::class.java)
            foregroundServiceIntent.action = ACTION_START
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(foregroundServiceIntent)
            } else {
                startService(foregroundServiceIntent)
            }


            ProximitySensorScreenHelper.refresh(this)
            if (SettingUtils.enableCactus) {
                registerReceiver(CactusReceiver(), IntentFilter().apply {
                    addAction(Cactus.CACTUS_WORK)
                    addAction(Cactus.CACTUS_STOP)
                    addAction(Cactus.CACTUS_BACKGROUND)
                    addAction(Cactus.CACTUS_FOREGROUND)
                })
                val activityIntent = Intent(this, MainActivity::class.java)
                val flags = if (Build.VERSION.SDK_INT >= 30) PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
                val pendingIntent = PendingIntent.getActivity(this, 0, activityIntent, flags)
                cactus {
                    setServiceId(FRONT_NOTIFY_ID)
                    setChannelId(FRONT_CHANNEL_ID)
                    setChannelName(FRONT_CHANNEL_NAME)
                    setTitle(getString(R.string.app_name))
                    setContent(SettingUtils.notifyContent)
                    setSmallIcon(R.drawable.ic_forwarder)
                    setLargeIcon(R.mipmap.ic_launcher)
                    setPendingIntent(pendingIntent)
                    if (SettingUtils.enablePlaySilenceMusic) {
                        setMusicEnabled(true)
                        setBackgroundMusicEnabled(true)
                        setMusicId(R.raw.silence)
                        setMusicInterval(SettingUtils.musicInterval.toLong())
                        isDebug(true)
                    }
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && SettingUtils.enableOnePixelActivity) {
                        setOnePixEnabled(true)
                    }
                    setCrashRestartUIEnabled(true)
                    addCallback({
                        Log.d(TAG, "Cactus 유지 서비스: onStop")
                    }) {
                        Log.d(TAG, "Cactus 유지 서비스: doWork")
                    }
                    addBackgroundCallback {
                        Log.d(TAG, if (it) "SMS 자동전달: 백그라운드로 전환" else "SMS 자동전달: 포그라운드로 전환")
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "onCreate: $e")
        }
    }

    /**
     */
    private fun initLibs() {
        Core.init(this)
        SharedPreference.init(applicationContext)
        XBasicLibInit.init(this)
        isDebug = SettingUtils.enableDebugMode
        Log.init(applicationContext)
        HistoryUtils.init(applicationContext)
        MultiLanguages.init(this)
        MultiLanguages.setAppLanguage(this, Locale.KOREAN)
        MultiLanguages.setOnLanguageListener(object : OnLanguageListener {
            override fun onAppLocaleChange(oldLocale: Locale, newLocale: Locale) {
                Log.i(TAG, "앱 언어 변경: $oldLocale → $newLocale")
                switchLanguage(newLocale)
            }

            override fun onSystemLocaleChange(oldLocale: Locale, newLocale: Locale) {
                Log.i(TAG, "시스템 언어 변경: $oldLocale → $newLocale")
                switchLanguage(newLocale)
            }
        })
        switchLanguage(MultiLanguages.getAppLanguage(this))
    }

    @SuppressLint("CheckResult")
    override fun doWork(times: Int) {
        Log.d(TAG, "doWork:$times")
        mStatus.postValue(true)
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("GMT+00:00")
        var oldTimer = CactusSave.timer
        if (times == 1) {
            CactusSave.lastTimer = oldTimer
            CactusSave.endDate = CactusSave.date
            oldTimer = 0L
        }
        mLastTimer.postValue(dateFormat.format(Date(CactusSave.lastTimer * 1000)))
        mEndDate.postValue(CactusSave.endDate)
        mDisposable = Observable.interval(1, TimeUnit.SECONDS).map {
            oldTimer + it
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe { aLong ->
            CactusSave.timer = aLong
            CactusSave.date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).run {
                format(Date())
            }
            mTimer.value = dateFormat.format(Date(aLong * 1000))
        }
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        mStatus.postValue(false)
        mDisposable?.apply {
            if (!isDisposed) {
                dispose()
            }
        }
    }

    private fun switchLanguage(newLocale: Locale) {
        isNeedSpaceBetweenWords = !newLocale.language.contains("zh")

        COMMON_TAG_MAP.clear()
        COMMON_TAG_MAP.putAll(
            mapOf(
                getString(R.string.tag_receive_time) to getString(R.string.insert_tag_receive_time),
                getString(R.string.tag_current_time) to getString(R.string.insert_tag_current_time),
                getString(R.string.tag_device_name) to getString(R.string.insert_tag_device_name),
                getString(R.string.tag_app_version) to getString(R.string.insert_tag_app_version),
            )
        )
        SMS_TAG_MAP.clear()
        SMS_TAG_MAP.putAll(
            mapOf(
                getString(R.string.tag_from) to getString(R.string.insert_tag_from),
                getString(R.string.tag_sms) to getString(R.string.insert_tag_sms),
                getString(R.string.tag_card_slot) to getString(R.string.insert_tag_card_slot),
                getString(R.string.tag_card_subid) to getString(R.string.insert_tag_card_subid),
                getString(R.string.tag_contact_name) to getString(R.string.insert_tag_contact_name),
                getString(R.string.tag_phone_area) to getString(R.string.insert_tag_phone_area),
                getString(R.string.tag_rule_title) to getString(R.string.insert_tag_rule_title),
            )
        )
        CALL_TAG_MAP.clear()
        CALL_TAG_MAP.putAll(
            mapOf(
                getString(R.string.tag_from) to getString(R.string.insert_tag_from),
                getString(R.string.tag_sms) to getString(R.string.insert_tag_msg),
                getString(R.string.tag_card_slot) to getString(R.string.insert_tag_card_slot),
                getString(R.string.tag_card_subid) to getString(R.string.insert_tag_card_subid),
                getString(R.string.tag_call_type) to getString(R.string.insert_tag_call_type),
                getString(R.string.tag_contact_name) to getString(R.string.insert_tag_contact_name),
                getString(R.string.tag_phone_area) to getString(R.string.insert_tag_phone_area),
                getString(R.string.tag_rule_title) to getString(R.string.insert_tag_rule_title),
            )
        )
        APP_TAG_MAP.clear()
        APP_TAG_MAP.putAll(
            mapOf(
                getString(R.string.tag_uid) to getString(R.string.insert_tag_uid),
                getString(R.string.tag_package_name) to getString(R.string.insert_tag_package_name),
                getString(R.string.tag_app_name) to getString(R.string.insert_tag_app_name),
                getString(R.string.tag_title) to getString(R.string.insert_tag_title),
                getString(R.string.tag_msg) to getString(R.string.insert_tag_msg),
                getString(R.string.tag_rule_title) to getString(R.string.insert_tag_rule_title),
            )
        )
        LOCATION_TAG_MAP.clear()
        LOCATION_TAG_MAP.putAll(
            mapOf(
                getString(R.string.tag_location) to getString(R.string.insert_tag_location),
                getString(R.string.tag_location_longitude) to getString(R.string.insert_tag_location_longitude),
                getString(R.string.tag_location_latitude) to getString(R.string.insert_tag_location_latitude),
                getString(R.string.tag_location_address) to getString(R.string.insert_tag_location_address),
            )
        )
        BATTERY_TAG_MAP.clear()
        BATTERY_TAG_MAP.putAll(
            mapOf(
                getString(R.string.tag_battery_pct) to getString(R.string.insert_tag_battery_pct),
                getString(R.string.tag_battery_status) to getString(R.string.insert_tag_battery_status),
                getString(R.string.tag_battery_plugged) to getString(R.string.insert_tag_battery_plugged),
                getString(R.string.tag_battery_info) to getString(R.string.insert_tag_battery_info),
                getString(R.string.tag_battery_info_simple) to getString(R.string.insert_tag_battery_info_simple),
            )
        )
        NETWORK_TAG_MAP.clear()
        NETWORK_TAG_MAP.putAll(
            mapOf(
                getString(R.string.tag_ipv4) to getString(R.string.insert_tag_ipv4),
                getString(R.string.tag_ipv6) to getString(R.string.insert_tag_ipv6),
                getString(R.string.tag_ip_list) to getString(R.string.insert_tag_ip_list),
                getString(R.string.tag_net_type) to getString(R.string.insert_tag_net_type),
            )
        )

        CALL_TYPE_MAP.clear()
        CALL_TYPE_MAP.putAll(
            mapOf(
                //"0" to getString(R.string.unknown_call),
                "1" to getString(R.string.incoming_call_ended),
                "2" to getString(R.string.outgoing_call_ended),
                "3" to getString(R.string.missed_call),
                "4" to getString(R.string.incoming_call_received),
                "5" to getString(R.string.incoming_call_answered),
                "6" to getString(R.string.outgoing_call_started),
            )
        )

        FILED_MAP.clear()
        FILED_MAP.putAll(
            mapOf(
                "transpond_all" to getString(R.string.rule_transpond_all),
                "phone_num" to getString(R.string.rule_phone_num),
                "msg_content" to getString(R.string.rule_msg_content),
                "multi_match" to getString(R.string.rule_multi_match),
                "package_name" to getString(R.string.rule_package_name),
                "inform_content" to getString(R.string.rule_inform_content),
                "call_type" to getString(R.string.rule_call_type),
                "uid" to getString(R.string.rule_uid),
            )
        )

        CHECK_MAP.clear()
        CHECK_MAP.putAll(
            mapOf(
                "is" to getString(R.string.rule_is),
                "notis" to getString(R.string.rule_notis),
                "contain" to getString(R.string.rule_contain),
                "startwith" to getString(R.string.rule_startwith),
                "endwith" to getString(R.string.rule_endwith),
                "notcontain" to getString(R.string.rule_notcontain),
                "regex" to getString(R.string.rule_regex),
            )
        )

        SIM_SLOT_MAP.clear()
        SIM_SLOT_MAP.putAll(
            mapOf(
                "ALL" to getString(R.string.rule_any),
                "SIM1" to "SIM1",
                "SIM2" to "SIM2",
            )
        )

        FORWARD_STATUS_MAP.clear()
        FORWARD_STATUS_MAP.putAll(
            mapOf(
                0 to getString(R.string.failed),
                1 to getString(R.string.processing),
                2 to getString(R.string.success),
            )
        )

        BARK_LEVEL_MAP.clear()
        BARK_LEVEL_MAP.putAll(
            mapOf(
                "critical" to getString(R.string.bark_level_critical),
                "active" to getString(R.string.bark_level_active),
                "timeSensitive" to getString(R.string.bark_level_timeSensitive),
                "passive" to getString(R.string.bark_level_passive)
            )
        )

        BARK_ENCRYPTION_ALGORITHM_MAP.clear()
        BARK_ENCRYPTION_ALGORITHM_MAP.putAll(
            mapOf(
                "none" to getString(R.string.bark_encryption_algorithm_none),
                "AES128/CBC/PKCS7Padding" to "AES128/CBC/PKCS7Padding",
                "AES128/ECB/PKCS7Padding" to "AES128/ECB/PKCS7Padding",
                "AES192/CBC/PKCS7Padding" to "AES192/CBC/PKCS7Padding",
                "AES192/ECB/PKCS7Padding" to "AES192/ECB/PKCS7Padding",
                "AES256/CBC/PKCS7Padding" to "AES256/CBC/PKCS7Padding",
                "AES256/ECB/PKCS7Padding" to "AES256/ECB/PKCS7Padding",
                "AES256/GCM/NoPadding" to "AES256/GCM/NoPadding",
            )
        )
    }

}
