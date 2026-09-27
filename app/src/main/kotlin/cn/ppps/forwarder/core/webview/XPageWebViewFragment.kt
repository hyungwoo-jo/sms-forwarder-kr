package cn.ppps.forwarder.core.webview

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import cn.ppps.forwarder.App
import cn.ppps.forwarder.R
import cn.ppps.forwarder.core.BaseFragment
import cn.ppps.forwarder.databinding.FragmentAgentwebBinding
import cn.ppps.forwarder.utils.XToastUtils
import com.just.agentweb.action.PermissionInterceptor
import com.just.agentweb.core.AgentWeb
import com.just.agentweb.core.client.DefaultWebClient
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
import com.just.agentweb.widget.IWebLayout
import com.xuexiang.xaop.annotation.SingleClick
import com.xuexiang.xpage.annotation.Page
import com.xuexiang.xpage.base.XPageActivity
import com.xuexiang.xpage.base.XPageFragment
import com.xuexiang.xpage.core.PageOption
import com.xuexiang.xui.widget.actionbar.TitleBar
import com.xuexiang.xutil.common.logger.Logger
import com.xuexiang.xutil.net.JsonUtil

/**
 *
 * @author xuexiang
 * @since 2019-05-26 18:15
 */
@Suppress("DEPRECATION", "unused", "UNUSED_PARAMETER", "NAME_SHADOWING", "OVERRIDE_DEPRECATION")
@Page(params = [AgentWebFragment.KEY_URL])
class XPageWebViewFragment : BaseFragment<FragmentAgentwebBinding?>(), View.OnClickListener {
    private var mAgentWeb: AgentWeb? = null
    private var mPopupMenu: PopupMenu? = null
    private var mDownloadingService: DownloadingService? = null
    override fun viewBindingInflate(
        inflater: LayoutInflater,
        container: ViewGroup,
    ): FragmentAgentwebBinding {
        return FragmentAgentwebBinding.inflate(inflater, container, false)
    }

    override fun initTitle(): TitleBar? {
        return null
    }

    /**
     */
    override fun initViews() {
        mAgentWeb = AgentWeb.with(this)
            .setAgentWebParent(
                (rootView as LinearLayout),
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
            .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.DISALLOW)
            .interceptUnkownUrl()
            .createAgentWeb()
            .ready()
            .go(url)
        if (App.isDebug) {
            AgentWebConfig.debug()
        }
        pageNavigator(View.GONE)
        addBackgroundChild(mAgentWeb!!.webCreator.webParentLayout)

        mAgentWeb!!.webCreator.webView.overScrollMode = WebView.OVER_SCROLL_NEVER
    }

    private val webLayout: IWebLayout<*, *>
        get() = WebLayout(activity)

    private fun addBackgroundChild(frameLayout: FrameLayout) {
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

    override fun initListeners() {
        binding!!.includeTitle.ivBack.setOnClickListener(this)
        binding!!.includeTitle.ivFinish.setOnClickListener(this)
        binding!!.includeTitle.ivMore.setOnClickListener(this)
    }

    private fun pageNavigator(tag: Int) {
        binding!!.includeTitle.ivBack.visibility = tag
        binding!!.includeTitle.viewLine.visibility = tag
    }

    @SingleClick
    override fun onClick(view: View) {
        val id = view.id
        if (id == R.id.iv_back) {
            if (!mAgentWeb!!.back()) {
                popToBack()
            }
        } else if (id == R.id.iv_finish) {
            popToBack()
        } else if (id == R.id.iv_more) {
            showPoPup(view)
        }
    }
    /**
     */
    private var mDownloadListenerAdapter: DownloadListenerAdapter =
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
                mimeType: String,
                contentLength: Long,
                extra: Extra,
            ): Boolean {
                Logger.i("onStart:$url")
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
                Logger.i("onBindService:$url  DownloadingService:$downloadingService")
            }

            /**
             * @param url
             * @param downloadingService
             */
            override fun onUnbindService(url: String, downloadingService: DownloadingService) {
                super.onUnbindService(url, downloadingService)
                mDownloadingService = null
                Logger.i("onUnbindService:$url")
            }

