package tradly.social.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tradly.social.R
import tradly.social.common.base.AppConstant
import tradly.social.common.base.ImageHelper
import tradly.social.common.base.NetworkUtil
import tradly.social.common.base.ThemeUtil
import tradly.social.common.network.parseHelper.ParseConstant
import tradly.social.common.base.setImageByUrl
import tradly.social.domain.entities.Collection
;
import tradly.social.ui.category.CategoryListActivity
import tradly.social.ui.groupListing.ListingActivity

class HomeAdapter(var ctx: Context, val list: ArrayList<Collection>, var callBack: (position: Int, scopeType: Int,obj:Any) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val inflater: LayoutInflater
    private val colorPrimary: Int
    private val viewAllBg: Int

    init {
        inflater = LayoutInflater.from(ctx)
        colorPrimary = ThemeUtil.getResourceValue(ctx, R.attr.colorPrimary)
        viewAllBg = ThemeUtil.getResourceDrawable(ctx, R.attr.buttonGradientBg)
    }

    object Background {
        const val THEME = "theme"
        const val WHITE = "white"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            ParseConstant.HOME_SCOPE.STORE,
            ParseConstant.HOME_SCOPE.GROUP -> {
                val view = inflater.inflate(R.layout.layout_widget_horz_theme, parent, false)
                HorZThemeViewHolder(view)
            }
            ParseConstant.HOME_SCOPE.INVITE_FRIENDS -> {
                val view = inflater.inflate(R.layout.layout_home_invite, parent, false)
                InviteHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.layout_widget_horz_theme, parent, false)
                HorZThemeViewHolder(view)
            }
        }

    class HorZThemeViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var title: TextView? = null
        var viewAllBtn: FrameLayout? = null
        var horzRecyclerView: RecyclerView? = null
        var topView: View? = null
        var bottomView: View? = null
        var parentLayout: ConstraintLayout? = null
        var txtViewAll: TextView? = null
        var topImageView: ImageView? = null

        init {
            title = item.findViewById(R.id.title)
            viewAllBtn = item.findViewById(R.id.btnViewAll)
            horzRecyclerView = item.findViewById(R.id.recycler_view)
            topView = item.findViewById(R.id.topView)
            bottomView = item.findViewById(R.id.bottomView)
            parentLayout = item.findViewById(R.id.parentLayout)
            txtViewAll = item.findViewById(R.id.txtViewAll)
            topImageView = item.findViewById(R.id.topImageView)
        }
    }


    class InviteHolder(item: View) : RecyclerView.ViewHolder(item) {
        var txtTitle: TextView? = null
        var inviteAll: FrameLayout? = null
        var txtDesc: TextView? = null
        var ivLogo:ImageView?=null

        init {
            txtTitle = item.findViewById(R.id.txtInviteOne)
            inviteAll = item.findViewById(R.id.btnInvite)
            txtDesc = item.findViewById(R.id.txtInviteTwo)
            ivLogo = item.findViewById(R.id.imgLogo)

        }
    }

    private fun bindInviteView(holder: InviteHolder?, list: List<Collection>, position: Int) {
        val item = list[position]
        holder?.txtTitle?.text = item.title
        holder?.txtDesc?.text = item.description
        holder?.ivLogo?.setImageByUrl(ctx,item.imagePath,R.drawable.ic_home_invite,R.drawable.ic_home_invite)
        holder?.inviteAll?.setOnClickListener {
            callBack(holder.adapterPosition, ParseConstant.HOME_SCOPE.INVITE_FRIENDS,item)
        }
    }

    private fun bindHorZTheme(holder: HorZThemeViewHolder?, list: ArrayList<Collection>, position: Int) {
        val item = list[position]
        holder?.title?.text = item.title
        holder?.viewAllBtn?.visibility = View.VISIBLE //TODO Need to remove
        holder?.viewAllBtn?.setOnClickListener {
            if(ParseConstant.HOME_SCOPE.GROUP == list[position].scopeType){
                val intent = Intent(ctx,ListingActivity::class.java)
                intent.putExtra("isFor", AppConstant.ListingType.GROUP_FOLLOW)
                intent.putExtra("toolbarTitle",R.string.group)
                //ctx.startActivity(intent)

            }
            else if(ParseConstant.HOME_SCOPE.STORE == list[position].scopeType){
                val intent = Intent(ctx,ListingActivity::class.java)
                intent.putExtra("isFor", AppConstant.ListingType.STORE_LIST)
                intent.putExtra(AppConstant.BundleProperty.COLLECTION_ID,item.id)
                intent.putExtra("toolbarTitle",R.string.storelist_header_title)
                ctx.startActivity(intent)
            }
            else if(ParseConstant.HOME_SCOPE.FEATURED_PRODUCTS==list[position].scopeType){
                val intent = Intent(ctx, CategoryListActivity::class.java)
                intent.putExtra("isFor", AppConstant.FragmentTag.PRODUCT_LIST_FRAGMENT)
                intent.putExtra("categoryId", AppConstant.EMPTY)
                intent.putExtra(AppConstant.BundleProperty.COLLECTION_ID,item.id)
                intent.putExtra("categoryName",list[position].title)
                ctx.startActivity(intent)
            }
        }
        if (Background.WHITE.equals(item.backgroundColor)) {
            holder?.txtViewAll?.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
            holder?.title?.setTextColor(ContextCompat.getColor(ctx, R.color.colorHomeCategoryImageTwo))
            holder?.parentLayout?.setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorWhite))
            holder?.topView?.setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorWhite))
            holder?.viewAllBtn?.setBackgroundResource(viewAllBg)
        } else if (Background.THEME.equals(item.backgroundColor) || !item.backgroundColor.isEmpty()) {
            holder?.txtViewAll?.setTextColor(ContextCompat.getColor(ctx, colorPrimary))
            holder?.title?.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
            holder?.topImageView?.visibility = View.GONE
            holder?.parentLayout?.setBackgroundColor(ContextCompat.getColor(ctx, colorPrimary))
            holder?.topView?.setBackgroundColor(ContextCompat.getColor(ctx, colorPrimary))
            holder?.viewAllBtn?.setBackgroundResource(R.drawable.bg_button_white)
        } else {
            holder?.txtViewAll?.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
            holder?.title?.setTextColor(ContextCompat.getColor(ctx, R.color.colorHomeCategoryImageTwo))
            holder?.parentLayout?.setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorWhite))
            holder?.topView?.setBackgroundColor(ContextCompat.getColor(ctx, android.R.color.transparent))
            holder?.bottomView?.setBackgroundColor(ContextCompat.getColor(ctx, android.R.color.transparent))
            holder?.viewAllBtn?.setBackgroundResource(viewAllBg)
            holder?.horzRecyclerView?.setBackgroundColor(ContextCompat.getColor(ctx, android.R.color.transparent))
            ImageHelper.getInstance().showImage(
                ctx,
                item.backgroundUrl,
                holder?.topImageView,
                R.drawable.placeholder_image,
                R.drawable.placeholder_image
            )
        }
        holder?.horzRecyclerView?.layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)
        var adapter: HomeHorzAdapter? = null
        if (ParseConstant.HOME_SCOPE.STORE == holder?.itemViewType) {
            adapter = HomeHorzAdapter(ctx, item.stores, item.scopeType, false) { position ->
                if(NetworkUtil.isConnectingToInternet()){
                    callBack(position, item.scopeType,item.stores[position])
                    item.stores.removeAt(position)
                    adapter?.notifyItemRemoved(position)
                    adapter?.notifyItemRangeChanged(position,item.stores.count())
                    if(item.stores.count()==0){
                        list.removeAt(holder.adapterPosition)
                        notifyItemRemoved(holder.adapterPosition)
                        notifyItemRangeChanged(holder.adapterPosition,list.count())
                    }
                }
            }
        } else if (ParseConstant.HOME_SCOPE.GROUP == holder?.itemViewType) {
            adapter = HomeHorzAdapter(ctx, item.groups, item.scopeType, false) { position ->
                if(NetworkUtil.isConnectingToInternet()){
                    callBack(position, item.scopeType,item.groups[position])
                    item.groups.removeAt(position)
                    adapter?.notifyItemRemoved(position)
                    adapter?.notifyItemRangeChanged(position,item.groups.count())
                    if(item.groups.count()==0){
                        list.removeAt(holder.adapterPosition)
                        notifyItemRemoved(holder.adapterPosition)
                        notifyItemRangeChanged(holder.adapterPosition,list.count())
                    }
                }
            }
        } else if (ParseConstant.HOME_SCOPE.FEATURED_PRODUCTS == holder?.itemViewType || ParseConstant.HOME_SCOPE.TRENDING_PRODUCTS == holder?.itemViewType) {
            adapter = HomeHorzAdapter(ctx, item.products, item.scopeType, false) { position ->
                if(NetworkUtil.isConnectingToInternet()){
                    callBack(position, item.scopeType,item.products[position])
                    item.products.removeAt(position)
                    adapter?.notifyItemRemoved(position)
                    adapter?.notifyItemRangeChanged(position,item.products.count())
                    if(item.products.count()==0){
                        list.removeAt(holder.adapterPosition)
                        notifyItemRemoved(holder.adapterPosition)
                        notifyItemRangeChanged(holder.adapterPosition,list.count())
                    }
                }
            }
        }
        holder?.horzRecyclerView?.isNestedScrollingEnabled = false
        holder?.horzRecyclerView?.adapter = adapter
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ParseConstant.HOME_SCOPE.GROUP,
            ParseConstant.HOME_SCOPE.STORE,
            ParseConstant.HOME_SCOPE.FEATURED_PRODUCTS-> {
                bindHorZTheme(holder as? HorZThemeViewHolder, list, position)
            }
            ParseConstant.HOME_SCOPE.INVITE_FRIENDS -> {
                bindInviteView(holder as? InviteHolder, list, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun getItemViewType(position: Int): Int {
        var viewType: Int
        if (ParseConstant.HOME_SCOPE.GROUP == list[position].scopeType) {
            viewType = ParseConstant.HOME_SCOPE.GROUP
        } else if (ParseConstant.HOME_SCOPE.STORE == list[position].scopeType) {
            viewType = ParseConstant.HOME_SCOPE.STORE
        } else if (ParseConstant.HOME_SCOPE.INVITE_FRIENDS == list[position].scopeType) {
            viewType = ParseConstant.HOME_SCOPE.INVITE_FRIENDS
        } else {
            viewType = ParseConstant.HOME_SCOPE.FEATURED_PRODUCTS
        }
        return viewType
    }
}