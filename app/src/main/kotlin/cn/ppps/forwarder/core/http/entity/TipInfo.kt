package cn.ppps.forwarder.core.http.entity

import androidx.annotation.Keep

/**
 * @author xuexiang
 * @since 2019-08-28 15:35
 */
@Keep
class TipInfo {
    /**
     * content :
     *
     *
     *<br></br>
     */
    var title: String? = null
    var content: String? = null
    override fun toString(): String {
        return "TipInfo{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                '}'
    }
}