package cn.ppps.forwarder.utils.sender

import com.google.gson.Gson
import java.net.URLEncoder

data class TelegramRequestSpec(val method: String, val url: String, val jsonBody: String? = null)

object TelegramRequestBuilder {
    fun build(method: String, apiToken: String, chatId: String, threadId: String, parseMode: String, content: String): TelegramRequestSpec {
        val endpoint = if (apiToken.startsWith("http")) apiToken else "https://api.telegram.org/bot$apiToken/sendMessage"
        if (method == "GET") {
            val separator = if (endpoint.contains("?")) "&" else "?"
            var url = "$endpoint${separator}chat_id=${URLEncoder.encode(chatId, "UTF-8")}&text=${URLEncoder.encode(content, "UTF-8")}"
            if (parseMode.isNotEmpty() && parseMode != "TEXT") url += "&parse_mode=$parseMode"
            if (threadId.isNotEmpty()) url += "&message_thread_id=$threadId"
            return TelegramRequestSpec(method, url)
        }
        val body = mutableMapOf<String, Any>("chat_id" to chatId, "disable_web_page_preview" to "true")
        if (threadId.isNotEmpty()) body["message_thread_id"] = threadId
        when (parseMode) {
            "MarkdownV2" -> { body["parse_mode"] = "MarkdownV2"; body["text"] = content.replace("-", "\\-") }
            "HTML" -> { body["parse_mode"] = "HTML"; body["text"] = content }
            else -> body["text"] = content
        }
        return TelegramRequestSpec(method, endpoint, Gson().toJson(body))
    }
}
