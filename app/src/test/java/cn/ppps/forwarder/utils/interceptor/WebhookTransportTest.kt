package cn.ppps.forwarder.utils.interceptor

import cn.ppps.forwarder.utils.sender.WebhookBodyTemplate
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.charset.StandardCharsets

class WebhookTransportTest {
    @Test
    fun plainTextWebhookPreservesUtf8BodyAndHeaders() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(204))
        server.start()
        try {
            val body = WebhookBodyTemplate.render("[msg]", mapOf("msg" to "한국어 줄바꿈\n이모지 ✅"), json = false)
            val request = Request.Builder().url(server.url("/ntfy"))
                .header("X-Topic", "sms")
                .post(RequestBody.create(MediaType.parse("text/plain; charset=utf-8"), body))
                .build()
            OkHttpClient().newCall(request).execute().use { assertEquals(204, it.code()) }
            val received = server.takeRequest()
            assertEquals("sms", received.getHeader("X-Topic"))
            assertEquals(body, received.body.readString(StandardCharsets.UTF_8))
            assertEquals("text/plain; charset=utf-8", received.getHeader("Content-Type"))
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun jsonWebhookEscapesLineBreaksButPlainTextDoesNot() {
        val text = "첫 줄\n둘째 줄 \"인용\""
        val template = "{\"text\":\"[msg]\"}"
        val values = mapOf("msg" to text)
        assertEquals("{\"text\":\"첫 줄\\n둘째 줄 \\\"인용\\\"\"}", WebhookBodyTemplate.render(template, values, json = true))
        assertEquals(text, WebhookBodyTemplate.render("[msg]", values, json = false))
    }
}
