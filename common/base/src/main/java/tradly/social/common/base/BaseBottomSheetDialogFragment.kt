package tradly.social.common.base

import android.app.ProgressDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class BaseBottomSheetDialogFragment:BottomSheetDialogFragment() {
    private var progressDialog: ProgressDialog? = null

    fun showLoader(message: Int) {
        progressDialog = ProgressDialog(requireContext())
        progressDialog?.setMessage(getString(message))
        progressDialog?.setCancelable(false)
        progressDialog?.show()
    }

    fun hideLoader() {
        progressDialog?.hide()
        progressDialog?.dismiss()
    }

}