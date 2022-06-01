package tradly.social.ui

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_web.*
import tradly.social.R
import tradly.social.common.base.AppConstant
import tradly.social.common.CustomWebViewClient
import tradly.social.common.base.getStringData
import tradly.social.common.base.BaseActivity

class WebActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        toolbarTitle.setText(getStringData(AppConstant.BundleProperty.TOOLBAR_TITLE))
        val progressDrawable: Drawable = progress.progressDrawable.mutate()
        progressDrawable.setColorFilter(
            ContextCompat.getColor(this, R.color.colorBlueLight),
            PorterDuff.Mode.SRC_IN
        )
        progress.progressDrawable = progressDrawable
        progress.visibility = View.VISIBLE
        bacNav.setOnClickListener { finish() }
        webview.webViewClient = WebViewClient()
        webview.settings.setAppCacheEnabled(false)
        webview.settings.setSupportZoom(true)
        webview.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webview.webChromeClient = CustomWebViewClient(progress)
        webview.loadUrl(getStringData(AppConstant.BundleProperty.WEB_URL))
    }
}

