package cn.ppps.forwarder.core.webview

import android.view.KeyEvent
import androidx.viewbinding.ViewBinding
import cn.ppps.forwarder.core.BaseFragment
import com.just.agentweb.core.AgentWeb

/**
 *
 * @author xuexiang
 * @since 2019/5/28 10:22
 */
abstract class BaseWebViewFragment : BaseFragment<ViewBinding?>() {
    private var mAgentWeb: AgentWeb? = null

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
}