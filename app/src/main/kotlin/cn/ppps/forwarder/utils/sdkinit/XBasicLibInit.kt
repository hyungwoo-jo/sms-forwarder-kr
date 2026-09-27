package cn.ppps.forwarder.utils.sdkinit

import android.app.Application
import cn.ppps.forwarder.App
import cn.ppps.forwarder.R
import cn.ppps.forwarder.core.BaseActivity
import cn.ppps.forwarder.utils.SettingUtils
import cn.ppps.forwarder.utils.XToastUtils
import com.xuexiang.xaop.XAOP
import com.xuexiang.xhttp2.XHttp
import com.xuexiang.xhttp2.XHttpSDK
import com.xuexiang.xhttp2.cache.model.CacheMode
import com.xuexiang.xpage.PageConfig
import com.xuexiang.xrouter.launcher.XRouter
import com.xuexiang.xui.XUI
import com.xuexiang.xutil.XUtil
import com.xuexiang.xutil.common.StringUtils

/**
 *
 * @author xuexiang
 * @since 2019-06-30 23:54
 */
class XBasicLibInit private constructor() {
    companion object {
        /**
         */
        fun init(application: Application) {
            initXUtil(application)

            initXHttp2(application)

            initXPage(application)

            initXAOP(application)

            initXUI(application)

            initRouter(application)
        }

        /**
         */
        private fun initXUtil(application: Application) {
            XUtil.init(application)
            XUtil.debug(App.isDebug)
        }

        /**
         */
        private fun initXHttp2(application: Application) {
            XHttpSDK.init(application)
            // Only the app's redacted interceptor may emit HTTP diagnostics.
            XHttpSDK.setBaseUrl("https://localhost.invalid/")
            XHttpSDK.addInterceptor(cn.ppps.forwarder.utils.interceptor.SecureTransportInterceptor())
            XHttp.getInstance().addNetworkInterceptor(cn.ppps.forwarder.utils.interceptor.SecureTransportInterceptor())
            cn.ppps.forwarder.utils.interceptor.SecureTransportInterceptor.configure(XHttp.getOkHttpClientBuilder())
            //XHttpSDK.debug(LoggingInterceptor())
            //XHttpSDK.addInterceptor(CustomDynamicInterceptor())
            //XHttpSDK.addInterceptor(CustomExpiredInterceptor())
            XHttp.getInstance()
                .debug(false)
                .setCacheMode(CacheMode.NO_CACHE)
                .setTimeout(SettingUtils.requestTimeout * 1000L)
        }

        /**
         */
        private fun initXPage(application: Application) {
            PageConfig.getInstance()
                .debug(App.isDebug)
                .setContainActivityClazz(BaseActivity::class.java)
                .init(application)
        }

        /**
         */
        private fun initXAOP(application: Application) {
            XAOP.init(application)
            XAOP.debug(App.isDebug)
            XAOP.setOnPermissionDeniedListener { permissionsDenied: List<String?>? ->
                XToastUtils.error(
                    application.getString(R.string.permission_request_denied) + StringUtils.listToString(permissionsDenied, ",")
                )
            }
        }

        /**
         */
        private fun initXUI(application: Application) {
            XUI.init(application)
            XUI.debug(App.isDebug)
        }

        /**
         */
        private fun initRouter(application: Application) {
            if (App.isDebug) {
                XRouter.openLog()
                XRouter.openDebug()
            }
            XRouter.init(application)
        }
    }

    init {
        throw UnsupportedOperationException("u can't instantiate me...")
    }
}
