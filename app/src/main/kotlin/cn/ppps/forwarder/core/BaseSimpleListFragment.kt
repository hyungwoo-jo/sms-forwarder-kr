package cn.ppps.forwarder.core

import android.content.res.Configuration
import android.os.Parcelable
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.xuexiang.xpage.base.XPageActivity
import com.xuexiang.xpage.base.XPageFragment
import com.xuexiang.xpage.base.XPageSimpleListFragment
import com.xuexiang.xpage.core.PageOption
import com.xuexiang.xpage.enums.CoreAnim
import com.xuexiang.xrouter.facade.service.SerializationService
import com.xuexiang.xrouter.launcher.XRouter
import com.xuexiang.xui.widget.actionbar.TitleBar
import com.xuexiang.xui.widget.actionbar.TitleUtils
import java.io.Serializable

/**
 * @author xuexiang
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
abstract class BaseSimpleListFragment : XPageSimpleListFragment() {
    override fun initPage() {
        initTitle()
        initViews()
        initListeners()
    }

    protected fun initTitle(): TitleBar {
        return TitleUtils.addTitleBarDynamic(
            rootView as ViewGroup,
            pageTitle
        ) { popToBack() }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val root = rootView as ViewGroup
        if (root.getChildAt(0) is TitleBar) {
            root.removeViewAt(0)
            initTitle()
        }
    }

    /**
     *
     * @param <T>
     * @return
    </T> */
    fun <T : XPageFragment?> openNewPage(clazz: Class<T>?): Fragment? {
        return PageOption(clazz)
            .setNewActivity(true)
            .open(this)
    }

    /**
     *
     * @param <T>
     * @return
    </T> */
    fun <T : XPageFragment?> openNewPage(pageName: String?): Fragment? {
        return PageOption(pageName)
            .setAnim(CoreAnim.slide)
            .setNewActivity(true)
            .open(this)
    }

    /**
     *
     * @param <T>
     * @return
    </T> */
    fun <T : XPageFragment?> openNewPage(
        clazz: Class<T>?,
        containActivityClazz: Class<out XPageActivity?>,
    ): Fragment? {
        return PageOption(clazz)
            .setNewActivity(true)
            .setContainActivityClazz(containActivityClazz)
            .open(this)
    }

    /**
     *
     * @param <T>
     * @return
    </T> */
    fun <T : XPageFragment?> openNewPage(clazz: Class<T>?, key: String?, value: Any?): Fragment? {
        val option = PageOption(clazz).setNewActivity(true)
        return openPage(option, key, value)
    }

    private fun openPage(option: PageOption, key: String?, value: Any?): Fragment? {
        when (value) {
            is Int -> {
                option.putInt(key, value)
            }
            is Float -> {
                option.putFloat(key, value)
            }
            is String -> {
                option.putString(key, value)
            }
            is Boolean -> {
                option.putBoolean(key, value)
            }
            is Long -> {
                option.putLong(key, value)
            }
            is Double -> {
                option.putDouble(key, value)
            }
            is Parcelable -> {
                option.putParcelable(key, value)
            }
            is Serializable -> {
                option.putSerializable(key, value)
            }
            else -> {
                option.putString(key, serializeObject(value))
            }
        }
        return option.open(this)
    }

    /**
     *
     * @param <T>
     * @return
    </T> */
    fun <T : XPageFragment?> openPage(
        clazz: Class<T>?,
        addToBackStack: Boolean,
        key: String?,
        value: String?,
    ): Fragment? {
        return PageOption(clazz)
            .setAddToBackStack(addToBackStack)
            .putString(key, value)
            .open(this)
    }

    /**
     *
     * @param <T>
     * @return
    </T> */
    fun <T : XPageFragment?> openPage(clazz: Class<T>?, key: String?, value: Any?): Fragment? {
        return openPage(clazz, true, key, value)
    }

    /**
     *
     * @param <T>
     * @return
    </T> */
    fun <T : XPageFragment?> openPage(
        clazz: Class<T>?,
        addToBackStack: Boolean,
        key: String?,
        value: Any?,
    ): Fragment? {
        val option = PageOption(clazz).setAddToBackStack(addToBackStack)
        return openPage(option, key, value)
    }

    /**
     *
     * @param <T>
     * @return
    </T> */
    fun <T : XPageFragment?> openPage(clazz: Class<T>?, key: String?, value: String?): Fragment? {
        return PageOption(clazz)
            .putString(key, value)
            .open(this)
    }

    /**
     *
     * @param <T>
     * @return
    </T> */
    fun <T : XPageFragment?> openPageForResult(
        clazz: Class<T>?,
        key: String?,
        value: Any?,
        requestCode: Int,
    ): Fragment? {
        val option = PageOption(clazz).setRequestCode(requestCode)
        return openPage(option, key, value)
    }

    /**
     *
     * @param <T>
     * @return
    </T> */
    fun <T : XPageFragment?> openPageForResult(
        clazz: Class<T>?,
        key: String?,
        value: String?,
        requestCode: Int,
    ): Fragment? {
        return PageOption(clazz)
            .setRequestCode(requestCode)
            .putString(key, value)
            .open(this)
    }

    /**
     *
     * @param <T>
     * @return
    </T> */
    fun <T : XPageFragment?> openPageForResult(clazz: Class<T>?, requestCode: Int): Fragment? {
        return PageOption(clazz)
            .setRequestCode(requestCode)
            .open(this)
    }

    /**
     *
     */
    fun serializeObject(`object`: Any?): String {
        return XRouter.getInstance().navigation(SerializationService::class.java)
            .object2Json(`object`)
    }
}
