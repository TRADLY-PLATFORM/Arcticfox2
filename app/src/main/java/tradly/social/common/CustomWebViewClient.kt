package tradly.social.common

import android.view.View
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ProgressBar

class CustomWebViewClient internal constructor(var progressBar: ProgressBar?) : WebChromeClient() {
    override fun onProgressChanged(view: WebView, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        if (progressBar != null) {
            if (newProgress == 100) {
                progressBar?.visibility = View.GONE
                return
            }
            progressBar?.visibility = View.VISIBLE
            progressBar?.progress = newProgress
        }
    }

    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        return super.onJsAlert(view, url, message, result)
    }
}