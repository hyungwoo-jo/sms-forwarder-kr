package cn.ppps.forwarder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import cn.ppps.forwarder.R
import cn.ppps.forwarder.utils.DELAY_TIME_AFTER_SIM_READY
import cn.ppps.forwarder.utils.Log
import cn.ppps.forwarder.utils.SettingUtils
import cn.ppps.forwarder.utils.TASK_CONDITION_SIM
import cn.ppps.forwarder.utils.TaskWorker
import cn.ppps.forwarder.utils.task.TaskUtils
import cn.ppps.forwarder.workers.SimWorker
import com.xuexiang.xutil.resource.ResUtils.getString
import java.util.concurrent.TimeUnit

@Suppress("PrivatePropertyName")
class SimStateReceiver : BroadcastReceiver() {

    private var TAG = SimStateReceiver::class.java.simpleName

    override fun onReceive(context: Context, intent: Intent) {

        if (SettingUtils.enablePureClientMode) return

        if (intent.action != "android.intent.action.SIM_STATE_CHANGED") return

        val simStateOld = TaskUtils.simState
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val simStateNew = telephonyManager.simState

        if (simStateOld == simStateNew) return

        var duration = 10L
        val msg = when (simStateNew) {
            TelephonyManager.SIM_STATE_ABSENT -> {
                Log.d(TAG, "SIM 카드가 제거되었습니다")
                TaskUtils.simState = simStateNew
                getString(R.string.sim_state_absent)
            }

            TelephonyManager.SIM_STATE_READY -> {
                Log.d(TAG, "SIM 카드를 사용할 수 있습니다")
                TaskUtils.simState = simStateNew
                duration = DELAY_TIME_AFTER_SIM_READY
                getString(R.string.sim_state_ready)
            }

            else -> {
                Log.d(TAG, "SIM 카드 상태를 알 수 없습니다")
                TaskUtils.simState = 0
                getString(R.string.sim_state_unknown)
            }

        }

        val request = OneTimeWorkRequestBuilder<SimWorker>()
            .setInitialDelay(duration, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    TaskWorker.CONDITION_TYPE to TASK_CONDITION_SIM,
                    TaskWorker.MSG to msg.toString().trimEnd(),
                )
            ).build()
        WorkManager.getInstance(context).enqueue(request)
    }

}