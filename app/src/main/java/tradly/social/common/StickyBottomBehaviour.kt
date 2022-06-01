package tradly.social.common

import android.view.View
import android.view.View.SCROLL_AXIS_VERTICAL
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout

class StickyBottomBehaviour(var anchorId:Int) : CoordinatorLayout.Behavior<View>(){
    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return (axes == SCROLL_AXIS_VERTICAL)
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        val anchorView:View = coordinatorLayout.findViewById(anchorId)
        val anchorLocation = IntArray(2)
        anchorView.getLocationInWindow(anchorLocation)
        val coordinatorBottom:Int = coordinatorLayout.bottom
        //vertical position, cannot scroll over the bottom of the coordinator layout
        child.y = Math.min(anchorLocation[1],coordinatorBottom-child.height).toFloat()
    }

}