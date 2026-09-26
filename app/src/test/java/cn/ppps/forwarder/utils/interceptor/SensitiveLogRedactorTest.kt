package cn.ppps.forwarder.utils.interceptor

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import cn.ppps.forwarder.utils.sender.TelegramRequestBuilder
import cn.ppps.forwarder.utils.PhoneAreaLookupPolicy

class SensitiveLogRedactorTest {
    @Test
    fun redactsEscapedQuotesAndStandaloneTokens() {
        val json = """{"text":"앞 \"민감\" 뒤","token":"secret-token"}"""
        val redacted = SensitiveLogRedactor.redact(json)
        assertFalse(redacted.contains("민감"))
        assertFalse(redacted.contains("뒤"))
        assertFalse(redacted.contains("secret-token"))
        assertEquals("token=***", SensitiveLogRedactor.redact("token=standalone-secret"))
    }

    @Test
    fun redactsDestinationCredentialsAndMessageContent() {
        val secretValues = listOf(
            "123456:telegram-token",
            "query-token",
            "Bearer proxy-secret",
            "custom-message",
            "proxy-password",
            "https://user:password@example.test/hook?token=query-token"
        )
        val log = """
            POST https://api.telegram.org/bot123456:telegram-token/sendMessage?token=query-token
            Authorization: Bearer proxy-secret
            Proxy-Authorization: Basic proxy-password
            {"text":"custom-message","chat_id":"12345"}
            https://user:password@example.test/hook?token=query-token
        """.trimIndent()

        val redacted = SensitiveLogRedactor.redact(log)

        secretValues.forEach { assertFalse("leaked: $it", redacted.contains(it)) }
        assertTrue(redacted.contains("[redacted-url]"))
        assertTrue(redacted.contains("Authorization: ***"))
    }

    @Test
    fun readingForLogsLeavesResponseBodyAvailableToSender() {
        val original = Response.Builder()
            .request(Request.Builder().url("https://example.test/").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(ResponseBody.create(null, "응답 본문 ✅"))
            .build()

        val snapshot = ResponseBodySnapshot.read(original)

        assertEquals("응답 본문 ✅", snapshot.body)
        assertEquals("응답 본문 ✅", snapshot.response.body()!!.string())
    }

    @Test
    fun telegramRequestBuilderKeepsKoreanEmojiAndSelectsParseMode() {
        val get = TelegramRequestBuilder.build("GET", "https://example.test/send", "42", "", "TEXT", "안녕 ✅")
        val markdown = TelegramRequestBuilder.build("POST", "token", "42", "7", "MarkdownV2", "제목-본문 ✅")
        assertTrue(get.url.contains("%EC%95%88%EB%85%95+%E2%9C%85"))
        assertTrue(markdown.jsonBody!!.contains("\\\\-"))
        assertTrue(markdown.jsonBody!!.contains("\"message_thread_id\":\"7\""))
    }

    @Test
    fun disabledPhoneAreaLookupDoesNotInvokeNetworkLookup() {
        var calls = 0
        val result = PhoneAreaLookupPolicy.resolve(false, "알 수 없는 지역") {
            calls += 1
            "network result"
        }
        assertEquals("알 수 없는 지역", result)
        assertEquals(0, calls)
    }
}
