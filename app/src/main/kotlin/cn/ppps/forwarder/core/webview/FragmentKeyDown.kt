package cn.ppps.forwarder.core.webview

import android.view.KeyEvent

/**
 *
 *
 * @author xuexiang
 */
interface FragmentKeyDown {
    /**
     * @param keyCode
     * @param event
     * @return
     */
    fun onFragmentKeyDown(keyCode: Int, event: KeyEvent?): Boolean
}