package tradly.social.common.font

import android.content.Context
import android.graphics.Typeface
import tradly.social.common.common.AppConstant
import tradly.social.common.persistance.shared.PreferenceSecurity

object FontManager {

    var currentFont:Typeface? = null
    private var fontMap = mutableMapOf<String,Typeface>()

    fun init(context: Context){
        val font = PreferenceSecurity.getString(AppConstant.PREF_CURRENT_APP_FONT)
        if(font.isNotEmpty()){
            getFont(context,font)
        }
    }
    fun getFont(context: Context,fontName:String):Typeface?{
        return if(fontMap.containsKey(fontName)){
            fontMap[fontName]
        } else{
            val typeface = Typeface.createFromAsset(context.assets,fontName)
            fontMap[fontName] = typeface
            currentFont = typeface
            typeface
        }
    }


}