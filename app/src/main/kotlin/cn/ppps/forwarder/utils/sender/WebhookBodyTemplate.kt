package cn.ppps.forwarder.utils.sender

import com.google.gson.Gson

/** Replaces webhook placeholders without JSON escaping a plain text body. */
object WebhookBodyTemplate {
    fun render(template: String, values: Map<String, String>, json: Boolean): String {
        var result = template
        for ((name, value) in values) {
            val replacement = if (json) Gson().toJson(value).removeSurrounding("\"") else value
            result = result.replace("[$name]", replacement)
        }
        return result
    }
}
