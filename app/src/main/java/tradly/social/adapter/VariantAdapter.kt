package tradly.social.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import tradly.social.R
import tradly.social.common.base.AppConstant
import tradly.social.common.CircleView
import tradly.social.common.base.ThemeUtil
import tradly.social.domain.entities.VariantProperties

class VariantAdapter(var ctx: Context,var isFor:Int, var list: List<VariantProperties>,var onClick:(pos:Int,obj:VariantProperties)->Unit) :
    RecyclerView.Adapter<VariantAdapter.VariantViewHolder>() {
    private val inflater: LayoutInflater
    private val colorPrimary: Int
    init {
        inflater = LayoutInflater.from(ctx)
        colorPrimary = ThemeUtil.getResourceValue(ctx, R.attr.colorPrimary)
    }

    class VariantViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var vTypeLayout:FrameLayout?=null
        var txtValue:TextView?=null
        var colorIndicator:CircleView?=null
        init {
            vTypeLayout = item.findViewById(R.id.vTypeLayout)
            txtValue = item.findViewById(R.id.txtValue)
            colorIndicator = item.findViewById(R.id.colorIndicator)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VariantViewHolder {
        val view = inflater.inflate(R.layout.list_item_variant, parent, false)
        return VariantViewHolder(view)
    }

    override fun onBindViewHolder(holder: VariantViewHolder, position: Int) {
        val item = list[position]
        holder.colorIndicator?.visibility = View.GONE
        holder.vTypeLayout?.visibility = View.VISIBLE
        holder.txtValue?.text = item.variantValue
       if(isFor == AppConstant.ListingType.VARIANT){
           if(item.isSelected){
               holder.txtValue?.setTextColor(ContextCompat.getColor(ctx,colorPrimary))
               holder.vTypeLayout?.setBackgroundResource(R.drawable.bg_variant_item)
           }
           else {
               holder.txtValue?.setTextColor(ContextCompat.getColor(ctx,R.color.colorTextBlack))
               holder.vTypeLayout?.setBackgroundResource(R.drawable.bg_variant_item_grey)
           }
           holder.itemView.setOnClickListener {
               val pos = holder.adapterPosition
               resetVariant()
               list[pos].isSelected = true
               notifyDataSetChanged()
               onClick(pos,item)
           }
       }
        else{
           holder.txtValue?.setTextColor(ContextCompat.getColor(ctx,R.color.colorTextBlack))
           holder.vTypeLayout?.setBackgroundResource(R.drawable.bg_set_variant)
       }

    }

    private fun resetVariant(){
        list.forEach {
            it.isSelected = false
        }
    }
    override fun getItemCount(): Int {
        return list.count()
    }
}