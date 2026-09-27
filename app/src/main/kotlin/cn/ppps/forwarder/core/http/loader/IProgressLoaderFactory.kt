package cn.ppps.forwarder.core.http.loader

import android.content.Context
import com.xuexiang.xhttp2.subsciber.impl.IProgressLoader

/**
 *
 * @author xuexiang
 * @since 2019-11-18 23:17
 */
interface IProgressLoaderFactory {
    /**
     *
     * @param context
     * @return
     */
    fun create(context: Context?): IProgressLoader?

    /**
     *
     * @param context
     * @return
     */
    fun create(context: Context?, message: String?): IProgressLoader?
}