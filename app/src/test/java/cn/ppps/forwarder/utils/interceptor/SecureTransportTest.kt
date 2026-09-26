package cn.ppps.forwarder.utils.interceptor

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class SecureTransportTest {
    private fun client() = SecureTransportInterceptor.configure(OkHttpClient.Builder())
        .addInterceptor(SecureTransportInterceptor()).addNetworkInterceptor(SecureTransportInterceptor()).build()

    @Test fun externalPlaintextAndMissingEndpointAreRejectedBeforeNetwork() {
        for (url in listOf("http://example.com/private", "https://localhost.invalid/private")) {
            try {
                client().newCall(Request.Builder().url(url).build()).execute().close()
                fail("Unsafe endpoint was dispatched")
            } catch (expected: IOException) {
                assertTrue(expected.message!!.contains("HTTPS"))
            }
        }
    }

    @Test fun loopbackWorksButRedirectDoesNotForwardSecrets() {
        val origin = MockWebServer()
        val destination = MockWebServer()
        origin.start(); destination.start()
        try {
            origin.enqueue(MockResponse().setResponseCode(302).setHeader("Location", destination.url("/leak")))
            client().newCall(Request.Builder().url(origin.url("/hook"))
                .header("Authorization", "test-secret").build()).execute().use {
                assertEquals(302, it.code())
            }
            assertEquals("test-secret", origin.takeRequest().getHeader("Authorization"))
            assertEquals(0, destination.requestCount)
        } finally { origin.shutdown(); destination.shutdown() }
    }
}
