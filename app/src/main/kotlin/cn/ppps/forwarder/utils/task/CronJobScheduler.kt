package cn.ppps.forwarder.utils.task

import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import cn.ppps.forwarder.database.entity.Task
import cn.ppps.forwarder.utils.Log
import cn.ppps.forwarder.utils.TaskWorker
import cn.ppps.forwarder.workers.CronWorker
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
class CronJobScheduler {

    companion object {

        private val TAG: String = CronJobScheduler::class.java.simpleName

        fun scheduleTask(task: Task) {
            if (!cn.ppps.forwarder.utils.SettingUtils.enableAutomation) return
            val currentTimeMillis = System.currentTimeMillis()
            val delayInMillis = task.nextExecTime.time / 1000 * 1000 - currentTimeMillis
            val inputData = Data.Builder().putLong(TaskWorker.TASK_ID, task.id).build()
            val taskRequest = if (delayInMillis <= 0L) {
                Log.d(TAG, "TASK-${task.id}: 즉시 실행, delayInMillis = $delayInMillis")
                OneTimeWorkRequestBuilder<CronWorker>()
                    .setInputData(inputData)
                    .build()
            } else {
                Log.d(TAG, "TASK-${task.id}: ${delayInMillis}ms 후 실행")
                OneTimeWorkRequestBuilder<CronWorker>()
                    .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .build()
            }

            val uniqueTaskName = "$TAG-${task.id}"
            WorkManager.getInstance().beginUniqueWork(
                uniqueTaskName,
                ExistingWorkPolicy.KEEP,
                taskRequest
            ).enqueue()
        }

        fun cancelTask(taskId: Long) {
            val uniqueTaskName = "$TAG-$taskId"
            WorkManager.getInstance().cancelUniqueWork(uniqueTaskName)
        }
    }
}
