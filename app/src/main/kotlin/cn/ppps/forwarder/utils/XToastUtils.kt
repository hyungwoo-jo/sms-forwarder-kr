package cn.ppps.forwarder.utils

import android.annotation.SuppressLint
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import com.xuexiang.xui.XUI
import com.xuexiang.xui.widget.toast.XToast

/**
 *
 * @author xuexiang
 * @since 2019-06-30 19:04
 */
class XToastUtils private constructor() {
    @SuppressLint("CheckResult")
    companion object {
        @MainThread
        fun toast(message: CharSequence) {
            XToast.normal(XUI.getContext(), message).show()
        }

        @MainThread
        fun toast(@StringRes message: Int) {
            XToast.normal(XUI.getContext(), message).show()
        }

        @MainThread
        fun toast(message: CharSequence, duration: Int) {
            XToast.normal(XUI.getContext(), message, duration).show()
        }

        @MainThread
        fun toast(@StringRes message: Int, duration: Int) {
            XToast.normal(XUI.getContext(), message, duration).show()
        }

        @MainThread
        fun error(throwable: Throwable) {
            XToast.error(XUI.getContext(), throwable.message!!).show()
        }

        @MainThread
        fun error(message: CharSequence) {
            XToast.error(XUI.getContext(), message).show()
        }

        @MainThread
        fun error(@StringRes message: Int) {
            XToast.error(XUI.getContext(), message).show()
        }

        @MainThread
        fun error(message: CharSequence, duration: Int) {
            XToast.error(XUI.getContext(), message, duration).show()
        }

        @MainThread
        fun error(@StringRes message: Int, duration: Int) {
            XToast.error(XUI.getContext(), message, duration).show()
        }

        @MainThread
        fun success(message: CharSequence) {
            XToast.success(XUI.getContext(), message).show()
        }

        @MainThread
        fun success(@StringRes message: Int) {
            XToast.success(XUI.getContext(), message).show()
        }

        @MainThread
        fun success(message: CharSequence, duration: Int) {
            XToast.success(XUI.getContext(), message, duration).show()
        }

        @MainThread
        fun success(@StringRes message: Int, duration: Int) {
            XToast.success(XUI.getContext(), message, duration).show()
        }

        @MainThread
        fun info(message: CharSequence) {
            XToast.info(XUI.getContext(), message).show()
        }

        @MainThread
        fun info(@StringRes message: Int) {
            XToast.info(XUI.getContext(), message).show()
        }

        @MainThread
        fun info(message: CharSequence, duration: Int) {
            XToast.info(XUI.getContext(), message, duration).show()
        }

        @MainThread
        fun info(@StringRes message: Int, duration: Int) {
            XToast.info(XUI.getContext(), message, duration).show()
        }

        @MainThread
        fun warning(message: CharSequence) {
            XToast.warning(XUI.getContext(), message).show()
        }

        @MainThread
        fun warning(@StringRes message: Int) {
            XToast.warning(XUI.getContext(), message).show()
        }

        @MainThread
        fun warning(message: CharSequence, duration: Int) {
            XToast.warning(XUI.getContext(), message, duration).show()
        }

        @MainThread
        fun warning(@StringRes message: Int, duration: Int) {
            XToast.warning(XUI.getContext(), message, duration).show()
        }

        init {
            XToast.Config.get()
                .setAlpha(200)
                .allowQueue(false)
        }
    }

    init {
        throw UnsupportedOperationException("u can't instantiate me...")
    }
}