package tradly.social.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tradly.social.R
import tradly.social.common.base.ImageHelper
import tradly.social.common.base.ThemeUtil
import tradly.social.domain.entities.Category

class HomeCategoryGridAdapter(
    var ctx: Context,
    var isCategoryFilter: Boolean,
    var list: List<Category>,
    var viewType: Int = GRID,
    var onClick: (category: Category) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val inflater: LayoutInflater
    private val iconTintColor: Int by lazy { ThemeUtil.getResourceValue(ctx, R.attr.colorPrimary) }

    companion object {
        const val GRID = 1
        const val LIST = 2
    }

    init {
        inflater = LayoutInflater.from(ctx)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (this.viewType == GRID){
            val view = inflater.inflate(R.layout.list_item_home_category, parent, false)
            return HomeCategoryGridHolder(view)
        }
        else{
            val view = inflater.inflate(R.layout.list_item_category, parent, false)
            return HomeCategoryHolder(view)
        }

    }

    class HomeCategoryGridHolder(item: View) : RecyclerView.ViewHolder(item) {
        var txtCategory: TextView? = null
        var imgCategory: ImageView? = null
        var imgChecked: ImageView? = null

        init {
            txtCategory = item.findViewById(R.id.txtCategory)
            imgCategory = item.findViewById(R.id.iconCategory)
            imgChecked = item.findViewById(R.id.imgChecked)
        }
    }

    class HomeCategoryHolder(item: View) : RecyclerView.ViewHolder(item) {
        var txtCategory: TextView? = null
        var divider: View? = null
        var iconCategory:ImageView?=null

        init {
            txtCategory = item.findViewById(R.id.txtCategory)
            divider = item.findViewById(R.id.divider)
            iconCategory = item.findViewById(R.id.iconCategory)
        }
    }

    override fun onBindViewHolder(holder:RecyclerView.ViewHolder, position: Int) {
        if (viewType == GRID){
            val gridHolder = holder as HomeCategoryGridHolder
            val category = list[position]
            gridHolder.txtCategory?.text = category.name
            if (category.isMore) {
                gridHolder.imgCategory?.setImageResource(R.drawable.ic_more_horiz_black_24dp)
            } else {
                ImageHelper.getInstance().showImage(ctx, category.imagePath, gridHolder.imgCategory, 0, 0)
            }
            if(isCategoryFilter){
                gridHolder.imgChecked?.visibility = if(category.isSelected) View.VISIBLE else View.GONE
            }
            gridHolder.itemView.setOnClickListener {
                onClick(category)
            }
        }
        else{
            val listHolder = holder as HomeCategoryHolder
            val category = list[position]
            ImageHelper.getInstance().showImage(ctx, category.imagePath, listHolder.iconCategory, R.drawable.placeholder_image, R.drawable.placeholder_image)
            listHolder.txtCategory?.text = category.name
            listHolder.divider?.visibility = if (position == list.size-1) View.GONE else View.VISIBLE
            listHolder.itemView.setOnClickListener {
                onClick(category)
            }
        }
    }

    override fun getItemCount(): Int = list.count()
}