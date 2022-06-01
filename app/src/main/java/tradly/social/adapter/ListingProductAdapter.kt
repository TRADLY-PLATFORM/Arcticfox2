package tradly.social.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.siyamed.shapeimageview.RoundedImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tradly.social.R
import tradly.social.common.base.*
import tradly.social.common.network.feature.common.datasource.ProductDataSourceImpl
import tradly.social.domain.entities.Constant
import tradly.social.domain.entities.Product
import tradly.social.domain.repository.ProductRepository
import tradly.social.domain.usecases.GetProducts
import tradly.social.common.base.BaseActivity
import tradly.social.ui.login.AuthenticationActivity
import tradly.social.ui.product.addProduct.productDetail.ProductDetailActivity


class ListingProductAdapter(
    var ctx: Context, var list: MutableList<Product>,
    val recyclerView: RecyclerView?, var isMyStoreListing:Boolean = false, var isFor:Int = AppConstant.ListingType.GRID_LIST, var onClick: ((position: Int) -> Unit)? = null) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val inflater: LayoutInflater
    private var totalItemCount = 0
    private var lastVisibleItem = 0
    private val visibleThreshold = 5
    private val colorPrimary:Int
    var loading: Boolean = false
    var getProducts:GetProducts?=null
    private var OnLoadMoreListener: OnLoadMoreListener? = null

    init {
        inflater = LayoutInflater.from(ctx)
        colorPrimary = ThemeUtil.getResourceValue(ctx, R.attr.colorPrimary)
        val parseProductDataSource = ProductDataSourceImpl()
        val productRepository = ProductRepository(parseProductDataSource)
        getProducts = GetProducts(productRepository)
        setScrolling()
    }

    private fun setScrolling(){
        if (recyclerView?.layoutManager is LinearLayoutManager) {
            val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int, dy: Int
                ) {
                    super.onScrolled(recyclerView, dx, dy)
                    if(dy>0){
                        totalItemCount = linearLayoutManager.itemCount
                        lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
                        if (!loading && totalItemCount <= lastVisibleItem + visibleThreshold) {
                            // End has been reached
                            OnLoadMoreListener?.onLoadMore()
                            loading = true
                        }
                    }
                }
            })
        }
    }

    fun setLoaded(loading: Boolean) {
        this.loading = loading
    }

    fun setOnLoadMoreListener(listener: OnLoadMoreListener) {
        this.OnLoadMoreListener = listener
    }

    class ListingViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var imgProduct: ImageView? = null
        var txtProductName: TextView? = null
        var txtFinalPrice: TextView? = null
        var txtPrice: TextView? = null
        var txtOffer: TextView? = null
        var actionLayout:FrameLayout? = null
        var iconAction:ImageView?=null
        var layoutSold:FrameLayout?=null
        var txtProductStatus: TextView

        init {
            imgProduct = item.findViewById(R.id.imgProduct)
            txtProductName = item.findViewById(R.id.txtProductName)
            txtFinalPrice = item.findViewById(R.id.txtFinalPrice)
            txtPrice = item.findViewById(R.id.txtPrice)
            txtOffer = item.findViewById(R.id.txtOffer)
            actionLayout = item.findViewById(R.id.actionLayout)
            iconAction = item.findViewById(R.id.iconAction)
            layoutSold = item.findViewById(R.id.layoutSold)
            txtProductStatus = item.findViewById(R.id.textStatus)
        }
    }

    class WishListHolder(item: View) : RecyclerView.ViewHolder(item) {
        var imgProduct: RoundedImageView? = null
        var txtProductName: TextView? = null
        var txtFinalPrice: TextView? = null
        var txtPrice: TextView? = null
        var txtOffer: TextView? = null
        var actionLayout: FrameLayout? = null
        var iconAction: ImageView? = null
        var txtSold: TextView? = null

        init {
            imgProduct = item.findViewById(R.id.imgProduct)
            txtProductName = item.findViewById(R.id.txtProductName)
            txtFinalPrice = item.findViewById(R.id.txtFinalPrice)
            txtPrice = item.findViewById(R.id.txtPrice)
            txtOffer = item.findViewById(R.id.txtOffer)
            actionLayout = item.findViewById(R.id.actionLayout)
            iconAction = item.findViewById(R.id.iconAction)
            txtSold = item.findViewById(R.id.txtSoldOut)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
       return when(viewType){
           AppConstant.ListingType.WISH_LIST->{
               val view = inflater.inflate(R.layout.list_item_horz_list, parent, false)
               WishListHolder(view)
           }
           AppConstant.ListingType.GRID_LIST-> {
               val view = inflater.inflate(R.layout.list_item_grid_product, parent, false)
               ListingViewHolder(view)
           }
           else->{
               val view = inflater.inflate(R.layout.list_item_grid_product, parent, false)
               ListingViewHolder(view)
           }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType){
            AppConstant.ListingType.GRID_LIST->bindViewProduct(holder as? ListingViewHolder, list, position)
            AppConstant.ListingType.WISH_LIST-> bindViewWishList(holder as? WishListHolder, list, position)
        }
    }


    private fun bindViewProduct(holder: ListingViewHolder?, list: List<Product>, position: Int) {
        val product = list[position]
        holder?.txtProductName?.text= product.title
        if(product.offerPercent ==0 || AppConfigHelper.getConfigKey(AppConfigHelper.Keys.HIDE_OFFER_PERCENT)){
            holder?.txtFinalPrice?.text= product.displayPrice
            holder?.txtFinalPrice?.visibility =  View.VISIBLE
            holder?.txtPrice?.visibility =  View.GONE
            holder?.txtOffer?.visibility = View.GONE
        }
        else{
            holder?.txtPrice?.visibility =  View.VISIBLE
            holder?.txtOffer?.visibility = View.VISIBLE
            holder?.txtFinalPrice?.visibility =  View.VISIBLE
            holder?.txtPrice?.text = product.displayPrice
            holder?.txtFinalPrice?.text = product.displayOffer
            holder?.txtPrice?.let { it.paintFlags = it.paintFlags or  Paint.STRIKE_THRU_TEXT_FLAG }
            holder?.txtOffer?.text = String.format(ctx.getString(R.string.off_data),product.offerPercent.toString())
        }
        var productImage = Constant.EMPTY
        if(product.images.count()>0){
            productImage = product.images[0]
        }
        ImageHelper.getInstance().loadThumbnailThenMain(ctx,productImage,holder?.imgProduct,R.drawable.placeholder_image,R.drawable.placeholder_image)
        holder?.itemView?.setOnClickListener {
            val intent = Intent(ctx,ProductDetailActivity::class.java)
            intent.putExtra("productId",product.id)
            if(isMyStoreListing){
                (ctx as? BaseActivity)?.startActivityForResult(intent, AppConstant.ActivityResultCode.REFRESH_LISTING_IN_STORE_DETAIL)
            }
            else{
                ctx.startActivity(intent)
            }
        }
        holder?.layoutSold?.visibility = if(!product.inStock || !product.isActive) View.VISIBLE else View.GONE
        if(!product.inStock){
            holder?.txtProductStatus?.text = ctx.getString(R.string.product_sold_out)
            holder?.layoutSold?.setBackgroundResource(R.drawable.sold_out_bg)
        }else if(!product.isActive){
            holder?.txtProductStatus?.text = ctx.getString(R.string.productlist_under_review)
            holder?.layoutSold?.setBackgroundResource(R.drawable.under_review_bg)
        }
        holder?.actionLayout?.visibility = View.VISIBLE
        holder?.iconAction?.setImageResource(if(isMyStoreListing) R.drawable.ic_more else if(product.isLiked)R.drawable.ic_favorite_black_24dp else R.drawable.ic_favorite_border_black_24dp)
        if(!isMyStoreListing){
            holder?.iconAction?.setColorFilter(ContextCompat.getColor(ctx, colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN)
        }
        holder?.iconAction?.visibility = if(isMyStoreListing)View.GONE else View.VISIBLE
        holder?.actionLayout?.safeClickListener {
           if(!isMyStoreListing){
               likeAction(holder.adapterPosition)
           }
            else{
               onClick?.let { it(holder.adapterPosition) }
           }
        }
    }

    private fun bindViewWishList(holder: WishListHolder?, list: List<Product>, position: Int) {
        val product = list[position]
        holder?.txtProductName?.text= product.title

        if(product.offerPercent==0 || AppConfigHelper.getConfigKey(AppConfigHelper.Keys.HIDE_OFFER_PERCENT)){
            holder?.txtFinalPrice?.text = product.displayPrice
            holder?.txtFinalPrice?.visibility = View.VISIBLE
            holder?.txtOffer?.visibility = View.GONE
            holder?.txtPrice?.visibility = View.GONE
        }
        else{
            holder?.txtFinalPrice?.text = product.displayOffer
            holder?.txtPrice?.text= product.displayPrice
            holder?.txtOffer?.visibility = View.VISIBLE
            holder?.txtPrice?.visibility = View.VISIBLE
            holder?.txtFinalPrice?.visibility = View.VISIBLE
            holder?.txtOffer?.text = String.format(ctx.getString(R.string.off_data),product.offerPercent.toString())
            holder?.txtPrice?.let { it.paintFlags = it.paintFlags or  Paint.STRIKE_THRU_TEXT_FLAG }
        }
        var productImage = Constant.EMPTY
        if(product.images.count()>0){
            productImage = product.images[0]
        }
        ImageHelper.getInstance().loadThumbnailThenMain(ctx,productImage,holder?.imgProduct,R.drawable.placeholder_image,R.drawable.placeholder_image)
        holder?.itemView?.setOnClickListener {
            val intent = Intent(ctx,ProductDetailActivity::class.java)
            intent.putExtra("productId",product.id)
            if(isMyStoreListing){
                (ctx as? BaseActivity)?.startActivityForResult(intent, AppConstant.ActivityResultCode.REFRESH_LISTING_IN_STORE_DETAIL)
            }
            else{
                ctx.startActivity(intent)
            }
        }
        holder?.txtSold?.visibility = if(product.inStock) View.GONE else View.VISIBLE
        holder?.actionLayout?.visibility = View.VISIBLE
        holder?.iconAction?.setImageResource(if(isMyStoreListing) R.drawable.ic_more else if(product.isLiked)R.drawable.ic_favorite_black_24dp else R.drawable.ic_favorite_border_black_24dp)
        if(!isMyStoreListing){
            holder?.iconAction?.setColorFilter(ContextCompat.getColor(ctx, colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN)
        }
        holder?.actionLayout?.setOnClickListener {
            if(!isMyStoreListing){
                likeAction(holder.adapterPosition)
            }
            else{
                onClick?.let { it(holder.adapterPosition) }
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        return isFor
    }
    override fun getItemCount(): Int {
        return list.count()
    }

    @Synchronized
    fun likeAction(pos:Int){
        if(NetworkUtil.isConnectingToInternet()){
            if(AppController.appController.getUser() != null){
                val productItem = list[pos]
                productItem.isLiked = !productItem.isLiked
                likeListing(productItem.id,productItem.isLiked)
                when(isFor){
                    AppConstant.ListingType.WISH_LIST-> {
                        list.removeAt(pos)
                        notifyDataSetChanged()
                    }
                    AppConstant.ListingType.GRID_LIST-> notifyItemChanged(pos)
                }
                ctx.showToast(if(productItem.isLiked)R.string.wishlist_item_added_to_wish_list else R.string.wishlist_item_deleted_to_wish_list)
            }
            else{
                ctx.startActivity(AuthenticationActivity::class.java)
            }
        }
    }
    fun likeListing(productId:String,isForLike:Boolean){
        GlobalScope.launch(Dispatchers.IO) {
            getProducts?.likeProduct(productId,isForLike)
        }
    }

}