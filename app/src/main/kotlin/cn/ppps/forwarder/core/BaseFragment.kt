package cn.ppps.forwarder.core

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import cn.ppps.forwarder.App
import cn.ppps.forwarder.core.http.loader.ProgressLoader
import com.xuexiang.xhttp2.subsciber.impl.IProgressLoader
import com.xuexiang.xpage.base.XPageActivity
import com.xuexiang.xpage.base.XPageFragment
import com.xuexiang.xpage.core.PageOption
import com.xuexiang.xpage.enums.CoreAnim
import com.xuexiang.xpage.utils.Utils
import com.xuexiang.xrouter.facade.service.SerializationService
import com.xuexiang.xrouter.launcher.XRouter
import com.xuexiang.xui.widget.actionbar.TitleBar
import com.xuexiang.xui.widget.actionbar.TitleUtils
import java.io.Serializable
import java.lang.reflect.Type


/**
 *
 *
 *
 * @author xuexiang
 */
@Suppress("MemberVisibilityCanBePrivate", "EmptyMethod")
abstract class BaseFragment<Binding : ViewBinding?> : XPageFragment() {
    private var mIProgressLoader: IProgressLoader? = null

    /**
     * ViewBinding
     */
    var binding: Binding? = null
        protected set

    override fun inflateView(inflater: LayoutInflater, container: ViewGroup): View {
        binding = viewBindingInflate(inflater, container)
        return binding!!.root
    }

    /**
     *
     * @param inflater  inflater
     * @return ViewBinding
     */
    protected abstract fun viewBindingInflate(
        inflater: LayoutInflater,
        container: ViewGroup,
    ): Binding

    private var activity: Activity? = null

    override fun getContext(): Context? {
        return if (activity == null) App.context else activity
    }

    override fun initPage() {
        activity = getActivity()
        initTitle()
        initViews()
        initListeners()
    }

    protected open fun initTitle(): TitleBar? {
        return TitleUtils.addTitleBarDynamic(
            rootView as ViewGroup,
            pageTitle
        ) { popToBack() }
    }

    override fun initListeners() {}

    /**
     *
     */
    val progressLoader: IProgressLoader?
        get() {
            if (mIProgressLoader == null) {
                mIProgressLoader = ProgressLoader.create(context)
            }
            return mIProgressLoader
        }

    /**
     *
     */
    fun getProgressLoader(message: String?): IProgressLoader? {
        if (mIProgressLoader == null) {
            mIProgressLoader = ProgressLoader.create(context, message)
        } else {
            mIProgressLoader!!.updateMessage(message)
        }
        return mIProgressLoader
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val root = rootView as ViewGroup
        if (root.getChildAt(0) is TitleBar) {
            root.removeViewAt(0)
            initTitle()
        }
    }

    override fun onDestroyView() {
        if (mIProgressLoader != null) {
            mIProgressLoader!!.dismissLoading()
        }
        super.onDestroyView()
        binding = null
    }

    /**
     *
     * @param <T>
     * @return
    </T> */
    fun <T : XPageFragment> openNewPage(clazz: Class<T>): Fragment? {
        return PageOption.to(clazz)
            .setNewActivity(true)
            .open(this)
    }

    /**
     *
     * @param <T>
     * @return
    </T> */
    fun <T : XPageFragment> openNewPage(pageName: String): Fragment? {
        return PageOption.to(pageName)
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
        clazz: Class<T>,
        containActivityClazz: Class<out XPageActivity>,
    ): Fragment? {
        return PageOption.to(clazz)
            .setNewActivity(true)
            .setContainActivityClazz(containActivityClazz)
            .open(this)
    }

    /**
     *
     * @param <T>
     * @return
    </T> */
    fun <T : XPageFragment?> openNewPage(clazz: Class<T>, key: String, value: Any?): Fragment? {
        val option = PageOption.to(clazz).setNewActivity(true)
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

    /**
     *
     */
    fun <T> deserializeObject(input: String?, clazz: Type?): T {
        return XRouter.getInstance().navigation(SerializationService::class.java)
            .parseObject(input, clazz)
    }

    override fun hideCurrentPageSoftInput() {
        if (activity == null) {
            return
        }
        Utils.hideSoftInputClearFocus(requireActivity().currentFocus)
    }
}
