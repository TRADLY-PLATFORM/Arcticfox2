package tradly.social.common.base

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.CoroutineScope
import tradly.social.data.model.CoroutinesManager
import java.lang.Exception

class ImageHelper {

    private var instance: ImageHelper? = null
    private val requestOptions: RequestOptions by lazy { RequestOptions().priority(Priority.HIGH).dontAnimate() }

    companion object {
        @Volatile
        private var INSTANCE: ImageHelper? = null

        fun getInstance(): ImageHelper {
            return INSTANCE ?: synchronized(this) {
                ImageHelper().also { INSTANCE = it }
            }
        }
    }


    private fun getRequestOptions(placeHolder: Int, errorImage: Int): RequestOptions {
        return requestOptions.placeholder(placeHolder).error(errorImage)
    }


    fun showImage(context: Context, imageUrl: String?, resource: ImageView?, placeHolder: Int, errorImage: Int) {
       resource?.let {
           GlideApp.with(context).load(imageUrl)
               .apply(getRequestOptions(placeHolder, errorImage))
               .into(resource)
       }
    }

    /*
    * Use allowParallelRequest True when image want loading with main Request Request and Thumbnail request.
    * Use loadWithThumbnail True when image want load with Thumbnail first then Main Request.
    * */

    fun showImage(context: Context, imageUrl: String?, resource: ImageView?, placeHolder: Int, errorImage: Int,loadWithThumbnail:Boolean,allowParallelRequest:Boolean){
        resource?.let {
           val builder =  GlideApp.with(context).load(if(!allowParallelRequest && loadWithThumbnail)getThumbUrl(imageUrl)else imageUrl)
                if(allowParallelRequest){
                    builder.thumbnail(GlideApp.with(context).load(getThumbUrl(imageUrl)))
                }
                builder.listener(object : RequestListener<Drawable>{
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        if(!allowParallelRequest && loadWithThumbnail){
                            GlideApp.with(context).load(imageUrl).preload()
                        }
                        return false
                    }
                })
                builder.apply(getRequestOptions(placeHolder, errorImage)).into(resource)
        }
    }

    fun showImage(context: Context, imageUri: Uri, resource: ImageView?, placeHolder: Int, errorImage: Int) {
        resource?.let {
            GlideApp.with(context).load(imageUri)
                .apply(getRequestOptions(placeHolder, errorImage))
                .into(resource)
        }
    }

    //when image want load with Thumbnail first then Main Request.
    fun loadThumbnailThenMain(context: Context, imageUrl: String?, resource: ImageView?, placeHolder: Int, errorImage: Int){
        resource?.let {
            val builder =  GlideApp.with(context).load(getThumbUrl(imageUrl))
            builder.listener(object : RequestListener<Drawable>{
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    GlideApp.with(context).load(imageUrl).preload()
                    return false
                }
            })
            builder.apply(getRequestOptions(placeHolder, errorImage)).into(resource)
        }
    }

    // when image want loading with main Request Request and Thumbnail request parallel
    fun loadMainWithThumbnail(context: Context, imageUrl: String?, resource: ImageView?, placeHolder: Int, errorImage: Int){
        resource?.let {
            val builder =  GlideApp.with(context).load(imageUrl)
            builder.thumbnail(GlideApp.with(context).load(getThumbUrl(imageUrl)))
            builder.apply(getRequestOptions(placeHolder, errorImage)).into(resource)
        }
    }

    fun submitRequest(scope: CoroutineScope,urls: List<String>,callback:(list:List<Bitmap>)->Unit){
        CoroutinesManager.ioThenMain(scope,{loadImageRequest(urls)},{ result->
            callback(result)
        })
    }

    private suspend fun loadImageRequest(urls:List<String>):List<Bitmap>{
        val list = mutableListOf<Bitmap>()
        for(url in urls){
           try{
               val futureTarget = Glide.with(AppController.appContext).asBitmap().load(url).submit()
               list.add(futureTarget.get())
           }
           catch (ex:Exception){
               ex.printStackTrace()
           }
        }
        return list
    }

    fun getThumbUrl(url: String?):String?{
        return try{
            if(!url.isNullOrEmpty()){
                url.substring(0, url.lastIndexOf("/") + 1).plus(AppConstant.THUMB_PREFIX).plus(url.substring(url.lastIndexOf("/") + 1, url.length))
            }
            else{
                AppConstant.EMPTY
            }
        } catch (ex:Exception){
            AppConstant.EMPTY
        }
    }
}