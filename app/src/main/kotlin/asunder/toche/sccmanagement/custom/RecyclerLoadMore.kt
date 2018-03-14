package asunder.toche.sccmanagement.custom

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

/**
 *Created by ToCHe on 10/3/2018 AD.
 */
class RecyclerLoadMore private constructor(builder: Builder) : RecyclerView.OnScrollListener() {

    internal var mTotalItemCount: Int = 0
    private val mListener: LoadMoreItemListener?
    private var smoothScroller: LayoutWithSmoothScroller? = null
    private var mGridLayoutManager: GridLayoutManager? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null
    var isLoading = false
    var isMax = false
    private var mVisibleThreshold = 6

    init {
        mListener = builder.mListener
        if (builder.mLayoutManager is LayoutWithSmoothScroller) {
            smoothScroller = builder.mLayoutManager as LayoutWithSmoothScroller?
            mVisibleThreshold = 6
        } else if (builder.mLayoutManager is GridLayoutManager) {
            mGridLayoutManager = builder.mLayoutManager as GridLayoutManager?
            mVisibleThreshold = 12
        } else {
            mLinearLayoutManager = builder.mLayoutManager as LinearLayoutManager?
            mVisibleThreshold = 6
        }
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)

    }

    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (!isMax) {
            val mLastVisibleItem: Int

            if (smoothScroller != null) {
                mTotalItemCount = smoothScroller!!.getItemCount()
                mLastVisibleItem = smoothScroller!!.findLastVisibleItemPosition()
            } else if (mGridLayoutManager != null) {
                mTotalItemCount = mGridLayoutManager!!.itemCount
                mLastVisibleItem = mGridLayoutManager!!.findLastVisibleItemPosition()
            } else {
                mTotalItemCount = mLinearLayoutManager!!.itemCount
                mLastVisibleItem = mLinearLayoutManager!!.findLastVisibleItemPosition()
            }

            if (!isLoading && mTotalItemCount <= mLastVisibleItem + mVisibleThreshold) {
                mListener?.onLoadMore()
            }
        }
    }

    fun resetState() {
        this.mTotalItemCount = 0
        this.isLoading = true
    }

    interface LoadMoreItemListener {
        fun onLoadMore()
    }

    class Builder {

        var mLayoutManager: RecyclerView.LayoutManager? = null
        var mListener: LoadMoreItemListener? = null

        fun setListener(listener: LoadMoreItemListener): Builder {
            this.mListener = listener
            return this
        }

        fun setLayoutManager(layoutManager: RecyclerView.LayoutManager): Builder {
            this.mLayoutManager = layoutManager
            return this
        }

        fun build(): RecyclerLoadMore {
            return RecyclerLoadMore(this)
        }
    }
}