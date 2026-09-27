package cn.ppps.forwarder.utils

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.Settings
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.text.TextUtils
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import cn.ppps.forwarder.App
import cn.ppps.forwarder.R
import cn.ppps.forwarder.core.Core
import cn.ppps.forwarder.entity.CallInfo
import cn.ppps.forwarder.entity.ContactInfo
import cn.ppps.forwarder.entity.SimInfo
import cn.ppps.forwarder.entity.SmsInfo
import com.xuexiang.xutil.XUtil
import com.xuexiang.xutil.app.IntentUtils
import com.xuexiang.xutil.data.DateUtils
import com.xuexiang.xutil.resource.ResUtils.getString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.min

@Suppress("DEPRECATION")
class PhoneUtils private constructor() {

    companion object {
        const val TAG = "PhoneUtils"

        fun getSimSlotCount() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            (App.context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).activeModemCount
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            (App.context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).phoneCount
        else
            -1

        @SuppressLint("Range")
        fun getSimMultiInfo(): MutableMap<Int, SimInfo> {
            val infoList = HashMap<Int, SimInfo>()
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    Log.d(TAG, "1. Android 5.1 이상: 시스템 API 사용")
                    val mSubscriptionManager = XUtil.getContext().getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                    ActivityCompat.checkSelfPermission(
                        XUtil.getContext(), permission.READ_PHONE_STATE
                    )
                    val activeSubscriptionInfoList: List<SubscriptionInfo>? = mSubscriptionManager.activeSubscriptionInfoList
                    if (!activeSubscriptionInfoList.isNullOrEmpty()) {
                        for (subscriptionInfo in activeSubscriptionInfoList) {
                            val simInfo = SimInfo()
                            simInfo.mCarrierName = subscriptionInfo.carrierName?.toString()
                            simInfo.mIccId = subscriptionInfo.iccId?.toString()
                            simInfo.mSimSlotIndex = subscriptionInfo.simSlotIndex
                            simInfo.mNumber = subscriptionInfo.number?.toString()
                            simInfo.mCountryIso = subscriptionInfo.countryIso?.toString()
                            simInfo.mSubscriptionId = subscriptionInfo.subscriptionId
                            Log.d(TAG, simInfo.toString())
                            infoList[simInfo.mSimSlotIndex] = simInfo
                        }
                    }
                } else {
                    Log.d(TAG, "2. Android 5.1 미만: SIM 데이터베이스 접근 확인")
                    val uri = Uri.parse("content://telephony/siminfo")
                    val resolver: ContentResolver = XUtil.getContext().contentResolver
                    val cursor = resolver.query(
                        uri, arrayOf(
                            "_id", "icc_id", "sim_id", "display_name", "carrier_name", "name_source", "color", "number", "display_number_format", "data_roaming", "mcc", "mnc"
                        ), null, null, null
                    )
                    if (cursor != null && cursor.moveToFirst()) {
                        do {
                            val simInfo = SimInfo()
                            simInfo.mCarrierName = cursor.getString(cursor.getColumnIndex("carrier_name"))
                            simInfo.mIccId = cursor.getString(cursor.getColumnIndex("icc_id"))
                            simInfo.mSimSlotIndex = cursor.getInt(cursor.getColumnIndex("sim_id"))
                            simInfo.mNumber = cursor.getString(cursor.getColumnIndex("number"))
                            simInfo.mCountryIso = cursor.getString(cursor.getColumnIndex("mcc"))
                            //val id = cursor.getString(cursor.getColumnIndex("_id"))
                            Log.d(TAG, simInfo.toString())
                            infoList[simInfo.mSimSlotIndex] = simInfo
                        } while (cursor.moveToNext())
                        cursor.close()
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Log.e(TAG, "getSimMultiInfo:", e)
            }
            /*if (infoList.isEmpty() || infoList.size == 1) {
                Log.d(TAG, "3. 사용자가 입력한 SIM 설명 사용")
                if (infoList.isEmpty()) {
                    val etExtraSim1 = SettingUtils.extraSim1
                    if (!TextUtils.isEmpty(etExtraSim1)) {
                        val simInfo1 = SimInfo()
                        simInfo1.mSimSlotIndex = 0
                        simInfo1.mNumber = etExtraSim1
                        simInfo1.mSubscriptionId = SettingUtils.subidSim1
                        infoList[simInfo1.mSimSlotIndex] = simInfo1
                    }
                    val etExtraSim2 = SettingUtils.extraSim2
                    if (!TextUtils.isEmpty(etExtraSim2)) {
                        val simInfo2 = SimInfo()
                        simInfo2.mSimSlotIndex = 1
                        simInfo2.mNumber = etExtraSim2
                        simInfo2.mSubscriptionId = SettingUtils.subidSim2
                        infoList[simInfo2.mSimSlotIndex] = simInfo2
                    }

                } else {
                    var infoListIndex = -1
                    for (obj in infoList) {
                        infoListIndex = obj.key
                    }
                    if (infoListIndex == 0 && !TextUtils.isEmpty(SettingUtils.extraSim2)) {
                        val simInfo2 = SimInfo()
                        simInfo2.mSimSlotIndex = 1
                        simInfo2.mNumber = SettingUtils.extraSim2
                        simInfo2.mSubscriptionId = SettingUtils.subidSim1
                        infoList[simInfo2.mSimSlotIndex] = simInfo2
                    } else if (infoListIndex == 1 && !TextUtils.isEmpty(SettingUtils.extraSim1)) {
                        val simInfo1 = SimInfo()
                        simInfo1.mSimSlotIndex = 0
                        simInfo1.mNumber = SettingUtils.extraSim1
                        simInfo1.mSubscriptionId = SettingUtils.subidSim1
                        infoList[simInfo1.mSimSlotIndex] = simInfo1
                    }
                }
            }*/
            Log.i(TAG, infoList.toString())
            return infoList
        }

        fun getDeviceName(): String {
            return try {
                Settings.Secure.getString(XUtil.getContentResolver(), "bluetooth_name")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "getDeviceName:", e)
                Build.BRAND + " " + Build.MODEL
            }
        }

