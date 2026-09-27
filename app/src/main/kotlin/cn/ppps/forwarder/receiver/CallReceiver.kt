package cn.ppps.forwarder.receiver

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.gson.Gson
import cn.ppps.forwarder.App.Companion.CALL_TYPE_MAP
import cn.ppps.forwarder.R
import cn.ppps.forwarder.entity.CallInfo
import cn.ppps.forwarder.entity.MsgInfo
import cn.ppps.forwarder.utils.Log
import cn.ppps.forwarder.utils.PhoneUtils
import cn.ppps.forwarder.utils.SettingUtils
import cn.ppps.forwarder.utils.Worker
import cn.ppps.forwarder.workers.SendWorker
import com.xuexiang.xrouter.utils.TextUtils
import com.xuexiang.xutil.resource.ResUtils.getString
import java.util.Date

open class CallReceiver : PhoneStateReceiver() {

    companion object {
        private val TAG = CallReceiver::class.java.simpleName

        //const val ACTION_IN = "android.intent.action.PHONE_STATE"
        const val ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL"
        const val EXTRA_PHONE_NUMBER = "android.intent.extra.PHONE_NUMBER"
    }

    override fun onIncomingCallReceived(context: Context, number: String?, start: Date) {
        Log.d(TAG, "onIncomingCallReceived：$number")
        sendNotice(context, 4, number)
    }

    override fun onIncomingCallAnswered(context: Context, number: String?, start: Date) {
        Log.d(TAG, "onIncomingCallAnswered：$number")
        sendNotice(context, 5, number)
    }

    override fun onIncomingCallEnded(context: Context, number: String?, start: Date, end: Date) {
        Log.d(TAG, "onIncomingCallEnded：$number")
        sendCallMsg(context, 1, number)
    }

    override fun onOutgoingCallStarted(context: Context, number: String?, start: Date) {
        Log.d(TAG, "onOutgoingCallStarted：$number")
        sendNotice(context, 6, number)
    }

    override fun onOutgoingCallEnded(context: Context, number: String?, start: Date, end: Date) {
        Log.d(TAG, "onOutgoingCallEnded：$number")
        sendCallMsg(context, 2, number)
    }

    override fun onMissedCall(context: Context, number: String?, start: Date) {
        Log.d(TAG, "onMissedCall：$number")
        sendCallMsg(context, 3, number)
    }

    private fun sendNotice(context: Context, callType: Int, phoneNumber: String?) {
        if (TextUtils.isEmpty(phoneNumber)) return

        if ((callType == 4 && !SettingUtils.enableCallType4) || (callType == 5 && !SettingUtils.enableCallType5) || (callType == 6 && !SettingUtils.enableCallType6)) {
            Log.w(TAG, "이 유형의 전달이 꺼져 있습니다. type=$callType")
            return
        }

        val contacts = PhoneUtils.getContactByNumber(phoneNumber)
        val contactName = if (contacts.isNotEmpty()) contacts[0].name else getString(R.string.unknown_number)

        val msg = StringBuilder()
        msg.append(getString(R.string.contact)).append(contactName).append("\n")
        msg.append(getString(R.string.mandatory_type))
        msg.append(CALL_TYPE_MAP[callType.toString()] ?: getString(R.string.unknown_call))

        val msgInfo = MsgInfo("call", phoneNumber.toString(), msg.toString(), Date(), "", -1, 0, callType)
        val request = OneTimeWorkRequestBuilder<SendWorker>().setInputData(
            workDataOf(
                Worker.SEND_MSG_INFO to Gson().toJson(msgInfo)
            )
        ).build()
        WorkManager.getInstance(context).enqueue(request)
    }

    private fun sendCallMsg(context: Context, callType: Int, phoneNumber: String?) {
        Thread.sleep(1000)

        Log.d(TAG, "callType = $callType, phoneNumber = $phoneNumber")
        val callInfo: CallInfo? = PhoneUtils.getLastCallInfo(callType, phoneNumber)
        Log.d(TAG, "callInfo = $callInfo")
        if (callInfo?.number == null) {
            Log.w(TAG, "통화 기록을 찾을 수 없어 알림을 직접 보냅니다")
            sendNotice(context, callType, phoneNumber)
            return
        }

        if ((callInfo.type == 1 && !SettingUtils.enableCallType1) || (callInfo.type == 2 && !SettingUtils.enableCallType2) || (callInfo.type == 3 && !SettingUtils.enableCallType3)) {
            Log.w(TAG, "이 유형의 전달이 꺼져 있습니다. type=" + callInfo.type)
            return
        }

        val simSlot = callInfo.simId
        val simInfo = when (simSlot) {
            0 -> "SIM1_" + SettingUtils.extraSim1
            1 -> "SIM2_" + SettingUtils.extraSim2
            else -> ""
        }

        if (TextUtils.isEmpty(callInfo.name)) {
            val contacts = PhoneUtils.getContactByNumber(phoneNumber)
            callInfo.name = if (contacts.isNotEmpty()) contacts[0].name else getString(R.string.unknown_number)
        }

        val msgInfo = MsgInfo("call", callInfo.number, PhoneUtils.getCallMsg(callInfo), Date(), simInfo, simSlot, callInfo.subId, callType)
        val request = OneTimeWorkRequestBuilder<SendWorker>().setInputData(
            workDataOf(
                Worker.SEND_MSG_INFO to Gson().toJson(msgInfo)
            )
        ).build()
        WorkManager.getInstance(context).enqueue(request)

    }

}