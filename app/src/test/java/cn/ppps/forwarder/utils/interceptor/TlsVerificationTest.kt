package cn.ppps.forwarder.utils.interceptor

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class TlsVerificationTest {
    @Test
    fun trustedCertificateSucceedsAndUntrustedCertificateFails() {
        // This publicly known password protects a test fixture, not a release key.
        val password = "changeit".toCharArray()
        val store = KeyStore.getInstance("PKCS12")
        javaClass.getResourceAsStream("/mock-tls.p12")!!.use { store.load(it, password) }
        val keys = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        keys.init(store, password)
        val trusts = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trusts.init(store)
        val trustManager = trusts.trustManagers.filterIsInstance<X509TrustManager>().single()
        val context = SSLContext.getInstance("TLS")
        context.init(keys.keyManagers, trusts.trustManagers, null)
        val server = MockWebServer()
        server.useHttps(context.socketFactory, false)
        server.enqueue(MockResponse().setBody("ok"))
        server.start()
        try {
            val request = Request.Builder().url(server.url("/")).build()
            val trusted = OkHttpClient.Builder().sslSocketFactory(context.socketFactory, trustManager).build()
            trusted.newCall(request).execute().use { assertEquals("ok", it.body()!!.string()) }
            try {
                OkHttpClient().newCall(request).execute().close()
                fail("An untrusted certificate was accepted")
            } catch (expected: SSLHandshakeException) {
                // Default certificate validation rejected the fixture as expected.
            }
        } finally {
            server.shutdown()
        }
    }
}