        /**
         *
         */
        @Suppress("DEPRECATION")
        @SuppressLint("SoonBlockedPrivateApi", "DiscouragedPrivateApi")
        @RequiresPermission(permission.SEND_SMS)
        fun sendSms(subId: Int, mobileList: String, message: String): String? {
            if (TextUtils.isEmpty(mobileList) || TextUtils.isEmpty(message)) {
                Log.e(TAG, "mobileList or message is empty!")
                return "mobileList or message is empty!"
            }

            val mobiles = mobileList.replace("；", ";").replace("，", ";").replace(",", ";")
            Log.d(TAG, "subId = $subId, mobiles = $mobiles, message = $message")
            val mobileArray = mobiles.split(";".toRegex()).toTypedArray()
            for (mobile in mobileArray) {
                Log.d(TAG, "mobile = $mobile")
                if (!isValidPhoneNumber(mobile)) {
                    Log.e(TAG, "mobile ($mobile) is invalid!")
                    continue
                }

                try {
                    val sendFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_ONE_SHOT
                    val sendPI = PendingIntent.getBroadcast(XUtil.getContext(), 0, Intent(), sendFlags)

                    val smsManager = if (subId > -1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) SmsManager.getSmsManagerForSubscriptionId(
                        subId
                    ) else SmsManager.getDefault()
                    if (subId > -1 && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
                        Log.d(TAG, "Android 5.1.1 미만: 리플렉션으로 SIM 슬롯 지정")
                        val clz = SmsManager::class.java
                        val field = clz.getDeclaredField("mSubId")
                        field.isAccessible = true
                        field.set(smsManager, subId)
                    }

                    if (message.length >= 70) {
                        val deliverFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) PendingIntent.FLAG_IMMUTABLE else 0
                        val deliverPI = PendingIntent.getBroadcast(
                            XUtil.getContext(), 0, Intent("DELIVERED_SMS_ACTION"), deliverFlags
                        )

                        val sentPendingIntents = ArrayList<PendingIntent>()
                        val deliveredPendingIntents = ArrayList<PendingIntent>()
                        val divideContents = smsManager.divideMessage(message)

                        for (i in divideContents.indices) {
                            sentPendingIntents.add(i, sendPI)
                            deliveredPendingIntents.add(i, deliverPI)
                        }
                        smsManager.sendMultipartTextMessage(
                            mobile, null, divideContents, sentPendingIntents, deliveredPendingIntents
                        )
                    } else {
                        smsManager.sendTextMessage(mobile, null, message, sendPI, null)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.message.toString())
                    return e.message.toString()
                }
            }

