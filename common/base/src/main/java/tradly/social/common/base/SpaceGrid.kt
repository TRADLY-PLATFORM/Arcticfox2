package tradly.social.common.base

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
 class SpaceGrid  constructor(
    private val mSpanCount: Int,
    private val mSpacing: Int,
    private val mIncludeEdge: Boolean
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % mSpanCount
        if (mIncludeEdge) {
            outRect.left = mSpacing - column * mSpacing / mSpanCount
            outRect.right = (column + 1) * mSpacing / mSpanCount
            if (position < mSpanCount) {
                outRect.top = mSpacing
            }
            outRect.bottom = mSpacing
        } else {
            outRect.left = column * mSpacing / mSpanCount
            outRect.right = mSpacing - (column + 1) * mSpacing / mSpanCount
            if (position < mSpanCount) {
                outRect.top = mSpacing
            }
        }
    }
}