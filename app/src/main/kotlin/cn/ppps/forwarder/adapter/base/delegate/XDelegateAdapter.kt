package cn.ppps.forwarder.adapter.base.delegate

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.vlayout.DelegateAdapter

/**
 *
 * @author xuexiang
 * @since 2020/3/20 12:17 AM
 */
@Suppress("unused")
abstract class XDelegateAdapter<T, V : RecyclerView.ViewHolder> : DelegateAdapter.Adapter<V> {
    /**
     */
    private val mData: MutableList<T> = ArrayList()
    /**
     */
    /**
     */
    private var selectPosition = -1

    constructor()
    constructor(list: Collection<T>?) {
        if (list != null) {
            mData.addAll(list)
        }
    }

    constructor(data: Array<T>?) {
        if (!data.isNullOrEmpty()) {
            mData.addAll(listOf(*data))
        }
    }

    /**
     *
     * @param parent
     * @param viewType
     * @return
     */
    protected abstract fun getViewHolder(parent: ViewGroup, viewType: Int): V

    /**
     *
     * @param holder
     */
    protected abstract fun bindData(holder: V, position: Int, item: T)

    /**
     *
     * @return
     */
    protected fun inflateView(parent: ViewGroup, @LayoutRes layoutId: Int): View {
        return LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): V {
        return getViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: V, position: Int) {
        bindData(holder, position, mData[position])
    }

    /**
     *
     * @param position
     * @return
     */
    private fun getItem(position: Int): T? {
        return if (checkPosition(position)) mData[position] else null
    }

    private fun checkPosition(position: Int): Boolean {
        return position >= 0 && position <= mData.size - 1
    }

    val isEmpty: Boolean
        get() = itemCount == 0

    override fun getItemCount(): Int {
        return mData.size
    }

    /**
     */
    val data: List<T>
        get() = mData

    /**
     *
     * @param pos
     * @param item
     * @return
     */
    fun add(pos: Int, item: T): XDelegateAdapter<*, *> {
        mData.add(pos, item)
        notifyItemInserted(pos)
        return this
    }

    /**
     *
     * @param item
     * @return
     */
    fun add(item: T): XDelegateAdapter<*, *> {
        mData.add(item)
        notifyItemInserted(mData.size - 1)
        return this
    }

    /**
     *
     * @param pos
     * @return
     */
    fun delete(pos: Int): XDelegateAdapter<*, *> {
        mData.removeAt(pos)
        notifyItemRemoved(pos)
        return this
    }

    /**
     *
     * @param pos
     * @param item
     * @return
     */
    fun refresh(pos: Int, item: T): XDelegateAdapter<*, *> {
        mData[pos] = item
        notifyItemChanged(pos)
        return this
    }

    /**
     *
     * @param collection
     * @return
     */
    @SuppressLint("NotifyDataSetChanged")
    open fun refresh(collection: Collection<T>?): XDelegateAdapter<*, *> {
        if (collection != null) {
            mData.clear()
            mData.addAll(collection)
            selectPosition = -1
            notifyDataSetChanged()
        }
        return this
    }

    /**
     *
     * @param array
     * @return
     */
    @SuppressLint("NotifyDataSetChanged")
    fun refresh(array: Array<T>?): XDelegateAdapter<*, *> {
        if (!array.isNullOrEmpty()) {
            mData.clear()
            mData.addAll(listOf(*array))
            selectPosition = -1
            notifyDataSetChanged()
        }
        return this
    }

    /**
     *
     * @param collection
     * @return
     */
    @SuppressLint("NotifyDataSetChanged")
    fun loadMore(collection: Collection<T>?): XDelegateAdapter<*, *> {
        if (collection != null) {
            mData.addAll(collection)
            notifyDataSetChanged()
        }
        return this
    }

    /**
     *
     * @param array
     * @return
     */
    @SuppressLint("NotifyDataSetChanged")
    fun loadMore(array: Array<T>?): XDelegateAdapter<*, *> {
        if (!array.isNullOrEmpty()) {
            mData.addAll(listOf(*array))
            notifyDataSetChanged()
        }
        return this
    }

    /**
     *
     * @param item
     * @return
     */
    @SuppressLint("NotifyDataSetChanged")
    fun load(item: T?): XDelegateAdapter<*, *> {
        if (item != null) {
            mData.add(item)
            notifyDataSetChanged()
        }
        return this
    }

    /**
     *
     * @param selectPosition
     * @return
     */
    @SuppressLint("NotifyDataSetChanged")
    fun setSelectPosition(selectPosition: Int): XDelegateAdapter<*, *> {
        this.selectPosition = selectPosition
        notifyDataSetChanged()
        return this
    }

    /**
     *
     */
    val selectItem: T?
        get() = getItem(selectPosition)

    /**
     */
    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        if (!isEmpty) {
            mData.clear()
            selectPosition = -1
            notifyDataSetChanged()
        }
    }
}