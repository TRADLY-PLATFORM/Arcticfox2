package tradly.social.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import tradly.social.R
import tradly.social.common.base.ImageHelper
import tradly.social.common.network.parseHelper.ParseConstant
import tradly.social.domain.entities.Constant
import tradly.social.domain.entities.Group
import tradly.social.domain.entities.Product
import tradly.social.domain.entities.Store
import tradly.social.ui.group.GroupDetailActivity
import tradly.social.ui.product.addProduct.productDetail.ProductDetailActivity
import tradly.social.ui.store.storeDetail.StoreDetailActivity
import com.github.siyamed.shapeimageview.mask.PorterShapeImageView
import de.hdodenhof.circleimageview.CircleImageView
import tradly.social.common.base.AppConfigHelper
import tradly.social.common.base.Utils


class HomeHorzAdapter(internal var ctx: Context, var list: List<Any>, var scopeType:Int ,var isForGroup: Boolean,val onClick:(position:Int)->Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val inflater: LayoutInflater
    init {
        inflater = LayoutInflater.from(ctx)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when(scopeType){
            ParseConstant.HOME_SCOPE.STORE,
            ParseConstant.HOME_SCOPE.GROUP->{
                val view = inflater.inflate(R.layout.layout_product_item_two, parent, false)
                HomeHorzViewHolder(view)
            }
            ParseConstant.HOME_SCOPE.FEATURED_PRODUCTS->{
                val view = inflater.inflate(R.layout.list_product_item_horz, parent, false)
                ProductViewHolder(view)
            }
            else->{
                val view = inflater.inflate(R.layout.layout_product_item_two, parent, false)
                HomeHorzViewHolder(view)
            }
        }

    class ProductViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var imgProduct: PorterShapeImageView? = null
        var txtProductName: TextView? = null
        var txtFinalPrice: TextView? = null
        var txtPrice: TextView? = null
        var txtOffer: TextView? = null

        init {
            imgProduct = item.findViewById(R.id.imgProduct)
            txtProductName = item.findViewById(R.id.txtProductName)
            txtFinalPrice = item.findViewById(R.id.txtFinalPrice)
            txtPrice = item.findViewById(R.id.txtPrice)
            txtOffer = item.findViewById(R.id.txtOffer)
        }
    }
    class HomeHorzViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var txtOne: TextView? = null
        var imageOne: ImageView? = null
        var txtTwo:TextView?= null
        var actionBtn:FrameLayout?= null
        var txtActionBtn:TextView?= null
        var imgTwo: CircleImageView?=null
        var parentLayout:ConstraintLayout?=null
        init {
            txtOne = item.findViewById(R.id.txtOne)
            imageOne = item.findViewById(R.id.imageOne)
            txtTwo = item.findViewById(R.id.txtTwo)
            actionBtn = item.findViewById(R.id.actionBtn)
            txtActionBtn = item.findViewById(R.id.txtActionBtn)
            imgTwo = item.findViewById(R.id.imgTwo)
            parentLayout = item.findViewById(R.id.parentLayout)
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(scopeType == ParseConstant.HOME_SCOPE.GROUP || scopeType == ParseConstant.HOME_SCOPE.STORE){
            bindViewGroup(holder as HomeHorzViewHolder,list,position)
        }
        else if(scopeType == ParseConstant.HOME_SCOPE.FEATURED_PRODUCTS){
          bindViewProduct(holder as ProductViewHolder, list,position)
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0,0,0,0)
        if (position == 0) {
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(Utils.getPixel(ctx,10),0,0,0)
           // holder.itemView.layoutParams = params
        } else if (position == list.count() - 1) {
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0,0, Utils.getPixel(ctx,10),0)
            //holder.itemView.layoutParams = params
        }

    }

    private fun bindViewGroup(holder: HomeHorzViewHolder, list: List<Any>, position: Int){
        if (scopeType == ParseConstant.HOME_SCOPE.STORE) {
            val item = list[position] as Store
            holder.txtOne?.text = item.storeName
            holder.imgTwo?.visibility = View.VISIBLE
            holder.txtTwo?.visibility = View.GONE
            holder.imgTwo?.visibility = View.GONE
            if(!item.webAddress.isNullOrEmpty()){
                holder.txtTwo?.visibility = View.VISIBLE
                holder.txtTwo?.text = item.webAddress
            }

            holder.txtActionBtn?.text = ctx.getString(R.string.home_follow)
            holder.txtActionBtn?.visibility = View.VISIBLE
            holder.actionBtn?.setOnClickListener {
                onClick(holder.adapterPosition)
            }
            ImageHelper.getInstance().loadThumbnailThenMain(ctx,
                item.storePic,
                holder.imageOne,
                R.drawable.placeholder_image,
                R.drawable.placeholder_image
            )
            if(!item.user?.profilePic.isNullOrEmpty()){
                ImageHelper.getInstance().showImage(ctx,
                    item.user?.profilePic,
                    holder.imgTwo,
                    R.drawable.placeholder_image,
                    R.drawable.placeholder_image
                )
            }
            holder.parentLayout?.setOnClickListener {
                val intent = Intent(ctx, StoreDetailActivity::class.java)
                intent.putExtra("storeName",item.storeName)
                intent.putExtra("id",item.id)
                ctx.startActivity(intent)
            }
        }
        else if(scopeType == ParseConstant.HOME_SCOPE.GROUP){
            val item = list[position] as? Group
            holder.txtOne?.text = item?.groupName
            holder.imgTwo?.visibility = View.GONE
            holder.txtTwo?.text = String.format(ctx.getString(R.string.group_members),item?.membersCount)
            holder.txtActionBtn?.text = ctx.getString(R.string.group_join)
            holder.actionBtn?.setOnClickListener {
                onClick(holder.adapterPosition)
            }
            ImageHelper.getInstance().showImage(
                ctx,
                item?.groupPic,
                holder.imageOne,
                R.drawable.placeholder_image,
                R.drawable.placeholder_image
            )
            holder.parentLayout?.setOnClickListener {
                val intent = Intent(ctx,GroupDetailActivity::class.java)
                intent.putExtra("groupId",item?.id)
               // ctx.startActivity(intent)
            }
        }
    }

    private fun bindViewProduct(holder: ProductViewHolder?, list: List<Any>, position: Int) {
        val product = list[position] as Product
        holder?.txtProductName?.text= product.title

        if(product.offerPercent == 0 || AppConfigHelper.getConfigKey(AppConfigHelper.Keys.HIDE_OFFER_PERCENT)){
            holder?.txtFinalPrice?.text = product.displayPrice
            holder?.txtFinalPrice?.visibility = View.VISIBLE
            holder?.txtOffer?.visibility = View.INVISIBLE
            holder?.txtPrice?.visibility = View.INVISIBLE

        }
        else
        {
            holder?.txtOffer?.visibility = View.VISIBLE
            holder?.txtPrice?.visibility = View.VISIBLE
            holder?.txtFinalPrice?.visibility = View.VISIBLE
            holder?.txtFinalPrice?.text = product.displayOffer
            holder?.txtOffer?.text = String.format(ctx.getString(R.string.off_data),product.offerPercent.toString())
            holder?.txtPrice?.text= product.displayPrice
            holder?.txtPrice?.let {
                it.paintFlags = it.paintFlags or  Paint.STRIKE_THRU_TEXT_FLAG
            }
        }


        var productImage = Constant.EMPTY
        if(product.images.count()>0){
            productImage = product.images[0]
        }
        ImageHelper.getInstance().loadThumbnailThenMain(ctx,productImage,holder?.imgProduct,R.drawable.placeholder_image,R.drawable.placeholder_image)
        holder?.itemView?.setOnClickListener {
            val intent = Intent(ctx, ProductDetailActivity::class.java)
            intent.putExtra("productId",product.id)
            ctx.startActivity(intent)
        }

    }

    override fun getItemCount(): Int = list.count()

}