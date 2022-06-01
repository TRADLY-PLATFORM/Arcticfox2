package tradly.social.common.base

import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView

interface OnLoadMoreListener {
    fun onLoadMore()
}

interface OnScrollListener{
    fun onLoadMore()
    fun onScrolled(recyclerView: RecyclerView, dx:Int, dy:Int)
}

open class OnScrollMoreListener:OnScrollListener{
    override fun onLoadMore() {

    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
    }

}