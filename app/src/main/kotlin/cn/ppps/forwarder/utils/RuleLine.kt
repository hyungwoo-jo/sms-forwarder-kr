package cn.ppps.forwarder.utils

import cn.ppps.forwarder.R
import cn.ppps.forwarder.entity.MsgInfo
import com.xuexiang.xutil.resource.ResUtils.getString
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

@Suppress("unused")
class RuleLine(line: String, lineNum: Int, beforeRuleLine: RuleLine?) {
    companion object {
        val CONJUNCTION_AND: String = getString(R.string.CONJUNCTION_AND)
        val CONJUNCTION_OR: String = getString(R.string.CONJUNCTION_OR)
        val FILED_PHONE_NUM: String = getString(R.string.FILED_PHONE_NUM)
        val FILED_MSG_CONTENT: String = getString(R.string.FILED_MSG_CONTENT)
        val FILED_PACKAGE_NAME: String = getString(R.string.FILED_PACKAGE_NAME)
        val FILED_UID: String = getString(R.string.FILED_UID)
        val FILED_INFORM_TITLE: String = getString(R.string.FILED_INFORM_TITLE)
        val FILED_INFORM_CONTENT: String = getString(R.string.FILED_INFORM_CONTENT)
        val FILED_SIM_SLOT_INFO: String = getString(R.string.FILED_SIM_SLOT_INFO)
        val FILED_CALL_TYPE: String = getString(R.string.FILED_CALL_TYPE)
        val SURE_YES: String = getString(R.string.SURE_YES)
        val SURE_NOT: String = getString(R.string.SURE_NOT)
        val CHECK_EQUALS: String = getString(R.string.CHECK_EQUALS)
        val CHECK_CONTAIN: String = getString(R.string.CHECK_CONTAIN)
        val CHECK_NOT_CONTAIN: String = getString(R.string.CHECK_NOT_CONTAIN)
        val CHECK_START_WITH: String = getString(R.string.CHECK_START_WITH)
        val CHECK_END_WITH: String = getString(R.string.CHECK_END_WITH)
        val CHECK_REGEX: String = getString(R.string.CHECK_REGEX)
        val CONJUNCTION_LIST: MutableList<String> = ArrayList()
        val FILED_LIST: MutableList<String> = ArrayList()
        val SURE_LIST: MutableList<String> = ArrayList()
        val CHECK_LIST: MutableList<String> = ArrayList()

        const val TAG = "RuleLine"
        private var START_LOG = true
        fun startLog(startLog: Boolean) {
            START_LOG = startLog
        }

        fun logg(msg: String?) {
            if (START_LOG) {
                Log.i(TAG, msg!!)
            }
        }

        init {
            CONJUNCTION_LIST.add("and")
            CONJUNCTION_LIST.add("or")
            CONJUNCTION_LIST.add(CONJUNCTION_AND)
            CONJUNCTION_LIST.add(CONJUNCTION_OR)
        }

        init {
            FILED_LIST.add(FILED_PHONE_NUM)
            FILED_LIST.add(FILED_PACKAGE_NAME)
            FILED_LIST.add(FILED_MSG_CONTENT)
            FILED_LIST.add(FILED_INFORM_CONTENT)
            FILED_LIST.add(FILED_INFORM_TITLE)
            FILED_LIST.add(FILED_SIM_SLOT_INFO)
            FILED_LIST.add(FILED_CALL_TYPE)
            FILED_LIST.add(FILED_UID)
        }

        init {
            SURE_LIST.add(SURE_YES)
            SURE_LIST.add(SURE_NOT)
        }

        init {
            CHECK_LIST.add(CHECK_EQUALS)
            CHECK_LIST.add(CHECK_CONTAIN)
            CHECK_LIST.add(CHECK_NOT_CONTAIN)
            CHECK_LIST.add(CHECK_START_WITH)
            CHECK_LIST.add(CHECK_END_WITH)
            CHECK_LIST.add(CHECK_REGEX)
        }
    }

    private var headSpaceNum = 0
    private var beforeRuleLine: RuleLine? = null
    private var nextRuleLine: RuleLine? = null
    private var parentRuleLine: RuleLine? = null
    private var childRuleLine: RuleLine? = null

