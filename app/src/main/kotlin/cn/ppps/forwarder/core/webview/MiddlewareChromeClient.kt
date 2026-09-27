package cn.ppps.forwarder.core.webview

import android.webkit.JsResult
import android.webkit.WebView
import cn.ppps.forwarder.utils.Log
import com.just.agentweb.core.client.MiddlewareWebChromeBase

/**
 * @author xuexiang
 */
open class MiddlewareChromeClient : MiddlewareWebChromeBase() {
    override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
        Log.i("Info", "onJsAlert:$url")
        return super.onJsAlert(view, url, message, result)
    }

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        Log.i("Info", "onProgressChanged:")
    }
}