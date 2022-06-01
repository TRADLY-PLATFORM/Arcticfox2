package tradly.social.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.PagerAdapter
import tradly.social.R
import tradly.social.common.base.ImageHelper
import tradly.social.domain.entities.ImageFeed
import kotlinx.android.synthetic.main.list_item_add_product.view.*
import java.io.File

class ProductViewPagerAdapter(internal val mContext: Context, internal val list: ArrayList<ImageFeed>, var onClick:(pos:Int)->Unit, var cancelClick:(pos:Int)->Unit) : PagerAdapter() {
    val totalPic:Int = 5
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(mContext)
        val layout = inflater.inflate(R.layout.list_item_add_product, container, false) as ViewGroup
        val item = list[position]
        if(item.isAddItem){
            layout.imgCancel?.visibility = View.GONE
            layout.addFab?.visibility = View.VISIBLE
            layout.imgSelected?.visibility = View.GONE
        }
        else{
            layout.imgCancel?.visibility = View.VISIBLE
            layout.addFab?.visibility = View.GONE
            layout.imgSelected?.visibility = View.VISIBLE
            if(!item.filePath.startsWith("file")){
                ImageHelper.getInstance().showImage(mContext, item.filePath,layout.imgSelected,R.drawable.placeholder_image,R.drawable.placeholder_image)
            }
            else{
                ImageHelper.getInstance().showImage(mContext, Uri.fromFile(File(item.filePath)),layout.imgSelected,R.drawable.placeholder_image,R.drawable.placeholder_image)
            }
        }

        if (list.size == totalPic) {
            if (list.size - 1 == position) {
                layout.visibility = View.GONE
                layout.layoutParams = ConstraintLayout.LayoutParams(0, 0)
            }
        }

        layout.addFab?.setOnClickListener {
            onClick(position)
        }
        layout.imgCancel?.setOnClickListener {
            cancelClick(position)
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
        return PagerAdapter.POSITION_NONE
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}