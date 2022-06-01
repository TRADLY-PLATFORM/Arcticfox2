package tradly.social.adapter

import tradly.social.domain.entities.PromoBanner
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import tradly.social.R
import tradly.social.common.base.ImageHelper
import kotlinx.android.synthetic.main.list_item_banner.view.*


class BannerViewPagerAdapter(internal val mContext: Context, internal val list: List<PromoBanner>,var onClick:(promoBanner:PromoBanner)->Unit) : PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val promoBanner = list[position]
        val inflater = LayoutInflater.from(mContext)
        val layout = inflater.inflate(R.layout.list_item_banner, container, false) as ViewGroup
        ImageHelper.getInstance().showImage(mContext,promoBanner.imagePath,layout.bannerImage,R.drawable.placeholder_image,R.drawable.placeholder_image)
        layout.setOnClickListener {
            if(promoBanner.reference.isNotEmpty()){
                onClick(promoBanner)
            }
        }
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