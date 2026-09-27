package cn.ppps.forwarder.server.component

import android.content.Context
import cn.ppps.forwarder.utils.HttpServerUtils
import com.xuexiang.xrouter.utils.TextUtils
import com.yanzhenjie.andserver.annotation.Config
import com.yanzhenjie.andserver.framework.config.WebConfig
import com.yanzhenjie.andserver.framework.website.AssetsWebsite
import com.yanzhenjie.andserver.framework.website.StorageWebsite

@Config
class AppConfig : WebConfig {

    override fun onConfig(context: Context, delegate: WebConfig.Delegate) {

        val serverWebPath = HttpServerUtils.serverWebPath
        if (!TextUtils.isEmpty(serverWebPath)) {
            delegate.addWebsite(StorageWebsite(serverWebPath))
        } else {
            delegate.addWebsite(AssetsWebsite(context, "/web/"))
        }

        /*delegate.setMultipart(
            Multipart.newBuilder()
                .allFileMaxSize(1024 * 1024 * 20)
                .fileMaxSize(1024 * 1024 * 5)
                .maxInMemorySize(1024 * 20)
                .uploadTempDir(context.cacheDir)
                .build()
        )*/
    }

}