package cn.ppps.forwarder.utils.interceptor

import okhttp3.OkHttpClient
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/** Validate before dispatch and on each network exchange. */
class SecureTransportInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        requireAllowed(chain.request().url())
        return chain.proceed(chain.request())
    }
    companion object {
        fun configure(builder: OkHttpClient.Builder): OkHttpClient.Builder = builder
            .followRedirects(false).followSslRedirects(false)

        fun requireAllowed(url: HttpUrl) {
            if (url.host() == "localhost.invalid") {
                throw IOException("전송할 서버의 완전한 HTTPS 주소를 입력하세요.")
            }
            val loopback = url.host() == "localhost" || url.host() == "127.0.0.1" || url.host() == "::1"
            if (!url.isHttps && !(url.scheme() == "http" && loopback)) {
                throw IOException("외부 전송에는 HTTPS가 필요합니다. 서버 주소를 확인하세요.")
            }
        }
    }
}
