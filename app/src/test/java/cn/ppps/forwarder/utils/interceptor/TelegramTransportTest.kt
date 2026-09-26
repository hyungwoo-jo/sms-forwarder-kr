package cn.ppps.forwarder.utils.interceptor

import cn.ppps.forwarder.utils.sender.TelegramRequestBuilder
import com.google.gson.JsonParser
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class TelegramTransportTest {
    @Test
    fun allMethodsAndModesReachOnlyLocalServerWithKoreanText() {
        val server = MockWebServer()
        server.start()
        try {
            val content = "제목\n한국어 & 이모지 ✅"
            for (method in listOf("GET", "POST")) for (mode in listOf("TEXT", "HTML", "MarkdownV2")) {
                server.enqueue(MockResponse().setBody("{\"ok\":true}"))
                val spec = TelegramRequestBuilder.build(method, server.url("/sendMessage").toString(), "42", "7", mode, content)
                val builder = Request.Builder().url(spec.url)
                if (method == "POST") builder.post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), spec.jsonBody!!))
                OkHttpClient().newCall(builder.build()).execute().use { assertTrue(it.isSuccessful) }
                val received = server.takeRequest()
                assertEquals(method, received.method)
                if (method == "GET") {
                    assertEquals(content, received.requestUrl!!.queryParameter("text"))
                    assertEquals(if (mode == "TEXT") null else mode, received.requestUrl!!.queryParameter("parse_mode"))
                } else {
                    val json = JsonParser().parse(received.body.readUtf8()).asJsonObject
                    assertEquals(content, json.get("text").asString)
                    assertEquals("42", json.get("chat_id").asString)
                    assertEquals("7", json.get("message_thread_id").asString)
                    assertEquals(mode != "TEXT", json.has("parse_mode"))
                }
            }
        } finally { server.shutdown() }
    }

    @Test
    fun unauthorizedRateLimitAndDisconnectedSocketAreFailures() {
        val server = MockWebServer()
        server.start()
        try {
            for (code in listOf(401, 429)) {
                server.enqueue(MockResponse().setResponseCode(code).setBody("{\"ok\":false}"))
                OkHttpClient().newCall(Request.Builder().url(server.url("/")).build()).execute().use {
                    assertEquals(code, it.code())
                    assertFalse(it.isSuccessful)
                }
            }
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
            try {
                OkHttpClient.Builder().retryOnConnectionFailure(false).build()
                    .newCall(Request.Builder().url(server.url("/")).build()).execute().close()
                fail("Disconnected socket unexpectedly succeeded")
            } catch (expected: IOException) { }
        } finally { server.shutdown() }
    }
}
