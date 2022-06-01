package tradly.social.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tradly.social.R
import tradly.social.common.base.ImageHelper
import tradly.social.common.base.ThemeUtil
import tradly.social.domain.entities.User
import de.hdodenhof.circleimageview.CircleImageView

class HorizontalMemberAdapter(var ctx: Context, var list: List<User>) :
    RecyclerView.Adapter<HorizontalMemberAdapter.HorizontalMemberViewHolder>() {

    private val inflater: LayoutInflater
    private val iconTintColor: Int by lazy { ThemeUtil.getResourceValue(ctx, R.attr.colorPrimary) }
    init {
        inflater = LayoutInflater.from(ctx)
    }

    class HorizontalMemberViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var image:CircleImageView?=null
        init {
            image = item.findViewById(R.id.paymentStatusImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalMemberViewHolder {
        val view = inflater.inflate(R.layout.list_item_member, parent, false)
        return HorizontalMemberViewHolder(view)
    }


    override fun onBindViewHolder(holder: HorizontalMemberViewHolder, position: Int) {
        ImageHelper.getInstance().showImage(ctx,list[position].profilePic,holder.image,R.drawable.ic_user_placeholder,R.drawable.ic_user_placeholder)
    }

    override fun getItemCount(): Int {
        return list.count()
    }
}