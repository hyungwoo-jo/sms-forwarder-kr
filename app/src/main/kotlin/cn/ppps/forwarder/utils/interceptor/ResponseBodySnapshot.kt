package cn.ppps.forwarder.utils.interceptor

import okhttp3.Response
import okhttp3.ResponseBody

/** Reads a response for diagnostics while returning a response whose body remains readable. */
object ResponseBodySnapshot {
    data class Value(val response: Response, val body: String)

    fun read(response: Response): Value {
        val body = requireNotNull(response.body())
        val text = body.string()
        val readableResponse = response.newBuilder()
            .body(ResponseBody.create(body.contentType(), text))
            .build()
        return Value(readableResponse, text)
    }
}
