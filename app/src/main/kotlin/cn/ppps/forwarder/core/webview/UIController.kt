package cn.ppps.forwarder.core.webview

import android.app.Activity
import android.os.Handler
import cn.ppps.forwarder.utils.Log
import android.webkit.WebView
import com.just.agentweb.core.web.AgentWebUIControllerImplBase
import java.lang.ref.WeakReference

/**
 *
 * @author xuexiang
 * @since 2019-10-30 23:18
 */
@Suppress("unused")
class UIController(activity: Activity) : AgentWebUIControllerImplBase() {
    private val mActivity: WeakReference<Activity> = WeakReference(activity)
    override fun onShowMessage(message: String, from: String) {
        super.onShowMessage(message, from)
        Log.i(TAG, "message:$message")
    }

    override fun onSelectItemsPrompt(
        view: WebView,
        url: String,
        items: Array<String>,
        callback: Handler.Callback,
    ) {
        super.onSelectItemsPrompt(view, url, items, callback)
    }

}