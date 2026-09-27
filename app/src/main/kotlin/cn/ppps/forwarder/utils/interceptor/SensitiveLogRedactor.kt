package cn.ppps.forwarder.utils.interceptor

/** Removes credentials, message content, and destination URLs from diagnostic logs. */
object SensitiveLogRedactor {
    private val han = Regex("[\\u3400-\\u9fff]")

    fun redact(message: String): String = message
        .replace(Regex("""(?i)("(?:token|api[_-]?token|access[_-]?token|app[_-]?secret|secret|password|proxy[_-]?password|authorization|cookie|text|content|message|chat_id)"\s*:\s*")(?:\\.|[^"\\])*"""), "$1***")
        .replace(Regex("(?i)((?:token|api[_-]?token|access[_-]?token|app[_-]?secret|secret|password|proxy[_-]?password|authorization|cookie|text|content|message|chat_id|sign)=)[^&\\s,}]+"), "$1***")
        .replace(Regex("(?i)((?:authorization|proxy-authorization|cookie):\\s*)[^\\r\\n]+"), "$1***")
        .replace(Regex("(?i)https?://[^\\s]+"), "[redacted-url]")
        .replace(Regex("(?i)bot[^/\\s]+/"), "bot***/")

    /** Avoid showing a third-party library or remote-service Chinese error verbatim. */
    fun redactForDisplay(message: String): String {
        val redacted = redact(message)
        return if (han.containsMatchIn(redacted)) {
            "외부 서비스 응답을 표시할 수 없습니다. 전송 상태와 서버 설정을 확인하세요."
        } else {
            redacted
        }
    }
}
