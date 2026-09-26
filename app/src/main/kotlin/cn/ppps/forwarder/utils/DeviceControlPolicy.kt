package cn.ppps.forwarder.utils

/** Only explicit local automation actions are supported. */
object DeviceControlPolicy {
    private val actions = setOf(TASK_ACTION_SENDSMS, TASK_ACTION_NOTIFICATION,
        TASK_ACTION_RULE, TASK_ACTION_SENDER, TASK_ACTION_ALARM, TASK_ACTION_RESEND, TASK_ACTION_TASK)
    fun allowsAction(type: Int) = type in actions
    fun allowsCondition(type: Int) = type == TASK_CONDITION_CRON ||
        type == TASK_CONDITION_SMS || type == TASK_CONDITION_CALL || type == TASK_CONDITION_APP
}
