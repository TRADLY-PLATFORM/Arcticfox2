package tradly.social.common

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

class OtpTextWatcher : TextWatcher {

    private var editText: EditText? = null
    private var edtTxtNextToFocus: EditText? = null
    private var ctx: Context? = null


    constructor(ctx: Context, editText: EditText, edtTxtNextToFocus: EditText) {
        this.editText = editText
        this.ctx = ctx
        this.edtTxtNextToFocus = edtTxtNextToFocus
    }

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

    }

    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
        if (charSequence.length > 0) {
            editText?.clearFocus()
            if (edtTxtNextToFocus != null) {
                requestFocus(edtTxtNextToFocus)
            }
        }
    }

    override fun afterTextChanged(editable: Editable) {

    }

    private fun requestFocus(editText: EditText?) {
        editText?.clearFocus()
        editText?.isFocusableInTouchMode = true
        editText?.requestFocus()
        val inputMethodManager = ctx?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        editText?.setSelection(editText.text.length)
    }
}