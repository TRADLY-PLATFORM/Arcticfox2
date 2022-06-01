package tradly.social.common.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import tradly.social.common.base.BR
import tradly.social.common.base.GenericAdapter
import tradly.social.common.base.R
import tradly.social.domain.entities.Variant
import java.lang.Exception

class VariantAdapter:GenericAdapter<Variant>() {

    var onClickListener: OnClickItemListener<Variant>?=null
    private var viewType: Int = VIEW_TYPE.VIEW_VARIANT

    object VIEW_TYPE {
        const val ADD_VARIANT = 1
        const val VIEW_VARIANT = 2
    }

    override fun getViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): GenericViewHolder<Variant> {
        val binding =  when(viewType){
            VIEW_TYPE.ADD_VARIANT -> DataBindingUtil.inflate<ViewDataBinding>(inflater, R.layout.layout_variant_list_item, parent, false)
            VIEW_TYPE.VIEW_VARIANT -> DataBindingUtil.inflate(inflater, R.layout.list_item_variant_detail_view, parent, false)
            else -> throw Exception("Unknown VIEW_TYPE")
        }
        return GenericViewHolder(binding)
    }

    override fun onBindData(
        binding: ViewDataBinding,
        value: Variant,
        position: Int,
        itemViewType: Int
    ) {
       when(itemViewType){
           VIEW_TYPE.ADD_VARIANT -> onBindAddVariant(binding, value, position)
           VIEW_TYPE.VIEW_VARIANT -> onBindViewVariant(binding,value, position)
       }
    }

    private fun onBindAddVariant(binding: ViewDataBinding,
                                 value: Variant,
                                 position: Int){
        binding.apply {
            setVariable(BR.listItem,value)
            setVariable(BR.onClickItemListListener,object :OnClickItemListListener<Variant>{
                override fun onClick(value: Variant, view: View) {
                    onClickListener?.onClick(value, view, position)
                }
            })
        }
    }

    private fun onBindViewVariant(binding: ViewDataBinding,
                                  value: Variant,
                                  position: Int){
        binding.apply {
            setVariable(BR.listItem,value)
            setVariable(BR.onClickItemListListener,object :OnClickItemListListener<Variant>{
                override fun onClick(value: Variant, view: View) {
                    onClickListener?.onClick(value, view, position)
                }
            })
            setVariable(BR.selectedVariantValue, value.variantValues.joinToString("/") { it.variantValue })
        }
    }

     fun setViewType(viewType:Int){
        this.viewType = viewType
    }

    override fun getItemViewType(position: Int): Int {
        return viewType
    }

}