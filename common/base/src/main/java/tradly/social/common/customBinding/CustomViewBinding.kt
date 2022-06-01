package tradly.social.common.customBinding

import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import tradly.social.common.base.DateTimeHelper
import tradly.social.common.base.GenericAdapter
import tradly.social.common.base.ImageHelper
import tradly.social.common.resources.R
import tradly.social.common.views.custom.CustomTextView

@BindingAdapter(value = ["imageUrl","placeholder","error"],requireAll = false)
fun setImageUrl(imageView:ImageView,imageUrl:String?,placeholder:Int?,error:Int?){
    ImageHelper.getInstance().showImage(imageView.context,imageUrl,imageView,placeholder?: R.drawable.placeholder_image,error?:R.drawable.placeholder_image)
}

@BindingAdapter(value = ["startDate","endDate","setDateFormat"],requireAll = true)
fun setDateRange(customTextView: CustomTextView,startDate:Long,endDate:Long,dateFormat:String?){
    dateFormat?.let {
        customTextView.text = customTextView.context.getString(
            R.string.placeholder_two_with_hyphen,
            DateTimeHelper.getDateFromTimeMillis(startDate, dateFormat),
            DateTimeHelper.getDateFromTimeMillis(endDate, dateFormat)
        )
    }
}

@BindingAdapter(value = ["milliseconds","setDateFormat"],requireAll = true)
fun setDate(customTextView: CustomTextView,milliseconds:Long,dateFormat:String?){
    dateFormat?.let {
        customTextView.text =  DateTimeHelper.getDateFromTimeMillis(milliseconds, dateFormat)
    }
}

@BindingAdapter(value = ["android:src","android:tint"],requireAll = false)
fun setFloatingButtonRes(floatingActionButton: FloatingActionButton,src:Int?=null,tint:Int?=null){
    if (src!=null){
        floatingActionButton.setImageResource(src)
    }
    if (tint!=null){
        floatingActionButton.imageTintList = ContextCompat.getColorStateList(floatingActionButton.context,tint)
    }
}

@BindingAdapter("onFocusListener",requireAll = false)
fun bindFocusListener(textInputEditText: TextInputEditText, listener: View.OnFocusChangeListener?){
    listener?.let {
        if (textInputEditText.onFocusChangeListener == null){
            textInputEditText.onFocusChangeListener = listener
        }
    }
}

@BindingAdapter("setError",requireAll = false)
fun bindError(textInputLayout: TextInputLayout, errorResId:Int = 0){
    if (errorResId!=0){
        textInputLayout.error = textInputLayout.context.getString(errorResId)
    }
}

@BindingAdapter("setInt",requireAll = false)
fun setText(textInputEditText: TextInputEditText,intValue:Int){
    if (textInputEditText.text.toString() != intValue.toString()){
        textInputEditText.setText(intValue.toString())
    }
}


@BindingAdapter("stringRes",requireAll = false)
fun setStringRes(button:Button, stringRes:Int?=null){
    if (stringRes!=null){
        button.text = button.context.getString(stringRes)
    }
}

/*@BindingAdapter(value = ["app:onBindClickListener","app:onBindValue"],requireAll = false)
fun <T>bindClickListener(view:View,listener:GenericAdapter.OnClickItemListListener<T>?,value:T?){
    view.setOnClickListener {
        takeIf{ listener!=null && value!=null }?.let{
            listener?.onClick(value!!,view)
        }
    }
}*/

@InverseBindingAdapter(attribute = "setInt")
fun getText(textInputEditText: TextInputEditText):Int{
    return textInputEditText.text.toString().toInt()
}

@BindingAdapter("setIntAttrChanged",requireAll = false)
fun setListeners(textInputEditText: TextInputEditText,attrChange: InverseBindingListener) {
    textInputEditText.doOnTextChanged { text, start, before, count ->
        attrChange.onChange()
    }
}
