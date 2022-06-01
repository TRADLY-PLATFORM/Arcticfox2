package tradly.social.common.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import tradly.social.common.uiEntity.Theme

class ThemeUtil {
    companion object {
        private var defaultTheme = ThemeList.THEME_GREEN

        object ThemeList{
            const val THEME_GREEN = 0x1
            const val THEME_BLUE = 0x2
            const val THEME_RED = 0x3
            const val THEME_PERSIAN_GREEN = 0x4
            const val THEME_BR_GREEN = 0x5
            const val THEME_BRICK_ORANGE = 0x6
            const val THEME_LS_GREEN = 0x7
            const val THEME_BRONZE = 0x8
            const val THEME_NAVY = 0x9
        }
        fun getSelectedTheme(): Int =
            when(tradly.social.common.persistance.shared.PreferenceSecurity.getInt(AppConstant.PREF_APP_THEME, defaultTheme)){
                ThemeList.THEME_GREEN -> R.style.AppTheme_Base_Tradly
                ThemeList.THEME_BLUE ->R.style.AppTheme_Base_Blue
                ThemeList.THEME_RED ->R.style.AppTheme_Base_Red
                ThemeList.THEME_PERSIAN_GREEN ->R.style.AppTheme_Base_PersianGreen
                ThemeList.THEME_BR_GREEN ->R.style.AppTheme_Base_BRGreen
                ThemeList.THEME_BRICK_ORANGE ->R.style.AppTheme_Base_BrickOrange
                else->R.style.AppTheme_Base_Tradly
            }

        fun getSelectedThemeId():Int = tradly.social.common.persistance.shared.PreferenceSecurity.getInt(
            AppConstant.PREF_APP_THEME, defaultTheme
        )

        fun getResourceValue(context: Context?, resId: Int): Int {
            val res: Int
            val typedValue = TypedValue()
            var theme = context?.theme
            theme?.resolveAttribute(resId, typedValue, true)
            res = typedValue.resourceId
            return res
        }

        fun getResourceDrawable(context: Context?, resId: Int): Int {
            val res: Int
            val typedValue = TypedValue()
            var theme = context?.theme
            theme?.resolveAttribute(resId, typedValue, true)
            res = typedValue.resourceId
            return res
        }

        fun applyTheme(activity: Activity ,themeId: Int) {
            persistTheme(themeId)
            val intent = Intent(activity, activity.javaClass)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.finish()
            activity.overridePendingTransition(0, 0)
            activity.startActivity(intent)
        }

        private fun persistTheme(themeId:Int){
            tradly.social.common.persistance.shared.PreferenceSecurity.putInt(AppConstant.PREF_APP_THEME,themeId)
        }

        fun getThemeList(): List<Theme> {
            val selectedTheme = tradly.social.common.persistance.shared.PreferenceSecurity.getInt(
                AppConstant.PREF_APP_THEME, ThemeList.THEME_GREEN
            )
            val themeList = mutableListOf<Theme>()
            themeList.add(Theme(ThemeList.THEME_GREEN, R.color.colorPrimaryGreen, false))
            themeList.add(Theme(ThemeList.THEME_BLUE, R.color.colorPrimaryBlue, false))
            themeList.add(Theme(ThemeList.THEME_RED,R.color.colorPrimaryRed,false))
            themeList.add(Theme(ThemeList.THEME_PERSIAN_GREEN,R.color.colorPrimaryPersianGreen,false))
            themeList.add(Theme(ThemeList.THEME_BR_GREEN,R.color.colorPrimaryGreenBR,false))
            themeList.add(Theme(ThemeList.THEME_BRICK_ORANGE,R.color.colorPrimaryBrickOrange,false))
            themeList.add(Theme(ThemeList.THEME_LS_GREEN,R.color.colorPrimaryLSGreen,false))
            return themeList.also { it.find { i -> i.id == selectedTheme }?.isSelected = true }

        }
    }
}
