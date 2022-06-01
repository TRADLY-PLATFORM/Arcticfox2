package tradly.social.common.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tradly.social.domain.entities.Constant
import org.json.JSONObject
import java.io.File
import java.util.*


fun Context?.showToast(messageId: Int, length: Int = Toast.LENGTH_SHORT) {
   this?.let {
       Toast.makeText(it, it.getString(messageId), length).show()
   }
}

fun Context?.showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
   this?.let {
       Toast.makeText(it, message, length).show()
   }
}

fun EditText?.getString(): String = this?.text?.toString()?.trim() ?: Constant.EMPTY

fun EditText?.getStringWithID():Pair<Int,String> = Pair(this?.id?:0,this?.text?.toString()?.trim() ?: Constant.EMPTY)

fun Context.getTwoStringData(any1: Any?, any2: Any?): String = String.format(
    getString(R.string.two_data),
    if (any1 is Int) getString(any1) else any1 as? String,
    if (any2 is Int) getString(any2) else any2 as? String
)


fun <T> Activity.startActivityForResult(cls: Class<T>, requestCode: Int, bundle: Bundle? = null) {
    val intent = Intent(this, cls)
    bundle?.let { intent.putExtras(it) }
    this.startActivityForResult(intent, requestCode)
}

fun Activity.startActivityResult(intent: Intent,@ActivityRequestCodes.Code requestCode: Int){
    this.startActivityForResult(intent,requestCode)
}

fun Fragment.startActivityResult(intent: Intent,@ActivityRequestCodes.Code requestCode: Int){
    this.startActivityForResult(intent,requestCode)
}

fun <T> Activity.startActivity(cls: Class<T>, bundle: Bundle? = null) {
    val intent = Intent(this, cls)
    bundle?.let { intent.putExtras(it) }
    this.startActivity(intent)
}

fun <T> Activity.startActivity(cls: Class<T>, bundleIntent: Intent) {
    val intent = Intent(this, cls)
    bundleIntent.let { intent.putExtras(it) }
    this.startActivity(intent)
}

fun <T> Context.startActivity(cls: Class<T>, bundle: Bundle? = null) {
    val intent = Intent(this, cls)
    bundle?.let { intent.putExtras(it) }
    this.startActivity(intent)
}


fun View.safeClickListener(onClick: (view: View) -> Unit) {
    setOnClickListener(CustomOnClickListener(object : CustomOnClickListener.OnCustomClickListener {
        override fun onCustomClick(view: View) {
            onClick(view)
        }}))
}


fun Activity.getStringData(key:String) = intent.getStringExtra(key)?:Constant.EMPTY

fun Activity.getIntData(key:String) = intent.getStringExtra(key)?:0

fun String?.toJsonObject():JSONObject{
    try{
        if(!this.isNullOrEmpty()){
            return JSONObject(this)
        }
    }catch (ex:Exception){}
    return JSONObject()
}

fun ImageView?.setImageByUrl(context: Context, imageUrl: String?,placeHolder: Int,errorPlaceHolder:Int = 0){
    this?.let { imageView->
        ImageHelper.getInstance().showImage(context,imageUrl,imageView,placeHolder,if(errorPlaceHolder!=0)errorPlaceHolder else placeHolder)
    }
}

fun String?.getValue() = this ?: AppConstant.EMPTY

fun String.getOrNull() = if(this.isNotEmpty())this else null

infix fun Int.percentOf(x:Int)= (((1.0f)*x/this)*100).toInt()

fun String?.toInteger():Int{
    return try{
        this?.toInt()?:0
    }catch (ex:Exception){
        0
    }
}

fun EditText.setFilter(regex:String){
    this.filters = arrayOf<InputFilter>(EditInputFilter(Regex(regex)))
}

fun Bundle?.getStringOrEmpty(key:String):String{
    if(this!=null){
       return this.getString(key, AppConstant.EMPTY)
    }
    return AppConstant.EMPTY
}

