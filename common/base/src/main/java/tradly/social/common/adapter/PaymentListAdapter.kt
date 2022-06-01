package tradly.social.common.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import tradly.social.common.base.BR
import tradly.social.common.base.GenericAdapter
import tradly.social.common.base.R
import tradly.social.domain.entities.Payment

class PaymentListAdapter:GenericAdapter<Payment>() {

    lateinit var onClickItemListener: OnClickItemListener<Payment>

    override fun getViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): GenericViewHolder<Payment> {
        val binding =  DataBindingUtil.inflate<ViewDataBinding>(inflater, R.layout.list_item_payment, parent, false)
        return GenericViewHolder(binding)
    }

    override fun onBindData(binding: ViewDataBinding, value: Payment, position: Int, itemViewType: Int) {
        binding.apply {
            setVariable(BR.payment,value)
            setVariable(BR.onClickItemListListener,object : OnClickItemListListener<Payment>{
                override fun onClick(value: Payment, view: View) {
                    onClickItemListener.onClick(value,view,position)
                }
            })
        }
    }

}