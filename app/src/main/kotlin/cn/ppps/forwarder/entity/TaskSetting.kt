package cn.ppps.forwarder.entity

import cn.ppps.forwarder.utils.task.TaskUtils
import java.io.Serializable

data class TaskSetting(
    val type: Int,
    val title: String,
    val description: String,
    var setting: String = "",
    var position: Int = -1
) : Serializable {

    val iconId: Int
        get() = TaskUtils.getTypeImageId(type)

    val greyIconId: Int
        get() = TaskUtils.getTypeGreyImageId(type)
}
