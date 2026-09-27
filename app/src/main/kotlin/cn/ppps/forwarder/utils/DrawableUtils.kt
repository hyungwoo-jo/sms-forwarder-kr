package cn.ppps.forwarder.utils

import android.graphics.drawable.GradientDrawable

/**
 * @author xuexiang
 */
@Suppress("unused")
class DrawableUtils private constructor() {
    companion object {
        /**
         */
        fun createRectangleDrawable(color: Int, cornerRadius: Float): GradientDrawable {
            val gradientDrawable = GradientDrawable()
            gradientDrawable.shape = GradientDrawable.RECTANGLE
            gradientDrawable.cornerRadius = cornerRadius
            gradientDrawable.setColor(color)
            return gradientDrawable
        }

        /**
         */
        fun createRectangleDrawable(colors: IntArray?, cornerRadius: Float): GradientDrawable {
            val gradientDrawable = GradientDrawable()
            gradientDrawable.shape = GradientDrawable.RECTANGLE
            gradientDrawable.cornerRadius = cornerRadius
            gradientDrawable.colors = colors
            return gradientDrawable
        }

        /**
         */
        fun createOvalDrawable(color: Int): GradientDrawable {
            val gradientDrawable = GradientDrawable()
            gradientDrawable.shape = GradientDrawable.OVAL
            gradientDrawable.setColor(color)
            return gradientDrawable
        }

        /**
         */
        fun createOvalDrawable(colors: IntArray?): GradientDrawable {
            val gradientDrawable = GradientDrawable()
            gradientDrawable.shape = GradientDrawable.OVAL
            gradientDrawable.colors = colors
            return gradientDrawable
        }
    }

    init {
        throw UnsupportedOperationException("Can not be instantiated.")
    }

}