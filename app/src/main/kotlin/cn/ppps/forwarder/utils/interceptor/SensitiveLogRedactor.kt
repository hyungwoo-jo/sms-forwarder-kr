package cn.ppps.forwarder.utils.interceptor

/** Removes credentials, message content, and destination URLs from diagnostic logs. */
object SensitiveLogRedactor {
    fun redact(message: String): String = message
        .replace(Regex("""(?i)("(?:token|api[_-]?token|access[_-]?token|app[_-]?secret|secret|password|proxy[_-]?password|authorization|cookie|text|content|message|chat_id)"\s*:\s*")(?:\\.|[^"\\])*"""), "$1***")
        .replace(Regex("(?i)((?:token|api[_-]?token|access[_-]?token|app[_-]?secret|secret|password|proxy[_-]?password|authorization|cookie|text|content|message|chat_id|sign)=)[^&\\s,}]+"), "$1***")
        .replace(Regex("(?i)((?:authorization|proxy-authorization|cookie):\\s*)[^\\r\\n]+"), "$1***")
        .replace(Regex("(?i)https?://[^\\s]+"), "[redacted-url]")
        .replace(Regex("(?i)bot[^/\\s]+/"), "bot***/")
}
