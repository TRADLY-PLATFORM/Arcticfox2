package tradly.social.common

import android.view.Gravity
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import tradly.social.BuildConfig
import tradly.social.R
import tradly.social.common.base.Utils
import tradly.social.common.base.setVisible
import tradly.social.common.resources.ResourceConfig


object Page{
    const val HOME = 1
}

object ViewItem{
    const val BOTTOM_FAB = 1
    const val HOME_TOOLBAR = 2
}

fun ImageView?.setClientImageResource(page:Int , viewItem:Int, resId:Int){
    if(this!=null){
        this.setImageResource(resId)
        when(page){
            Page.HOME->{
                when(viewItem){
                    ViewItem.BOTTOM_FAB->{
                        val layoutParams = this.layoutParams as CoordinatorLayout.LayoutParams
                        when(BuildConfig.FLAVOR){
                             "tradlyPlatform", "tradlySocial"->{
                                 layoutParams.anchorGravity = Gravity.CENTER or Gravity.TOP
                                 layoutParams.height = Utils.getPixel(context,56)
                                 layoutParams.width =  Utils.getPixel(context,56)
                             }
                        }
                        this.layoutParams = layoutParams
                        this.setVisible()
                    }
                }
            }
        }
    }
}

fun Toolbar?.setClientTitle(page:Int , viewItem:Int, title:String){
    if(this != null){
        this.title = title
        when(page){
            Page.HOME->{
                when(viewItem){
                    ViewItem.HOME_TOOLBAR->{
                        if(ResourceConfig.TENANT_ID=="wyldeplants"){
                            this.setTitleTextAppearance(context,R.style.Toolbar_Home)
                        }
                        else{
                            this.setTitleTextAppearance(context,R.style.Toolbar_Home_Regular)
                        }
                    }
                }
            }
        }
    }
}


