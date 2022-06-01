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
import tradly.social.domain.entities.GroupType

class GroupTypeGridAdapter(var ctx: Context, var list: List<GroupType>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val inflater: LayoutInflater
    init {
        inflater = LayoutInflater.from(ctx)
    }
    class GroupTypeViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var txtType:TextView?=null
        var icon:ImageView?=null
        var imgChecked:ImageView?=null
        init {
            txtType = item.findViewById(R.id.txtType)
            icon = item.findViewById(R.id.icon)
            imgChecked = item.findViewById(R.id.imgChecked)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = inflater.inflate(R.layout.list_item_group_type, parent, false)
        return GroupTypeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]
        val groupTypeViewHolder = holder as? GroupTypeViewHolder
        groupTypeViewHolder?.imgChecked?.visibility = if(item.isSelected) View.VISIBLE else View.GONE
        groupTypeViewHolder?.txtType?.text = item.name
        ImageHelper.getInstance().showImage(ctx,item.groupTypeImage,groupTypeViewHolder?.icon,R.drawable.placeholder_image,R.drawable.placeholder_image)
        groupTypeViewHolder?.itemView?.setOnClickListener {
            list.forEach {
                it.isSelected = false
            }
            list[groupTypeViewHolder.adapterPosition]?.isSelected = true
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    fun getSelectedType():GroupType?{
        return list.filter { i->i.isSelected }.singleOrNull()
    }
}