package tradly.social.common.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.databinding.ViewDataBinding
import tradly.social.common.base.BR
import tradly.social.common.base.GenericAdapter
import tradly.social.common.base.databinding.ListItemSubscriptionBinding
import tradly.social.common.cache.CurrencyCache
import tradly.social.domain.entities.Subscription
import tradly.social.domain.entities.SubscriptionProduct

class SubscriptionAdapter:GenericAdapter<SubscriptionProduct>() {

    var onItemClickItemListener:OnClickItemListListener<SubscriptionProduct>?=null
    private val currency = CurrencyCache.getDefaultCurrency()

    override fun getViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): GenericViewHolder<SubscriptionProduct> {
        return GenericViewHolder(ListItemSubscriptionBinding.inflate(inflater,parent,false))
    }

    override fun onBindData(
        binding: ViewDataBinding,
        value: SubscriptionProduct,
        position: Int,
        itemViewType: Int
    ) {
        binding.apply {
            setVariable(BR.currency,currency)
            setVariable(BR.listItem,value)
            setVariable(BR.onClickItemListener,this@SubscriptionAdapter.onItemClickItemListener)
        }
    }
}