            /**
             *
             */
            override fun onProgress(url: String, loaded: Long, length: Long, usedTime: Long) {
                val mProgress = (loaded / length.toFloat() * 100).toInt()
                Logger.i("onProgress:$mProgress")
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
     *
     * @return IAgentWebSettings
     */
    private val settings: IAgentWebSettings<*>
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
                downloadListener: DownloadListener,
            ): WebListenerManager {
                return super.setDownloader(
                    webView,
                    DefaultDownloadImpl
                        .create(
                            activity!!,
                            webView,
                            mDownloadListenerAdapter,
                            mDownloadListenerAdapter,
                            mAgentWeb.permissionInterceptor
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
                target = bundle.getString(AgentWebFragment.KEY_URL).toString()
            }
            if (TextUtils.isEmpty(target)) {
                target = "https://github.com/xuexiangjys"
            }
            return target
        }

    /**
     */
    private var mWebChromeClient: WebChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
        }

        override fun onReceivedTitle(view: WebView, title: String) {
            var title = title
            super.onReceivedTitle(view, title)
            if (!TextUtils.isEmpty(title)) {
                if (title.length > 10) {
                    title = title.substring(0, 10) + "..."
                }
                binding!!.includeTitle.toolbarTitle.text = title
            }
        }
    }

    /**
     */
    @Suppress("DEPRECATION")
    private var mWebViewClient: WebViewClient = object : WebViewClient() {
        private val mTimer = HashMap<String, Long?>()
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

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
            mTimer[url] = System.currentTimeMillis()
            //if (url == url) {
            //    pageNavigator(View.GONE)
            //} else {
            pageNavigator(View.VISIBLE)
            //}
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            if (mTimer[url] != null) {
                val overTime = System.currentTimeMillis()
                val startTime = mTimer[url]
                Logger.i(" page mUrl:" + url + "  used time:" + (overTime - startTime!!))
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
                return@OnMenuItemClickListener true
            }

            R.id.copy -> {
                if (mAgentWeb != null) {
                    mAgentWeb!!.webCreator.webView.url?.let { toCopy(context, it) }
                }
                return@OnMenuItemClickListener true
            }

            R.id.default_browser -> {
                if (mAgentWeb != null) {
                    mAgentWeb!!.webCreator.webView.url?.let { openBrowser(it) }
                }
                return@OnMenuItemClickListener true
            }

            R.id.share -> {
                if (mAgentWeb != null) {
                    mAgentWeb!!.webCreator.webView.url?.let { shareWebUrl(it) }
                }
                return@OnMenuItemClickListener true
            }

            else -> false
        }
    }

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
        if (mAgentWeb != null) {
            mAgentWeb!!.webLifeCycle.onResume()
        }
        super.onResume()
    }

    override fun onPause() {
        if (mAgentWeb != null) {
            mAgentWeb!!.webLifeCycle.onPause()
        }
        super.onPause()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return mAgentWeb != null && mAgentWeb!!.handleKeyEvent(keyCode, event)
    }

    override fun onDestroyView() {
        if (mAgentWeb != null) {
            mAgentWeb!!.destroy()
        }
        super.onDestroyView()
    }
    // do you work
    /**
     */
    private val middlewareWebClient: MiddlewareWebClientBase
        get() = object : MiddlewareWebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.startsWith("agentweb")) {
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
    private val middlewareWebChrome: MiddlewareWebChromeBase
        get() = object : MiddlewareChromeClient() {}

    /**
     */
    private var mPermissionInterceptor = PermissionInterceptor { url, permissions, action ->

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
        Logger.i("mUrl:" + url + "  permission:" + JsonUtil.toJson(permissions) + " action:" + action)
        false
    }

    companion object {
        /**
         *
         * @param xPageActivity
         * @param url
         * @return
         */
        fun openUrl(xPageActivity: XPageActivity?, url: String?): Fragment {
            return PageOption.to(XPageWebViewFragment::class.java)
                .putString(AgentWebFragment.KEY_URL, url)
                .open(xPageActivity!!)
        }

        /**
         *
         * @param fragment
         * @param url
         * @return
         */
        fun openUrl(fragment: XPageFragment?, url: String?): Fragment {
            return PageOption.to(XPageWebViewFragment::class.java)
                .setNewActivity(true)
                .putString(AgentWebFragment.KEY_URL, url)
                .open(fragment!!)
        }
    }
}