fun TextView?.setTextWithUnderline(content:String){
    val spannableString = SpannableString(content)
    spannableString.setSpan(UnderlineSpan(),0,content.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    this?.setText(spannableString)
}

fun View?.setGone(){
    if(this!=null){
        this.visibility = View.GONE
    }
}

fun View?.setVisible(){
    if(this!=null){
        this.visibility = View.VISIBLE
    }
}

fun View?.setVisible(show:Boolean){
    if(this!=null){
        this.visibility = if (show)View.VISIBLE else View.GONE
    }
}

fun View?.setInVisible(show:Boolean){
    if(this!=null){
        this.visibility = if (show)View.VISIBLE else View.INVISIBLE
    }
}

fun View?.setInvisible(){
    if(this!=null){
        this.visibility = View.INVISIBLE
    }
}

fun View?.setConfigVisibility(key: String, setInvisible:Boolean = false){
    if(this!=null){
        this.visibility = if (AppConfigHelper.getConfigKey(key)) View.VISIBLE else {
            if (setInvisible)View.INVISIBLE else View.GONE
        }
    }
}

fun String?.isNotNull() = this!=null

fun String?.isNotNullOrEmpty() = this.isNotNull() && this!!.isNotEmpty()

val File.size get() = if (!exists()) 0.0 else length().toDouble()
val File.sizeInKb get() = size / 1024
val File.sizeInMb get() = sizeInKb / 1024
val File.sizeInGb get() = sizeInMb / 1024

fun Activity.makeStatusBarTransparent() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
            statusBarColor = Color.TRANSPARENT
        }
    }
}

fun String?.toSafeInt() =
    try{
        this?.toInt()?:0
    }catch(ex:Exception){
        0
    }


fun View.setMarginTop(marginTop: Int) {
    val menuLayoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
    menuLayoutParams.setMargins(0, marginTop, 0, 0)
    this.layoutParams = menuLayoutParams
}

inline fun <reified T> Fragment.getPrimitiveArgumentData(key: String):T{
    return when(T::class){
        String::class-> arguments?.getString(key,AppConstant.EMPTY).getOrDefault()
        Int::class-> arguments?.getInt(key).getOrDefault()
        Double::class-> arguments?.getDouble(key).getOrDefault()
        Float::class -> arguments?.getFloat(key).getOrDefault()
        Boolean::class -> arguments?.getBoolean(key).getOrDefault()
        Long::class -> arguments?.getLong(key).getOrDefault()
        else-> throw IllegalArgumentException("Unknown Type:: "+T::class+" Mention valid type in when statement")
    }
}

inline fun <reified T> Activity.getIntentExtra(key: String):T{
    return when(T::class){
        String::class-> intent.getStringExtra(key).getOrDefault()
        Int::class-> intent.getIntExtra(key,0).getOrDefault()
        Double::class-> intent.getDoubleExtra(key,0.0).getOrDefault()
        Float::class -> intent.getFloatExtra(key,0f).getOrDefault()
        Boolean::class -> intent.getBooleanExtra(key,false).getOrDefault()
        Long::class -> intent.getLongExtra(key,0L).getOrDefault()
        else-> throw IllegalArgumentException("Unknown Type:: "+T::class+" Mention valid type in when statement")
    }
}



inline fun <reified T>Any?.getOrDefault(): T{
    return when(T::class){
        String::class-> this as? T?:AppConstant.EMPTY as T
        Int::class-> this as? T?:0 as T
        Double::class-> this as? T?:0.0 as T
        Long::class-> this as? T?:0L as T
        Float::class -> this as? T?: 0f as T
        Boolean::class -> this as? T?: false as T
        Date::class-> this as? T?: Date() as T
        else-> throw IllegalArgumentException("Unknown Type:: Mention valid type in when statement")
    }
}

fun RecyclerView.setLoadMoreListener(loadMoreListener: OnScrollMoreListener,visibleThreshold:Int = 5){
    this.addOnScrollListener(object :RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            loadMoreListener.onScrolled(recyclerView, dx, dy)
            val linearLayoutManager = recyclerView.layoutManager!! as LinearLayoutManager
            val totalItemCount = linearLayoutManager.itemCount
            val lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
            if (totalItemCount <= lastVisibleItem + visibleThreshold) {
                loadMoreListener.onLoadMore()
            }
        }
    })
}

/*fun AdView?.setAdWidth(activity: Activity){
    val display = activity.windowManager.defaultDisplay
    val outMetrics = DisplayMetrics()
    display.getMetrics(outMetrics)
    val density = outMetrics.density
    val adWidthPixels = outMetrics.widthPixels.toFloat()
    val adWidth = (adWidthPixels / density).toInt()
    this?.adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this?.context,adWidth)
}*/



