package tradly.social.common

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager.widget.ViewPager

class CustomViewPager : ViewPager {
    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attr: AttributeSet) : super(ctx, attr)
}