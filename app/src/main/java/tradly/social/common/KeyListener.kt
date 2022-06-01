package tradly.social.common

import android.content.Context
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

class KeyListener :View.OnKeyListener{
    private var editText: EditText?=null
    private var edtTxtPrevToFocus: EditText?=null
    private var ctx: Context?=null

     constructor(ctx:Context,edtTxtPrevToFocus: EditText?, editText: EditText?) {
        this.editText = editText
        this.ctx = ctx
        this.edtTxtPrevToFocus = edtTxtPrevToFocus
    }

    override fun onKey(view: View, actionId: Int, keyEvent: KeyEvent): Boolean {
        if (KeyEvent.KEYCODE_DEL == actionId) {
            if (editText?.text?.length == 0) {
                if (edtTxtPrevToFocus != null) {
                    editText?.clearFocus()
                    requestFocus(edtTxtPrevToFocus)
                }
                return true
            }
        }
        return false
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