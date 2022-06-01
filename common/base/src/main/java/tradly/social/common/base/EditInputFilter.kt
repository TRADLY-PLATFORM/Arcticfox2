package tradly.social.common.base

import android.text.InputFilter
import android.text.Spanned
import tradly.social.domain.entities.Constant

class EditInputFilter(var regex:Regex) : InputFilter{
    override fun filter(
        srcText: CharSequence?,
        p1: Int,
        p2: Int,
        p3: Spanned?,
        p4: Int,
        p5: Int
    ): CharSequence {
        if(srcText=="")return srcText
        if(srcText.toString().matches(regex)){
            return srcText?:Constant.EMPTY
        }
        return Constant.EMPTY
    }
}