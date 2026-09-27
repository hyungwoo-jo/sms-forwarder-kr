package cn.ppps.forwarder.adapter.base.broccoli

import android.view.View
import com.xuexiang.xui.adapter.recyclerview.BaseRecyclerAdapter
import com.xuexiang.xui.adapter.recyclerview.RecyclerViewHolder
import com.xuexiang.xui.adapter.recyclerview.XRecyclerAdapter
import me.samlss.broccoli.Broccoli

/**
 *
 * @author XUE
 * @since 2019/4/8 16:33
 */
abstract class BroccoliRecyclerAdapter<T>(collection: Collection<T>?) :
    BaseRecyclerAdapter<T>(collection) {
    /**
     */
    private var mHasLoad = false
    private val mBroccoliMap: MutableMap<View, Broccoli> = HashMap()
    override fun bindData(holder: RecyclerViewHolder, position: Int, item: T) {
        var broccoli = mBroccoliMap[holder.itemView]
        if (broccoli == null) {
            broccoli = Broccoli()
            mBroccoliMap[holder.itemView] = broccoli
        }
        if (mHasLoad) {
            broccoli.removeAllPlaceholders()
            onBindData(holder, item, position)
        } else {
            onBindBroccoli(holder, broccoli)
            broccoli.show()
        }
    }

    /**
     *
     * @param holder
     * @param model
     * @param position
     */
    protected abstract fun onBindData(holder: RecyclerViewHolder?, model: T, position: Int)

    /**
     *
     * @param broccoli
     */
    protected abstract fun onBindBroccoli(holder: RecyclerViewHolder?, broccoli: Broccoli?)
    override fun refresh(collection: Collection<T>): XRecyclerAdapter<*, *> {
        mHasLoad = true
        return super.refresh(collection)
    }

    /**
     */
    fun recycle() {
        for (broccoli in mBroccoliMap.values) {
            broccoli.removeAllPlaceholders()
        }
        mBroccoliMap.clear()
        clear()
    }
}