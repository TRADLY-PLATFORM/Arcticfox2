package tradly.social.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import tradly.social.R
import tradly.social.common.base.ImageHelper
import kotlinx.android.synthetic.main.list_item_product_slider.view.*
import tradly.social.common.base.safeClickListener

class ProductSliderAdapter(internal val mContext: Context, internal val list: List<String>,var callback:()->Unit) :
    PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val image = list[position]
        val inflater = LayoutInflater.from(mContext)
        val layout = inflater.inflate(R.layout.list_item_product_slider, container, false) as ViewGroup
        ImageHelper.getInstance().showImage(
            mContext, image, layout.productImage,
            R.drawable.placeholder_image,
            R.drawable.placeholder_image
        )
        container.addView(layout)
        layout.productImage?.safeClickListener {
            callback()
        }
        return layout
    }

    override fun getCount(): Int {
        return list.count()
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}