package cn.ppps.forwarder.entity.result

data class DingtalkInnerRobotResult(
    var accessToken: String?,
    var expireIn: Long?,
    var processQueryKey: String?,
    //var invalidStaffIdList: String[],
    //var flowControlledStaffIdList: String[],
)