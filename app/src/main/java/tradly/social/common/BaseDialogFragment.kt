package tradly.social.common

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment

open class BaseDialogFragment:DialogFragment(){
    private var progressDialog: ProgressDialog? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

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