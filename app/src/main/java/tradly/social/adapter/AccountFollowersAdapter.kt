package tradly.social.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import tradly.social.BR
import tradly.social.common.base.GenericAdapter
import tradly.social.databinding.ListItemAccountFollowerBinding
import tradly.social.domain.entities.User

class AccountFollowersAdapter:GenericAdapter<User>() {
    override fun getViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): GenericViewHolder<User> {
        return GenericViewHolder(ListItemAccountFollowerBinding.inflate(inflater,parent,false))
    }

    override fun onBindData(
        binding: ViewDataBinding,
        value: User,
        position: Int,
        itemViewType: Int
    ) {
        binding.setVariable(BR.listItem,value)
    }


}