    //and or
    var conjunction: String

    private var field: String

    private var sure: String
    private var check: String
    private var value: String

    fun checkMsg(msg: MsgInfo): Boolean {

        var mixChecked = false
        when (field) {
            FILED_PHONE_NUM, FILED_PACKAGE_NAME -> mixChecked = checkValue(msg.from)
            FILED_UID -> mixChecked = checkValue(msg.uid.toString())
            FILED_CALL_TYPE -> mixChecked = checkValue(msg.callType.toString())
            FILED_MSG_CONTENT, FILED_INFORM_CONTENT -> mixChecked = checkValue(msg.content)
            FILED_INFORM_TITLE, FILED_SIM_SLOT_INFO -> mixChecked = checkValue(msg.simInfo)
            else -> {}
        }
        when (sure) {
            SURE_YES -> {}
            SURE_NOT -> mixChecked = !mixChecked
            else -> mixChecked = false
        }
        logg("rule:$this checkMsg:$msg checked:$mixChecked")
        return mixChecked
    }

    private fun checkValue(msgValue: String?): Boolean {
        if (msgValue == null) return false

        fun evaluateCondition(condition: String): Boolean {
            return when (check) {
                CHECK_EQUALS -> msgValue == condition
                CHECK_CONTAIN -> msgValue.contains(condition)
                CHECK_NOT_CONTAIN -> !msgValue.contains(condition)
                CHECK_START_WITH -> msgValue.startsWith(condition)
                CHECK_END_WITH -> msgValue.endsWith(condition)
                CHECK_REGEX -> try {
                    val pattern = Pattern.compile(condition, Pattern.CASE_INSENSITIVE)
                    val matcher = pattern.matcher(msgValue)
                    matcher.find()
                } catch (e: PatternSyntaxException) {
                    logg("PatternSyntaxException: ${e.description}, Index: ${e.index}, Message: ${e.message}, Pattern: ${e.pattern}")
                    false
                }

                else -> false
            }
        }

        fun parseAndEvaluate(expression: String): Boolean {
            // Split by "||" and evaluate each segment joined by "&&"
            val orGroups = expression.split("||")
            return orGroups.any { orGroup ->
                val andGroups = orGroup.split("&&")
                andGroups.all { andGroup ->
                    val trimmedCondition = andGroup.trim()
                    evaluateCondition(trimmedCondition)
                }
            }
        }

        val checked = if (value.contains("&&") || value.contains("||")) {
            parseAndEvaluate(value)
        } else {
            evaluateCondition(value)
        }

        logg("checkValue $msgValue $check $value checked:$checked")
        return checked
    }

    override fun toString(): String {
        return "RuleLine{" +
                "headSpaceNum='" + headSpaceNum + '\'' +
                "conjunction='" + conjunction + '\'' +
                ", field='" + field + '\'' +
                ", sure='" + sure + '\'' +
                ", check='" + check + '\'' +
                ", value='" + value + '\'' +
                '}'
    }

    fun getNextRuleLine(): RuleLine? {
        return nextRuleLine
    }

    fun setNextRuleLine(nextRuleLine: RuleLine?) {
        this.nextRuleLine = nextRuleLine
    }

    fun getChildRuleLine(): RuleLine? {
        return childRuleLine
    }

    fun setChildRuleLine(childRuleLine: RuleLine?) {
        this.childRuleLine = childRuleLine
    }

