package cn.ppps.forwarder.core.webview

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import cn.ppps.forwarder.App
import cn.ppps.forwarder.R
import cn.ppps.forwarder.utils.Log
import cn.ppps.forwarder.utils.XToastUtils
import com.just.agentweb.action.PermissionInterceptor
import com.just.agentweb.core.AgentWeb
import com.just.agentweb.core.client.MiddlewareWebChromeBase
import com.just.agentweb.core.client.MiddlewareWebClientBase
import com.just.agentweb.core.client.WebListenerManager
import com.just.agentweb.core.web.AbsAgentWebSettings
import com.just.agentweb.core.web.AgentWebConfig
import com.just.agentweb.core.web.IAgentWebSettings
import com.just.agentweb.download.AgentWebDownloader.Extra
import com.just.agentweb.download.DefaultDownloadImpl
import com.just.agentweb.download.DownloadListenerAdapter
import com.just.agentweb.download.DownloadingService
import com.just.agentweb.utils.LogUtils
import com.just.agentweb.widget.IWebLayout
import com.xuexiang.xutil.net.JsonUtil

/**
 *
 * @author xuexiang
 */
@Suppress(
    "unused",
    "ProtectedInFinal",
    "NAME_SHADOWING",
    "UNUSED_PARAMETER",
    "OVERRIDE_DEPRECATION"
)
class AgentWebFragment : Fragment(), FragmentKeyDown {
    private var mBackImageView: ImageView? = null
    private var mLineView: View? = null
    private var mFinishImageView: ImageView? = null
    private var mTitleTextView: TextView? = null
    private var mAgentWeb: AgentWeb? = null
    private var mMoreImageView: ImageView? = null
    private var mPopupMenu: PopupMenu? = null
    private var mDownloadingService: DownloadingService? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_agentweb, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAgentWeb = AgentWeb.with(this)
            .setAgentWebParent(
                (view as LinearLayout),
                -1,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            .useDefaultIndicator(-1, 3)
            .setAgentWebWebSettings(settings)
            .setWebViewClient(mWebViewClient) //WebChromeClient
            .setWebChromeClient(mWebChromeClient)
            .useMiddlewareWebChrome(middlewareWebChrome)
            .useMiddlewareWebClient(middlewareWebClient)
            .setPermissionInterceptor(mPermissionInterceptor)
            .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK)
            .setAgentWebUIController(UIController(requireActivity()))
            .setMainFrameErrorView(R.layout.agentweb_error_page, -1)
            .setWebLayout(webLayout)
            .interceptUnkownUrl()
            .createAgentWeb()
            .ready()
            .go(url)
        if (App.isDebug) {
            AgentWebConfig.debug()
        }

        addBackgroundChild(mAgentWeb!!.webCreator.webParentLayout)
        initView(view)

