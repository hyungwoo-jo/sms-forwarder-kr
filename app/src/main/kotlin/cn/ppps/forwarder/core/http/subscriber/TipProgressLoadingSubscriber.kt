package cn.ppps.forwarder.core.http.subscriber

import cn.ppps.forwarder.core.BaseFragment
import cn.ppps.forwarder.utils.XToastUtils
import com.xuexiang.xhttp2.exception.ApiException
import com.xuexiang.xhttp2.model.XHttpRequest
import com.xuexiang.xhttp2.subsciber.ProgressLoadingSubscriber
import com.xuexiang.xhttp2.subsciber.impl.IProgressLoader
import com.xuexiang.xutil.common.StringUtils
import com.xuexiang.xutil.common.logger.Logger

/**
 *
 * @author xuexiang
 * @since 2019-11-18 23:11
 */
@Suppress("unused")
abstract class TipProgressLoadingSubscriber<T> : ProgressLoadingSubscriber<T> {
    /**
     */
    private var mUrl: String? = null

    constructor() : super()
    constructor(fragment: BaseFragment<*>) : super(fragment.progressLoader)
    constructor(iProgressLoader: IProgressLoader?) : super(iProgressLoader)
    constructor(req: XHttpRequest) : this(req.url)
    constructor(url: String?) : super() {
        mUrl = url
    }

    override fun onError(e: ApiException) {
        super.onError(e)
        XToastUtils.error(e)
        if (!StringUtils.isEmpty(mUrl)) {
            Logger.e("Request Url: $mUrl", e)
        } else {
            Logger.e(e)
        }
    }
}