package tradly.social.common

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalSpaceItemDecoration(private val mHorizontalSpaceWidth: Int) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.right = mHorizontalSpaceWidth
    }
}