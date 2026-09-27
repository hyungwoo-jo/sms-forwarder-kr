package cn.ppps.forwarder.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.gson.Gson
import cn.ppps.forwarder.core.Core
import cn.ppps.forwarder.entity.MsgInfo
import cn.ppps.forwarder.entity.TaskSetting
import cn.ppps.forwarder.entity.condition.CronSetting
import cn.ppps.forwarder.utils.Log
import cn.ppps.forwarder.utils.TaskWorker
import cn.ppps.forwarder.utils.task.ConditionUtils
import cn.ppps.forwarder.utils.task.CronJobScheduler
import gatewayapps.crondroid.CronExpression
import java.util.Date

@Suppress("PrivatePropertyName", "DEPRECATION")
class CronWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val TAG: String = CronWorker::class.java.simpleName

    override suspend fun doWork(): Result {
        if (!cn.ppps.forwarder.utils.SettingUtils.enableAutomation) return Result.success()
        try {
            val taskId = inputData.getLong(TaskWorker.TASK_ID, -1L)
            if (taskId == -1L) {
                Log.d(TAG, "taskId is -1L")
                return Result.failure()
            }

            val task = Core.task.getOne(taskId)
            if (task == null || task.status == 0) {
                Log.d(TAG, "TASK-$taskId：task is disabled")
                return Result.success()
            }

            val conditionList = Gson().fromJson(task.conditions, Array<TaskSetting>::class.java).toMutableList()
            if (conditionList.isEmpty()) {
                Log.d(TAG, "TASK-${task.id}：conditionList is empty")
                return Result.failure()
            }
            val firstCondition = conditionList.firstOrNull()
            if (firstCondition == null) {
                Log.d(TAG, "TASK-${task.id}：firstCondition is null")
                return Result.failure()
            }
            val cronSetting = Gson().fromJson(firstCondition.setting, CronSetting::class.java)
            if (cronSetting == null) {
                Log.d(TAG, "TASK-${task.id}：cronSetting is null")
                return Result.failure()
            }

            if (!ConditionUtils.checkCondition(task.id, conditionList)) {
                Log.d(TAG, "TASK-${task.id}：other condition is not satisfied")
                return Result.failure()
            }

            val now = Date()
            task.lastExecTime = task.nextExecTime
            val cronExpression = CronExpression(cronSetting.expression)
            val nextExecTime = cronExpression.getNextValidTimeAfter(now)
            nextExecTime.time = nextExecTime.time / 1000 * 1000
            task.nextExecTime = nextExecTime
            Log.d(TAG, "TASK-${task.id}：lastExecTime = ${task.lastExecTime}, nextExecTime = ${task.nextExecTime}")

            if (task.nextExecTime.time / 1000 < now.time / 1000) {
                task.status = 0
            }

            Core.task.updateExecTime(task.id, task.lastExecTime, task.nextExecTime, task.status)

            if (task.status == 0) {
                Log.d(TAG, "TASK-${task.id}：task is disabled")
                return Result.success()
            }

            val msgInfo = MsgInfo("task", task.name, task.description, Date(), task.name)
            val actionData = Data.Builder().putLong(TaskWorker.TASK_ID, task.id).putString(TaskWorker.TASK_ACTIONS, task.actions).putString(TaskWorker.MSG_INFO, Gson().toJson(msgInfo)).build()
            val actionRequest = OneTimeWorkRequestBuilder<ActionWorker>().setInputData(actionData).build()
            WorkManager.getInstance().enqueue(actionRequest)

            CronJobScheduler.cancelTask(task.id)
            CronJobScheduler.scheduleTask(task)
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "doWork error", e)
            return Result.failure()
        }
    }

}
