package cn.ppps.forwarder.core.http.loader

import android.content.Context
import com.xuexiang.xhttp2.subsciber.impl.IProgressLoader

/**
 *
 * @author xuexiang
 * @since 2019-07-02 12:51
 */
@Suppress("unused")
class ProgressLoader private constructor() {
    companion object {
        private var sIProgressLoaderFactory: IProgressLoaderFactory = MiniProgressLoaderFactory()
        fun setIProgressLoaderFactory(sIProgressLoaderFactory: IProgressLoaderFactory) {
            Companion.sIProgressLoaderFactory = sIProgressLoaderFactory
        }

        /**
         *
         * @param context
         * @return
         */
        fun create(context: Context?): IProgressLoader? {
            return sIProgressLoaderFactory.create(context)
        }

        /**
         *
         * @param context
         * @return
         */
        fun create(context: Context?, message: String?): IProgressLoader? {
            return sIProgressLoaderFactory.create(context, message)
        }
    }

    init {
        throw UnsupportedOperationException("u can't instantiate me...")
    }
}