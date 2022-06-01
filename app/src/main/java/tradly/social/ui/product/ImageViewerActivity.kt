package tradly.social.ui.product

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_image_viewer.*
import kotlinx.android.synthetic.main.toolbar.*
import tradly.social.R
import tradly.social.common.base.AppConstant
import tradly.social.common.ProductZoomSliderAdapter
import tradly.social.common.util.parser.extension.toList
import tradly.social.common.base.BaseActivity

class ImageViewerActivity : BaseActivity() {
    lateinit var imageSliderAdapter: ProductZoomSliderAdapter
    var isSystemUIShowing:Boolean = false
    var imageList = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_image_viewer)
        setToolbar(toolbar, backNavIcon = R.drawable.ic_back)
        toolbar?.setTitle(AppConstant.EMPTY)
        toolbar?.background = ContextCompat.getDrawable(this, android.R.color.transparent)
        intent.getStringExtra(AppConstant.BundleProperty.IMAGES)?.let {
            setSliderAdapter(getImageList(it)!!)
        }

        window.decorView.setOnSystemUiVisibilityChangeListener {
            if (it == 0) {
                toolbar.animate().setDuration(150).setListener(object : AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        toolbar.visibility = View.VISIBLE
                    }
                }).start()
                showSystemUI()
                isSystemUIShowing = true
            } else {
                toolbar.animate().setDuration(150).setListener(object : AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        toolbar.visibility = View.GONE
                    }
                }).start()
                hideSystemUI()
                isSystemUIShowing = false
            }
        }
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        hideSystemUI()
        setToolbarMargin()
    }

    private fun setToolbarMargin(){
        val params = toolbar.layoutParams as? ConstraintLayout.LayoutParams
        params?.setMargins(0, getStatusBarHeight(), 0, 0)
        toolbar.layoutParams = params
        if (Build.VERSION.SDK_INT >= 19) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(android.R.color.black)
        }
    }
    fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE)
    }

    private fun setSliderAdapter(list: List<String>) {
        imageList.addAll(list)
        imageSliderAdapter = ProductZoomSliderAdapter(this, imageList, true , callback = {
            if(isSystemUIShowing){
                hideSystemUI()
            }
            else{
                showSystemUI()
            }
        })
        viewPager.adapter = imageSliderAdapter
        viewPager.currentItem = intent.getIntExtra(AppConstant.BundleProperty.POSITION,0)
    }

    private fun getImageList(list: String): List<String>? = list.toList<String>()
}
