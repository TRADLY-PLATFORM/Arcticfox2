package tradly.social.common.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

abstract class GenericAdapter<T>: RecyclerView.Adapter<GenericAdapter.GenericViewHolder<T>>() {

    private lateinit var onLoadMoreListener: OnLoadMoreListener

    var items: MutableList<T> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder<T> {
        return getViewHolder( LayoutInflater.from(parent.context),parent, viewType)
    }

    abstract fun getViewHolder(inflater: LayoutInflater,parent: ViewGroup, viewType: Int): GenericViewHolder<T>

    abstract fun onBindData(binding: ViewDataBinding,value:T,position: Int,itemViewType: Int)

    class GenericViewHolder<T>(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: GenericViewHolder<T>, position: Int) {
        onBindData(holder.binding,items[position],holder.adapterPosition,holder.itemViewType)
        holder.binding.executePendingBindings()
    }

    override fun getItemCount() = items.size

    fun updateList(list:List<T>){
        val index = this.items.size
        this.items.addAll(list)
        notifyItemRangeInserted(index,items.size)
    }

    interface OnClickItemListener<T>{
        fun onClick(value:T, view: View, itemPosition: Int)
    }

    interface OnClickItemListListener<T>{
        fun onClick(value:T, view: View)
    }

}