    init {
        logg("----------$lineNum-----------------")
        logg(line)


        var isCountHeading = false
        var isDealMiddle = false
        var isDealValue = false

        val middleList: MutableList<String> = ArrayList(4)
        var buildMiddleWord = StringBuilder()
        val valueBuilder = StringBuilder()
        for (i in line.indices) {
            val w = line[i].toString()
            logg("walk over:$w")

            if (i == 0) {
                if (" " == w) {
                    logg("start to isCountHeading:")
                    isCountHeading = true
                } else {
                    isDealMiddle = true
                    logg("start to isDealMiddle:")
                }
            }
            if (isCountHeading && " " != w) {
                logg("isCountHeading to isDealMiddle:")
                isCountHeading = false
                isDealMiddle = true
            }

            if (isDealMiddle && middleList.size == 4) {
                logg("isDealMiddle done middleList:$middleList")
                logg("isDealMiddle to isDealValue:")
                isDealMiddle = false
                isDealValue = true
            }
            logg("isCountHeading:$isCountHeading")
            logg("isDealMiddle:$isDealMiddle")
            logg("isDealValue:$isDealValue")
            if (isCountHeading) {
                logg("headSpaceNum++:$headSpaceNum")
                headSpaceNum++
            }
            if (isDealMiddle) {
                if (" " == w) {
                    buildMiddleWord = if (buildMiddleWord.isEmpty()) {
                        throw Exception(lineNum.toString() + "행: 연속된 공백은 사용할 수 없습니다.")
                    } else {
                        middleList.add(buildMiddleWord.toString())
                        logg("get Middle++:$buildMiddleWord")
                        StringBuilder()
                    }
                } else {
                    buildMiddleWord.append(w)
                    logg("buildMiddleWord length:" + buildMiddleWord.length + "buildMiddleWord:" + buildMiddleWord)
                }
            }
            if (isDealValue) {
                valueBuilder.append(w)
            }
        }
        logg("isDealValue done valueBuilder:$valueBuilder")
        if (middleList.size != 4) {
            throw Exception(lineNum.toString() + "행: 규칙은 연결어, 확인어, 필드, 비교 연산자로 구성해야 합니다.")
        }


        if (beforeRuleLine != null) {
            logg("beforeRuleLine :$beforeRuleLine")
            logg("thisRuleLine :$this")

            if (headSpaceNum == beforeRuleLine.headSpaceNum) {
                logg("같은 수준")
                this.beforeRuleLine = beforeRuleLine
                beforeRuleLine.nextRuleLine = this
            }
            if (headSpaceNum - 1 == beforeRuleLine.headSpaceNum) {
                logg("하위 수준")
                parentRuleLine = beforeRuleLine
                beforeRuleLine.childRuleLine = this
            }
            if (headSpaceNum < beforeRuleLine.headSpaceNum) {
                var fBeforeRuleLine = beforeRuleLine.beforeRuleLine
                if (fBeforeRuleLine == null) {
                    fBeforeRuleLine = beforeRuleLine.parentRuleLine
                }
                while (fBeforeRuleLine != null) {
                    logg("fBeforeRuleLine$fBeforeRuleLine")

                    if (headSpaceNum == fBeforeRuleLine.headSpaceNum) {
                        logg("상위 수준")
                        this.beforeRuleLine = fBeforeRuleLine
                        fBeforeRuleLine.nextRuleLine = this
                        break
                    } else {
                        var pBeforeRuleLine = fBeforeRuleLine.beforeRuleLine
                        if (pBeforeRuleLine == null) {
                            pBeforeRuleLine = fBeforeRuleLine.parentRuleLine
                        }
                        fBeforeRuleLine = pBeforeRuleLine
                    }
                }
            }
        } else {
            logg("최상위 수준")
        }
        conjunction = middleList[0]
        sure = middleList[1]
        field = middleList[2]
        check = middleList[3]
        value = valueBuilder.toString()
        if (!CONJUNCTION_LIST.contains(conjunction)) {
            throw Exception(lineNum.toString() + "행: 허용되는 연결어: " + CONJUNCTION_LIST + " / 입력값: " + conjunction)
        }
        if (!FILED_LIST.contains(field)) {
            throw Exception(lineNum.toString() + "행: 허용되는 필드: " + FILED_LIST + " / 입력값: " + field)
        }
        if (!SURE_LIST.contains(sure)) {
            throw Exception(lineNum.toString() + "행: 규칙 오류 " + sure + " / 허용되는 확인어: " + SURE_LIST + " / 입력값: " + sure)
        }
        if (!CHECK_LIST.contains(check)) {
            throw Exception(lineNum.toString() + "행: 허용되는 비교 연산자: " + CHECK_LIST + " / 입력값: " + check)
        }
        logg("----------$lineNum==$this")
    }
}