package cn.ppps.forwarder.core.webview

import android.net.Uri
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.annotation.RequiresApi
import cn.ppps.forwarder.R
import cn.ppps.forwarder.core.webview.WebViewInterceptDialog.Companion.show
import cn.ppps.forwarder.utils.Log
import com.just.agentweb.core.client.MiddlewareWebClientBase
import com.xuexiang.xutil.resource.ResUtils.getStringArray
import java.util.Locale

/**
 *
 *
 *
 *
 *
 *
 * .useMiddlewareWebClient(getMiddlewareWebClient())  // 1
 * .useMiddlewareWebClient(getMiddlewareWebClient())  // 2
 * .useMiddlewareWebClient(getMiddlewareWebClient())  // 3
 * .useMiddlewareWebClient(getMiddlewareWebClient())  // 4
 * .useMiddlewareWebClient(getMiddlewareWebClient())  // 5
 * .useMiddlewareWebClient(getMiddlewareWebClient())  // 6
 * .useMiddlewareWebClient(getMiddlewareWebClient())  // 7
 * DefaultWebClient                                  // 8
 * .setWebViewClient(mWebViewClient)                 // 9
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
@Suppress("UNUSED_PARAMETER", "DEPRECATION", "OVERRIDE_DEPRECATION")
open class MiddlewareWebViewClient : MiddlewareWebClientBase() {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        Log.i(
            "Info",
            "MiddlewareWebViewClient -- >  shouldOverrideUrlLoading:" + request.url.toString() + "  c:" + count++
        )
        return if (shouldOverrideUrlLoadingByApp(view, request.url.toString())) {
            true
        } else super.shouldOverrideUrlLoading(view, request)
    }

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        Log.i(
            "Info",
            "MiddlewareWebViewClient -- >  shouldOverrideUrlLoading:" + url + "  c:" + count++
        )
        return if (shouldOverrideUrlLoadingByApp(view, url)) {
            true
        } else super.shouldOverrideUrlLoading(view, url)
    }

    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
        val tUrl = url.lowercase(Locale.ROOT)
        return if (!hasAdUrl(tUrl)) {
            super.shouldInterceptRequest(view, tUrl)
        } else {
            WebResourceResponse(null, null, null)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest,
    ): WebResourceResponse? {
        val url = request.url.toString().lowercase(Locale.ROOT)
        return if (!hasAdUrl(url)) {
            super.shouldInterceptRequest(view, request)
        } else {
            WebResourceResponse(null, null, null)
        }
    }

    /**
     */
    private fun shouldOverrideUrlLoadingByApp(webView: WebView, url: String): Boolean {
        if (url.startsWith("http") || url.startsWith("https") || url.startsWith("ftp")) {
            val uri = Uri.parse(url)
            if (uri != null && !(WebViewInterceptDialog.APP_LINK_HOST == uri.host && url.contains("xpage"))) {
                return false
            }
        }
        show(url)
        return true
    }

    companion object {
        private var count = 1

        /**
         *
         * @param url
         * @return
         */
        private fun hasAdUrl(url: String): Boolean {
            val adUrls = getStringArray(R.array.adBlockUrl)
            for (adUrl in adUrls) {
                if (url.contains(adUrl)) {
                    return true
                }
            }
            return false
        }
    }
}