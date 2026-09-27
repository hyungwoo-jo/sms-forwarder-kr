package cn.ppps.forwarder.utils

import cn.ppps.forwarder.R
import cn.ppps.forwarder.entity.MsgInfo
import com.xuexiang.xutil.resource.ResUtils.getString
import java.util.*

@Suppress("unused")
object RuleLineUtils {
    const val TAG = "RuleLineUtils"
    private var START_LOG = false

    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val a = """${RuleLine.CONJUNCTION_AND} ${RuleLine.SURE_YES} ${RuleLine.FILED_PHONE_NUM} ${RuleLine.CHECK_EQUALS} 10086
 ${RuleLine.CONJUNCTION_OR} ${RuleLine.SURE_YES} ${RuleLine.FILED_PHONE_NUM} ${RuleLine.CHECK_END_WITH} 예시
  ${RuleLine.CONJUNCTION_AND} ${RuleLine.SURE_YES} ${RuleLine.FILED_MSG_CONTENT} ${RuleLine.CHECK_CONTAIN} test
 ${RuleLine.CONJUNCTION_OR} ${RuleLine.SURE_YES} ${RuleLine.FILED_PHONE_NUM} ${RuleLine.CHECK_END_WITH} pppscn
${RuleLine.CONJUNCTION_AND} ${RuleLine.SURE_YES} ${RuleLine.FILED_PHONE_NUM} ${RuleLine.CHECK_EQUALS} 100861
${RuleLine.CONJUNCTION_AND} ${RuleLine.SURE_YES} ${RuleLine.FILED_PHONE_NUM} ${RuleLine.CHECK_EQUALS} 100861"""
        val msg = MsgInfo("sms", "10086", "예시", Date(), "15888888888")
        logg("check:" + checkRuleLines(msg, a))
    }

    fun startLog(startLog: Boolean) {
        START_LOG = startLog
    }

    private fun logg(msg: String?) {
        if (START_LOG) {
            Log.i(TAG, msg!!)
        }
    }

    @Throws(Exception::class)
    fun checkRuleLines(msg: MsgInfo, ruleLines: String?): Boolean {
        val scanner = Scanner(ruleLines)
        var lineNum = 0
        var headRuleLine: RuleLine? = null
        var beforeRuleLine: RuleLine? = null
        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            logg("$lineNum : $line")
            if (lineNum == 0) {
                if (line.startsWith(" ")) {
                    throw Exception(getString(R.string.no_indentation_allowed_on_the_first_line))
                }
            }

            // process the line
            beforeRuleLine = generateRuleTree(line, lineNum, beforeRuleLine)
            if (lineNum == 0) {
                headRuleLine = beforeRuleLine
            }
            lineNum++
        }
        assert(headRuleLine != null)
        return checkRuleTree(msg, headRuleLine)
    }

    /**
     */
    @Throws(Exception::class)
    fun checkRuleTree(msg: MsgInfo, currentRuleLine: RuleLine?): Boolean {
        var currentAll = currentRuleLine!!.checkMsg(msg)
        logg("current:$currentRuleLine checked:$currentAll")

        if (currentRuleLine.getChildRuleLine() != null) {
            logg(" child:" + currentRuleLine.getChildRuleLine())
            currentAll = when (currentRuleLine.getChildRuleLine()!!.conjunction) {
                RuleLine.CONJUNCTION_AND -> currentAll && checkRuleTree(msg, currentRuleLine.getChildRuleLine())
                RuleLine.CONJUNCTION_OR -> currentAll || checkRuleTree(msg, currentRuleLine.getChildRuleLine())
                else -> throw Exception("child wrong conjunction")
            }
        }

        if (currentRuleLine.getNextRuleLine() != null) {
            logg("next:" + currentRuleLine.getNextRuleLine())
            currentAll = when (currentRuleLine.getNextRuleLine()!!.conjunction) {
                RuleLine.CONJUNCTION_AND -> currentAll && checkRuleTree(msg, currentRuleLine.getNextRuleLine())
                RuleLine.CONJUNCTION_OR -> currentAll || checkRuleTree(msg, currentRuleLine.getNextRuleLine())
                else -> throw Exception("next wrong conjunction")
            }
        }
        return currentAll
    }

    /**
     */
    @Throws(Exception::class)
    fun generateRuleTree(line: String, lineNum: Int, parentRuleLine: RuleLine?): RuleLine {
        //val words = line.split(" ".toRegex()).toTypedArray()
        return RuleLine(line, lineNum, parentRuleLine)
    }
}