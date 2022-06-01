package tradly.social.common

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import kotlinx.android.synthetic.main.list_item_product_slider.view.productImage
import kotlinx.android.synthetic.main.list_item_product_zoom_slider.view.*
import tradly.social.R
import tradly.social.common.base.ImageHelper

class ProductZoomSliderAdapter(internal val mContext: Context, internal val list: List<String>, val shouldZoom:Boolean = false, var callback:(()->Unit)? = null) : PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val image = list[position]
        val inflater = LayoutInflater.from(mContext)
        val layout = inflater.inflate(R.layout.list_item_product_zoom_slider, container, false) as ViewGroup
        layout.productImage?.setOnClickListener {
            callback?.let { it() }
        }

        if(shouldZoom){
            layout.zoomImage.setOnClickListener {
                callback?.let { it() }
            }
        }

        var imageView: ImageView
        if(shouldZoom){
            imageView = layout.zoomImage
            layout.zoomImage.visibility = View.VISIBLE
        }
        else{
            imageView = layout.productImage
            layout.productImage.visibility = View.VISIBLE
        }
        ImageHelper.getInstance().showImage(
            mContext, image, imageView,
            R.drawable.placeholder_image,
            R.drawable.placeholder_image
        )
        container.addView(layout)
        return layout
    }

    override fun getCount(): Int {
        return list.count()
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getItemPosition(`object`: Any): Int {
        return super.getItemPosition(`object`)
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

}