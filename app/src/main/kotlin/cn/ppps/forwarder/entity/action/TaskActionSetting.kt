package cn.ppps.forwarder.entity.action

import cn.ppps.forwarder.database.entity.Task
import java.io.Serializable

data class TaskActionSetting(
    var description: String = "",
    var status: Int = 1,
    var taskList: List<Task>,
) : Serializable
