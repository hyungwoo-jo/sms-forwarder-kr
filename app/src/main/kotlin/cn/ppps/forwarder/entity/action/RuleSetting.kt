package cn.ppps.forwarder.entity.action

import cn.ppps.forwarder.database.entity.Rule
import java.io.Serializable

data class RuleSetting(
    var description: String = "",
    var status: Int = 1,
    var ruleList: List<Rule>,
) : Serializable
