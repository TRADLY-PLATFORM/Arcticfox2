package tradly.social.common.base

import android.view.View

class CustomOnClickListener(var customOnClick: OnCustomClickListener):View.OnClickListener{
    interface OnCustomClickListener{
        fun onCustomClick(view:View)
    }
    override fun onClick(view: View) {
        if(AppController.appController.shouldAllowToClick()){
            customOnClick.onCustomClick(view)
        }
    }
}