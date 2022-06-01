package tradly.social.common.views.custom

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView
import tradly.social.common.font.FontManager

class CustomTextView :TextView{
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun setTypeface(tf: Typeface?) {
        if(FontManager.currentFont!=null){
            super.setTypeface(FontManager.currentFont)
        }
        else{
            super.setTypeface(tf)
        }
    }
}