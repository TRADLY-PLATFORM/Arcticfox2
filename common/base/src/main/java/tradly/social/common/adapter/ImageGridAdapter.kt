package tradly.social.common.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import tradly.social.common.base.BR
import tradly.social.common.base.GenericAdapter
import tradly.social.common.base.R
import tradly.social.domain.entities.ImageFeed

class ImageGridAdapter : GenericAdapter<ImageFeed>() {

    var onClickListener:OnClickItemListener<ImageFeed>?=null

    override fun getViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): GenericViewHolder<ImageFeed> {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            inflater,
            R.layout.list_item_add_image,
            parent,
            false
        )
        return GenericViewHolder(binding)
    }

    override fun onBindData(
        binding: ViewDataBinding,
        value: ImageFeed,
        position: Int,
        itemViewType: Int
    ) {
        binding.setVariable(BR.imageFeed, items[position])
        binding.setVariable(BR.closeIconVisible, !value.isAddItem)
        binding.setVariable(BR.adapterPosition,position)
        binding.setVariable(BR.onClickItemListener, object : OnClickItemListener<ImageFeed> {
            override fun onClick(value: ImageFeed, view: View, itemPosition: Int) {
                onClickListener?.onClick(value, view, position)
            }
        })
    }

    fun getFilePathList(): MutableList<ImageFeed> {
        return this.items
    }

    override fun getItemCount(): Int {
        return this.items.size
    }
}