        mAgentWeb!!.webCreator.webView.overScrollMode = WebView.OVER_SCROLL_NEVER
    }

    protected val webLayout: IWebLayout<*, *>
        get() = WebLayout(activity)

    protected fun initView(view: View) {
        mBackImageView = view.findViewById(R.id.iv_back)
        mLineView = view.findViewById(R.id.view_line)
        mFinishImageView = view.findViewById(R.id.iv_finish)
        mTitleTextView = view.findViewById(R.id.toolbar_title)
        mBackImageView?.setOnClickListener(mOnClickListener)
        mFinishImageView?.setOnClickListener(mOnClickListener)
        mMoreImageView = view.findViewById(R.id.iv_more)
        mMoreImageView?.setOnClickListener(mOnClickListener)
        pageNavigator(View.GONE)
    }

    protected fun addBackgroundChild(frameLayout: FrameLayout) {
        val textView = TextView(frameLayout.context)
        textView.text = getString(R.string.provided_by_agentweb)
        textView.textSize = 16f
        textView.setTextColor(Color.parseColor("#727779"))
        frameLayout.setBackgroundColor(Color.parseColor("#272b2d"))
        val params = FrameLayout.LayoutParams(-2, -2)
        params.gravity = Gravity.CENTER_HORIZONTAL
        val scale = frameLayout.context.resources.displayMetrics.density
        params.topMargin = (15 * scale + 0.5f).toInt()
        frameLayout.addView(textView, 0, params)
    }

    private fun pageNavigator(tag: Int) {
        mBackImageView!!.visibility = tag
        mLineView!!.visibility = tag
    }

    private val mOnClickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.iv_back ->
                if (!mAgentWeb!!.back()) {
                    this.requireActivity().finish()
                }

            R.id.iv_finish -> this.requireActivity().finish()
            R.id.iv_more -> showPoPup(v)
            else -> {}
        }
    }
    //========================================//
    /**
     */
    protected var mPermissionInterceptor = PermissionInterceptor { url, permissions, action ->

        /**
         * @param url
         * @param permissions
         * @param action
         */
        /**
         * @param url
         * @param permissions
         * @param action
         */
        Log.i(
            TAG,
            "mUrl:" + url + "  permission:" + JsonUtil.toJson(permissions) + " action:" + action
        )
        false
    }
    /**
     */
    protected var mDownloadListenerAdapter: DownloadListenerAdapter =
        object : DownloadListenerAdapter() {
            /**
             *
             * @param userAgent          UserAgent
             * @param contentDisposition ContentDisposition
             */
            override fun onStart(
                url: String,
                userAgent: String,
                contentDisposition: String,
                mimetype: String,
                contentLength: Long,
                extra: Extra,
            ): Boolean {
                LogUtils.i(TAG, "onStart:$url")
                extra.setOpenBreakPointDownload(true)
                    .setIcon(R.drawable.ic_file_download_black_24dp)
                    .setConnectTimeOut(6000)
                    .setBlockMaxTime(10 * 60 * 1000)
                    .setDownloadTimeOut(Long.MAX_VALUE)
                    .setParallelDownload(false)
                    .setEnableIndicator(true)
                    .addHeader("Cookie", "xx")
                    .setAutoOpen(true).isForceDownload = true
                return false
            }

            /**
             *
             * @param url
             */
            override fun onBindService(url: String, downloadingService: DownloadingService) {
                super.onBindService(url, downloadingService)
                mDownloadingService = downloadingService
                LogUtils.i(TAG, "onBindService:$url  DownloadingService:$downloadingService")
            }

            /**
             * @param url
             * @param downloadingService
             */
            override fun onUnbindService(url: String, downloadingService: DownloadingService) {
                super.onUnbindService(url, downloadingService)
                mDownloadingService = null
                LogUtils.i(TAG, "onUnbindService:$url")
            }

            /**
             *
             */
            override fun onProgress(url: String, loaded: Long, length: Long, usedTime: Long) {
                val mProgress = (loaded / java.lang.Float.valueOf(length.toFloat()) * 100).toInt()
                LogUtils.i(TAG, "onProgress:$mProgress")
                super.onProgress(url, loaded, length, usedTime)
            }

            /**
             *
             */
            override fun onResult(path: String, url: String, throwable: Throwable): Boolean {
                //if (null == throwable) {
                //do you work
                //}
                return false
            }
        }
    /**
     * @return WebListenerManager
     */
    /**
     * @return IAgentWebSettings
     */
    val settings: IAgentWebSettings<*>
        get() = object : AbsAgentWebSettings() {
            private val mAgentWeb: AgentWeb? = null
            override fun bindAgentWebSupport(agentWeb: AgentWeb) {
                this.mAgentWeb = agentWeb
            }

            /**
             * @return WebListenerManager
             */
            override fun setDownloader(
                webView: WebView,
                downloadListener: DownloadListener?,
            ): WebListenerManager {
                return super.setDownloader(
                    webView,
                    DefaultDownloadImpl
                        .create(
                            requireActivity(),
                            webView,
                            mDownloadListenerAdapter,
                            mDownloadListenerAdapter,
                            this.mAgentWeb.permissionInterceptor
                        )
                )
            }
        }
    /**
     *
     * @return mUrl
     */
    val url: String
        get() {
            var target = ""
            val bundle = arguments
            if (bundle != null) {
                target = bundle.getString(KEY_URL).toString()
            }
            if (TextUtils.isEmpty(target)) {
                target = "https://github.com/xuexiangjys"
            }
            return target
        }
    protected var mWebChromeClient: WebChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            Log.i(TAG, "onProgressChanged:$newProgress  view:$view")
        }

        override fun onReceivedTitle(view: WebView, title: String) {
            var title = title
            super.onReceivedTitle(view, title)
            if (mTitleTextView != null && !TextUtils.isEmpty(title)) {
                if (title.length > 10) {
                    title = title.substring(0, 10) + "..."
                }
                mTitleTextView!!.text = title
            }
        }
    }

    @Suppress("DEPRECATION")
    protected var mWebViewClient: WebViewClient = object : WebViewClient() {
        private val timer = HashMap<String, Long?>()
        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError,
        ) {
            super.onReceivedError(view, request, error)
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            return shouldOverrideUrlLoading(view, request.url.toString() + "")
        }

        override fun shouldInterceptRequest(
            view: WebView,
            request: WebResourceRequest,
        ): WebResourceResponse? {
            return super.shouldInterceptRequest(view, request)
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return url.startsWith("intent://") && url.contains("com.youku.phone")
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            Log.i(TAG, "mUrl:$url onPageStarted  target:$url")
            timer[url] = System.currentTimeMillis()
            //if (url == url) {
            //    pageNavigator(View.GONE)
            //} else {
            pageNavigator(View.VISIBLE)
            //}
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            if (timer[url] != null) {
                val overTime = System.currentTimeMillis()
                val startTime = timer[url]
                Log.i(TAG, "  page mUrl:" + url + "  used time:" + (overTime - startTime!!))
            }
        }

        override fun onReceivedHttpError(
            view: WebView,
            request: WebResourceRequest,
            errorResponse: WebResourceResponse,
        ) {
            super.onReceivedHttpError(view, request, errorResponse)
        }

        override fun onReceivedError(
            view: WebView,
            errorCode: Int,
            description: String,
            failingUrl: String,
        ) {
            super.onReceivedError(view, errorCode, description, failingUrl)
        }
    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }*/
    /**
     *
     */
    private fun openBrowser(targetUrl: String) {
        if (TextUtils.isEmpty(targetUrl) || targetUrl.startsWith("file://")) {
            XToastUtils.toast(targetUrl + getString(R.string.cannot_open_with_browser))
            return
        }
        val intent = Intent()
        intent.action = "android.intent.action.VIEW"
        val uri = Uri.parse(targetUrl)
        intent.data = uri
        startActivity(intent)
    }

    /**
     *
     */
    private fun showPoPup(view: View) {
        if (mPopupMenu == null) {
            mPopupMenu = PopupMenu(requireContext(), view)
            mPopupMenu!!.inflate(R.menu.menu_toolbar_web)
            mPopupMenu!!.setOnMenuItemClickListener(mOnMenuItemClickListener)
        }
        mPopupMenu!!.show()
    }

    /**
     */
    private val mOnMenuItemClickListener = PopupMenu.OnMenuItemClickListener { item ->
        when (item.itemId) {
            R.id.refresh -> {
                if (mAgentWeb != null) {
                    mAgentWeb!!.urlLoader.reload()
                }
                true
            }

            R.id.copy -> {
                if (mAgentWeb != null) {
                    mAgentWeb!!.webCreator.webView.url?.let { toCopy(context, it) }
                }
                true
            }

            R.id.default_browser -> {
                if (mAgentWeb != null) {
                    mAgentWeb!!.webCreator.webView.url?.let { openBrowser(it) }
                }
                true
            }

            R.id.share -> {
                if (mAgentWeb != null) {
                    mAgentWeb!!.webCreator.webView.url?.let { shareWebUrl(it) }
                }
                true
            }

            else -> false
        }
    }

    /**
     *
     */
    private fun shareWebUrl(url: String) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_TEXT, url)
        shareIntent.type = "text/plain"
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_to)))
    }

    /**
     *
     * @param context
     * @param text
     */
    private fun toCopy(context: Context?, text: String) {
        val manager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        manager.setPrimaryClip(ClipData.newPlainText(null, text))
    }

    override fun onResume() {
        mAgentWeb!!.webLifeCycle.onResume()
        super.onResume()
    }

    override fun onPause() {
        mAgentWeb!!.webLifeCycle.onPause()
        super.onPause()
    }

    override fun onFragmentKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return mAgentWeb!!.handleKeyEvent(keyCode, event)
    }

    override fun onDestroyView() {
        mAgentWeb!!.webLifeCycle.onDestroy()
        super.onDestroyView()
    }
    // do you work
    /**
     *
     * @return
     */
    @Suppress("DEPRECATION")
    protected val middlewareWebClient: MiddlewareWebClientBase
        get() = object : MiddlewareWebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.startsWith("agentweb")) {
                    Log.i(TAG, "agentweb scheme ~")
                    return true
                }
                return super.shouldOverrideUrlLoading(view, url)
                // do you work
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest,
            ): Boolean {
                return super.shouldOverrideUrlLoading(view, request)
            }
        }
    protected val middlewareWebChrome: MiddlewareWebChromeBase
        get() = object : MiddlewareChromeClient() {}

    companion object {
        const val KEY_URL = "com.xuexiang.xuidemo.base.webview.key_url"
        val TAG: String = AgentWebFragment::class.java.simpleName
        fun getInstance(url: String?): AgentWebFragment {
            val bundle = Bundle()
            bundle.putString(KEY_URL, url)
            return getInstance(bundle)
        }

        fun getInstance(bundle: Bundle?): AgentWebFragment {
            val fragment = AgentWebFragment()
            if (bundle != null) {
                fragment.arguments = bundle
            }
            return fragment
        }
    }
}