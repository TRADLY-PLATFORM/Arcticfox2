package tradly.social.event.explore.common

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.bottomsheet.BottomSheetBehavior
import tradly.social.event.explore.R


class CustomBottomsheetBehaviour<V : View?> : BottomSheetBehavior<V> {
    private var mLocked = false

    constructor() {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
    }

    fun setLocked(locked: Boolean) {
        mLocked = locked
    }

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: V,
        event: MotionEvent
    ): Boolean {
        var handled = false
        if (!mLocked) {
            handled = super.onInterceptTouchEvent(parent, child, event)
        }
        return handled
        /*val nested = child!!.findViewById<RecyclerView>(R.id.rvEventList) //NestedScrollView
        var x = event.x
        var y = event.y

        val position = IntArray(2)
        nested.getLocationOnScreen(position)

        var nestedX = position[0]
        var nestedY = position[1]


        var boundLeft = nestedX
        var boundRight = nestedX + nested.width
        var boundTop = nestedY
        var boundBottom = nestedY + nested.height


        if ((x > boundLeft && x < boundRight && y > boundTop && y < boundBottom) || event.action == MotionEvent.ACTION_CANCEL) {
            //Touched inside of the scrollview-> pass the touch event to the scrollview
            return false
        }*/



        //touched outside, use the parents computation to make the bottomsheet work
        return super.onInterceptTouchEvent(parent, child, event)
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        var handled = false
        if (!mLocked) {
            handled = super.onTouchEvent(parent, child, event)
        }
        return handled
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        directTargetChild: View,
        target: View,
        nestedScrollAxes: Int
    ): Boolean {
        var handled = false
        if (!mLocked) {
            handled = super.onStartNestedScroll(
                coordinatorLayout,
                child,
                directTargetChild,
                target,
                nestedScrollAxes
            )
        }
        return handled
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray
    ) {
        if (!mLocked) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed)
        }
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View) {
        if (!mLocked) {
            super.onStopNestedScroll(coordinatorLayout, child, target)
        }
    }

    override fun onNestedPreFling(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        target: View,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        var handled = false
        if (!mLocked) {
            handled = super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY)
        }
        return handled
    }
}