            return null
        }

        fun getCallInfoList(
            type: Int, limit: Int, offset: Int, phoneNumber: String?
        ): MutableList<CallInfo> {
            val callInfoList: MutableList<CallInfo> = mutableListOf()
            try {
                var selection = "1=1"
                val selectionArgs = ArrayList<String>()
                if (type > 0) {
                    selection += " and " + CallLog.Calls.TYPE + " = ?"
                    selectionArgs.add("$type")
                }
                if (!TextUtils.isEmpty(phoneNumber)) {
                    selection += " and " + CallLog.Calls.NUMBER + " like ?"
                    selectionArgs.add("%$phoneNumber%")
                }
                Log.d(TAG, "selection = $selection")
                Log.d(TAG, "selectionArgs = $selectionArgs")

                val cursor = Core.app.contentResolver.query(
                    CallLog.Calls.CONTENT_URI, null, selection, selectionArgs.toTypedArray(), CallLog.Calls.DEFAULT_SORT_ORDER // + " limit $limit offset $offset"
                ) ?: return callInfoList
                Log.i(TAG, "cursor count:" + cursor.count)

                if (cursor.count == 0 || offset >= cursor.count) {
                    cursor.close()
                    return callInfoList
                }

                if (cursor.moveToFirst()) {
                    Log.d(TAG, "Call ColumnNames=${cursor.columnNames.contentToString()}")
                    val indexName = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
                    val indexNumber = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                    val indexDate = cursor.getColumnIndex(CallLog.Calls.DATE)
                    val indexDuration = cursor.getColumnIndex(CallLog.Calls.DURATION)
                    val indexType = cursor.getColumnIndex(CallLog.Calls.TYPE)
                    val indexViaNumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && cursor.getColumnIndex("via_number") != -1) cursor.getColumnIndex("via_number") else -1
                    var indexSimId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID) else -1
                    var indexSubId = indexSimId
                    val forwardColumns = cursor.columnNames.filter { it.contains("forward", ignoreCase = true) }

                    /**
                     */
                    var isSimId = false
                    val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
                    Log.i(TAG, "manufacturer = $manufacturer")
                    if (manufacturer.contains(Regex(pattern = "xiaomi|redmi"))) {
                        if (cursor.getColumnIndex("simid") != -1) indexSimId = cursor.getColumnIndex("simid")
                        indexSubId = indexSimId
                    } else if (manufacturer.contains(Regex(pattern = "huawei|honor"))) {
                        indexSubId = -1
                        isSimId = true
                    }

                    var curOffset = 0
                    do {
                        if (curOffset >= offset) {
                            var isForwarded = false;
                            for (forwardColumn in forwardColumns) {
                                val forwardIndex = cursor.getColumnIndex(forwardColumn)
                                if (forwardIndex != -1) {
                                    val forwardedValue = cursor.getInt(forwardIndex)
                                    Log.d(TAG, "forwardColumn = $forwardColumn, forwardedValue = $forwardedValue")
                                    if (forwardedValue == 1) {
                                        isForwarded = true
                                        break
                                    }
                                }
                            }

                            val callInfo = CallInfo(
                                cursor.getString(indexName) ?: "",
                                cursor.getString(indexNumber) ?: "",
                                cursor.getLong(indexDate),
                                cursor.getInt(indexDuration),
                                cursor.getInt(indexType),
                                if (indexViaNumber != -1) cursor.getString(indexViaNumber) else "",
                                if (indexSimId != -1) getSimId(cursor.getInt(indexSimId), isSimId) else -1,
                                if (indexSubId != -1) cursor.getInt(indexSubId) else 0,
                                isForwarded
                            )
                            Log.d(TAG, callInfo.toString())
                            callInfoList.add(callInfo)
                            if (limit == 1) {
                                cursor.close()
                                return callInfoList
                            }
                        }
                        curOffset++
                        if (curOffset >= offset + limit) break
                    } while (cursor.moveToNext())
                    if (!cursor.isClosed) cursor.close()
                }
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "getCallInfoList:", e)
            }

            return callInfoList
        }

        @SuppressLint("Range")
        fun getLastCallInfo(callType: Int, phoneNumber: String?): CallInfo? {
            val callInfoList = getCallInfoList(callType, 1, 0, phoneNumber)
            if (callInfoList.isNotEmpty()) return callInfoList[0]
            return null
        }

        fun getContactInfoList(
            limit: Int, offset: Int, phoneNumber: String?, name: String?, isFuzzy: Boolean = true
        ): MutableList<ContactInfo> {
            val contactInfoList: MutableList<ContactInfo> = mutableListOf()

            try {
                var selection = "1=1"
                val selectionArgs = ArrayList<String>()
                if (!TextUtils.isEmpty(phoneNumber)) {
                    selection += " and replace(replace(" + ContactsContract.CommonDataKinds.Phone.NUMBER + ",' ',''),'-','') like ?"
                    if (isFuzzy) {
                        selectionArgs.add("%$phoneNumber%")
                    } else {
                        selectionArgs.add("%$phoneNumber")
                    }
                }
                if (!TextUtils.isEmpty(name)) {
                    selection += " and " + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like ?"
                    selectionArgs.add("%$name%")
                }
                Log.d(TAG, "selection = $selection")
                Log.d(TAG, "selectionArgs = $selectionArgs")

                val cursor = Core.app.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, selection, selectionArgs.toTypedArray(), ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY
                ) ?: return contactInfoList
                Log.i(TAG, "cursor count:" + cursor.count)

                if (cursor.count == 0 || offset >= cursor.count) {
                    cursor.close()
                    return contactInfoList
                }

                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val mobileNoIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    do {
                        val contactInfo = ContactInfo(
                            cursor.getString(displayNameIndex),
                            cursor.getString(mobileNoIndex),
                        )
                        Log.d(TAG, contactInfo.toString())
                        contactInfoList.add(contactInfo)
                        if (limit == 1) {
                            cursor.close()
                            return contactInfoList
                        }
                    } while (cursor.moveToNext())
                    if (!cursor.isClosed) cursor.close()
                }
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "getContactInfoList:", e)
            }

            return contactInfoList
        }

        // Retain template/API compatibility without any external number lookup.
        fun getPhoneArea(phoneNumber: String): String = getString(R.string.unknown_area)

        fun getContactByNumber(phoneNumber: String?): MutableList<ContactInfo> {
            val contactInfoList = mutableListOf<ContactInfo>()
            if (!SettingUtils.enableContactNames || ActivityCompat.checkSelfPermission(Core.app, permission.READ_CONTACTS) != android.content.pm.PackageManager.PERMISSION_GRANTED) return contactInfoList
            if (TextUtils.isEmpty(phoneNumber)) return contactInfoList

            val normalizedInputNumber = if (phoneNumber!!.startsWith("+") && phoneNumber.length > 4) {
                phoneNumber.substring(4).replace("[^0-9]".toRegex(), "")
            } else {
                phoneNumber.replace("[^0-9]".toRegex(), "")
            }

            contactInfoList.addAll(getContactInfoList(99, 0, normalizedInputNumber, null, false))
            if (contactInfoList.isEmpty() || contactInfoList.size == 1) {
                return contactInfoList
            }

            val scoredContacts = contactInfoList.map { contact ->
                val normalizedContactNumber = contact.phoneNumber.replace("[^0-9]".toRegex(), "")
                val matchLength = calculateMatchLength(normalizedInputNumber, normalizedContactNumber)
                val priority = when {
                    normalizedInputNumber == normalizedContactNumber -> 2
                    matchLength == normalizedInputNumber.length -> 1
                    else -> 0
                }
                contact to Pair(matchLength, priority)
            }.sortedWith(compareByDescending<Pair<ContactInfo, Pair<Int, Int>>> { it.second.first }
                .thenByDescending { it.second.second })

            val maxMatchLength = scoredContacts.first().second.first
            val maxPriority = scoredContacts.first().second.second
            return scoredContacts
                .filter { it.second.first == maxMatchLength && it.second.second == maxPriority }
                .map { it.first }
                .toMutableList()
        }

        private fun calculateMatchLength(number1: String, number2: String): Int {
            var matchLength = 0
            val minLength = min(number1.length, number2.length)

            for (i in 1..minLength) {
                if (number1[number1.length - i] == number2[number2.length - i]) {
                    matchLength++
                } else {
                    break
                }
            }

            return matchLength
        }

        fun getCallMsg(callInfo: CallInfo): String {
            val sb = StringBuilder()
            sb.append(getString(R.string.contact)).append(callInfo.name).append("\n")
            if (!TextUtils.isEmpty(callInfo.viaNumber)) sb.append(getString(R.string.via_number)).append(callInfo.viaNumber).append("\n")
            if (callInfo.dateLong > 0L) sb.append(getString(R.string.call_date)).append(
                DateUtils.millis2String(
                    callInfo.dateLong, SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                )
            ).append("\n")
            if (callInfo.duration > 0) {
                if (callInfo.type == 3) {
                    sb.append(getString(R.string.ring_duration))
                } else {
                    sb.append(getString(R.string.call_duration))
                }
                sb.append(callInfo.duration).append("s\n")
            }
            sb.append(getString(R.string.mandatory_type))
            when (callInfo.type) {
                1 -> sb.append(getString(R.string.incoming_call_ended))
                2 -> sb.append(getString(R.string.outgoing_call_ended))
                3 -> sb.append(getString(R.string.missed_call))
                4 -> sb.append(getString(R.string.incoming_call_received))
                5 -> sb.append(getString(R.string.incoming_call_answered))
                6 -> sb.append(getString(R.string.outgoing_call_started))
                else -> sb.append(getString(R.string.unknown_call))
            }
            if (callInfo.isForwarded) {
                sb.append("\n").append(getString(R.string.forwarding_call)).append(getString(R.string.lab_yes))
            }
            return sb.toString()
        }

        fun getSmsInfoList(
            type: Int, limit: Int, offset: Int, keyword: String
        ): MutableList<SmsInfo> {
            val smsInfoList: MutableList<SmsInfo> = mutableListOf()
            try {
                var selection = "1=1"
                val selectionArgs = ArrayList<String>()
                if (type > 0) {
                    selection += " and type = ?"
                    selectionArgs.add("$type")
                }
                if (!TextUtils.isEmpty(keyword)) {
                    selection += " and body like ?"
                    selectionArgs.add("%$keyword%")
                }
                Log.d(TAG, "selection = $selection")
                Log.d(TAG, "selectionArgs = $selectionArgs")

                val cursorTotal = Core.app.contentResolver.query(
                    Uri.parse("content://sms/"), null, selection, selectionArgs.toTypedArray(), "date desc"
                ) ?: return smsInfoList
                if (offset >= cursorTotal.count) {
                    cursorTotal.close()
                    return smsInfoList
                }

                val cursor = Core.app.contentResolver.query(
                    Uri.parse("content://sms/"), null, selection, selectionArgs.toTypedArray(), "date desc limit $limit offset $offset"
                ) ?: return smsInfoList

                Log.i(TAG, "cursor count:" + cursor.count)
                if (cursor.count == 0) {
                    cursor.close()
                    return smsInfoList
                }

                if (cursor.moveToFirst()) {
                    Log.d(TAG, "SMS ColumnNames=${cursor.columnNames.contentToString()}")
                    val indexAddress = cursor.getColumnIndex("address")
                    val indexBody = cursor.getColumnIndex("body")
                    val indexDate = cursor.getColumnIndex("date")
                    val indexType = cursor.getColumnIndex("type")
                    var indexSimId = cursor.getColumnIndex("sim_id")
                    var indexSubId = cursor.getColumnIndex("sub_id")

                    /**
                     */
                    var isSimId = false
                    val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
                    Log.i(TAG, "manufacturer = $manufacturer")
                    if (manufacturer.contains(Regex(pattern = "xiaomi|redmi"))) {
                        indexSubId = cursor.getColumnIndex("sim_id")
                    } else if (manufacturer.contains(Regex(pattern = "huawei|honor"))) {
                        indexSimId = cursor.getColumnIndex("sub_id")
                        isSimId = true
                    }

                    do {
                        val smsInfo = SmsInfo()
                        val phoneNumber = cursor.getString(indexAddress)
                        val contacts = getContactByNumber(phoneNumber)
                        smsInfo.name = if (contacts.isNotEmpty()) contacts[0].name else getString(R.string.unknown_number)
                        smsInfo.number = phoneNumber
                        smsInfo.content = cursor.getString(indexBody)
                        smsInfo.date = cursor.getLong(indexDate)
                        smsInfo.type = cursor.getInt(indexType)
                        smsInfo.simId = if (indexSimId != -1) getSimId(cursor.getInt(indexSimId), isSimId) else -1
                        smsInfo.subId = if (indexSubId != -1) cursor.getInt(indexSubId) else 0

                        smsInfoList.add(smsInfo)
                    } while (cursor.moveToNext())

                    if (!cursorTotal.isClosed) cursorTotal.close()
                    if (!cursor.isClosed) cursor.close()
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Log.e(TAG, "getSmsInfoList:", e)
            }
            return smsInfoList
        }

        /**
         *
         */
        fun dial(phoneNumber: String?) {
            XUtil.getContext().startActivity(IntentUtils.getDialIntent(phoneNumber, true))
        }

        /**
         *
         *
         */
        fun call(phoneNumber: String?) {
            XUtil.getContext().startActivity(IntentUtils.getCallIntent(phoneNumber, true))
        }

        /**
         *
         *
         * @param mId SubscriptionId
         */
        private fun getSimId(mId: Int, isSimId: Boolean): Int {
            Log.i(TAG, "mId = $mId, isSimId = $isSimId")
            if (isSimId) return mId

            if (SettingUtils.subidSim1 > 0 || SettingUtils.subidSim2 > 0) {
                return if (mId == SettingUtils.subidSim1) 0 else 1
            } else {
                if (App.SimInfoList.isEmpty()) {
                    App.SimInfoList = getSimMultiInfo()
                }
                Log.i(TAG, "SimInfoList = " + App.SimInfoList.toString())

                val simSlot = -1
                if (App.SimInfoList.isEmpty()) return simSlot
                for (simInfo in App.SimInfoList.values) {
                    if (simInfo.mSubscriptionId == mId && simInfo.mSimSlotIndex != -1) {
                        Log.i(TAG, "simInfo = $simInfo")
                        return simInfo.mSimSlotIndex
                    }
                }
                return simSlot
            }
        }

        private fun isValidPhoneNumber(phoneNumber: String): Boolean {
            val regex = Regex("^\\+?\\d{3,20}$")
            return regex.matches(phoneNumber)
        }

    }

    init {
        throw UnsupportedOperationException("u can't instantiate me...")
    }
}
