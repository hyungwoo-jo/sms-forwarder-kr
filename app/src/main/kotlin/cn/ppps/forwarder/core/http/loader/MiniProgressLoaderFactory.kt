package cn.ppps.forwarder.core.http.loader

import android.content.Context
import com.xuexiang.xhttp2.subsciber.impl.IProgressLoader

/**
 *
 * @author xuexiang
 * @since 2019-11-18 23:23
 */
class MiniProgressLoaderFactory : IProgressLoaderFactory {
    override fun create(context: Context?): IProgressLoader {
        return MiniLoadingDialogLoader(context)
    }

    override fun create(context: Context?, message: String?): IProgressLoader {
        return MiniLoadingDialogLoader(context, message)
    }
}