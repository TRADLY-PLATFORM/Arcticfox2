package tradly.social.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.siyamed.shapeimageview.CircularImageView
import com.github.siyamed.shapeimageview.RoundedImageView
import com.github.siyamed.shapeimageview.mask.PorterShapeImageView
import com.google.gson.Gson
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tradly.social.R
import tradly.social.common.*
import tradly.social.common.base.DateTimeHelper.Companion.FORMAT_DATE_DD_MM_YYYY
import tradly.social.common.base.DateTimeHelper.Companion.FORMAT_TIME_AM_PM
import tradly.social.common.base.*
import tradly.social.common.util.parser.extension.toJson
import tradly.social.common.views.custom.CustomTextView
import tradly.social.common.uiEntity.Theme
import tradly.social.common.network.feature.common.datasource.ProductDataSourceImpl
import tradly.social.domain.entities.*
import tradly.social.domain.entities.Set
import tradly.social.domain.repository.ProductRepository
import tradly.social.domain.usecases.GetProducts
import tradly.social.ui.group.GroupDetailActivity
import tradly.social.ui.login.AuthenticationActivity
import tradly.social.ui.product.ImageViewerActivity
import tradly.social.ui.product.addProduct.productDetail.ProductDetailActivity
import tradly.social.ui.shipment.AddressListActivity
import tradly.social.ui.shipment.ManageShippingAddressActivity
import tradly.social.ui.shipment.ShippingPresenter
import tradly.social.ui.store.storeDetail.StoreDetailActivity
import java.io.File

class ListingAdapter(
    var ctx: Context,
    var list: List<Any>,
    var isFor: Int,
    val recyclerView: RecyclerView?,
    val click: (position: Int, obj: Any) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val inflater: LayoutInflater
    private val btnJoin: Int
    private val txtJoin: String
    private val txtJoined: String
    private val colorPrimary: Int
    private val colorSoupBubble:Int
    private val txtFollow: String
    private val txtFollowing: String
    private var totalItemCount = 0
    private var lastVisibleItem = 0
    private val visibleThreshold = 5
    private var currentUser:User? = null
    var isLoading: Boolean = false
    private var onLoadMoreListener: OnLoadMoreListener? = null

    val minQtyList: List<String> by lazy {
        ctx.resources.getStringArray(R.array.cart_qty_min).toList()
    }
    var isForPickupAddress: Boolean = false

    init {
        btnJoin = ThemeUtil.getResourceDrawable(ctx, R.attr.buttonGradientBg)
        colorPrimary = ThemeUtil.getResourceValue(ctx, R.attr.colorPrimary)
        colorSoupBubble = ThemeUtil.getResourceValue(ctx,R.attr.orderTabColor)
        inflater = LayoutInflater.from(ctx)
        txtJoin = ctx.getString(R.string.group_join)
        txtJoined = ctx.getString(R.string.group_joined)
        txtFollow = ctx.getString(R.string.storelist_follow)
        txtFollowing = ctx.getString(R.string.storelist_following)
        currentUser = AppController.appController.getUser()
        setScrolling()
        setSpanSizeLookup()
    }

    private fun setScrolling() {
        if (recyclerView != null && recyclerView.getLayoutManager() is LinearLayoutManager) {
            val linearLayoutManager = recyclerView.getLayoutManager() as LinearLayoutManager
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int, dy: Int
                ) {
                    super.onScrolled(recyclerView, dx, dy)
                    totalItemCount = linearLayoutManager.itemCount
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
                    if (!isLoading && totalItemCount <= lastVisibleItem + visibleThreshold) {
                        // End has been reached
                        onLoadMoreListener?.onLoadMore()
                        isLoading = true
                    }

                }
            })
        }
    }

    private fun setSpanSizeLookup(){
        if (isFor == AppConstant.ListingType.PRODUCT_LIST){
            recyclerView?.layoutManager?.let {
            (it as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup(){
                override fun getSpanSize(position: Int): Int {
                    return when(getItemViewType(position)){
                        AppConstant.ListingType.AD_VIEW-> it.spanCount
                        else -> 1
                    }
                }
            }
        }
        }
    }

    fun setLoaded() {
        isLoading = false
    }

    fun setOnLoadMoreListener(listener: OnLoadMoreListener) {
        this.onLoadMoreListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (AppConstant.ListingType.GROUP_TAG_PRODUCT == viewType) {
            val view = inflater.inflate(R.layout.list_item_group, parent, false)
            return GroupListingViewHolder(view)
        } else if (AppConstant.ListingType.GROUP_FOLLOW == viewType) {
            val view = inflater.inflate(R.layout.list_grid_item, parent, false)
            return GroupGridViewHolder(view)
        } else if (AppConstant.ListingType.STORE_LIST == viewType) {
            val view = inflater.inflate(R.layout.list_grid_item, parent, false)
            return StoreGridViewHolder(view)
        } else if (AppConstant.ListingType.CHAT_ROOM == viewType) {
            val view = inflater.inflate(R.layout.list_item_chat_room, parent, false)
            return ChatRoomViewHolder(view)
        } else if (AppConstant.ListingType.CART_LIST == viewType) {
            val view = inflater.inflate(R.layout.list_item_cart, parent, false)
            return CartViewHolder(view)
        } else if (AppConstant.ListingType.VARIANT == viewType || AppConstant.ListingType.SET_VARIANT_LIST == viewType) {
            val view = inflater.inflate(R.layout.list_item_variant_type, parent, false)
            return VariantViewHolder(view)
        } else if (AppConstant.ListingType.SPECIFICATION == viewType) {
            val view = inflater.inflate(R.layout.layout_item_specification, parent, false)
            return SpecificationViewHolder(view)
        } else if (AppConstant.ListingType.SET_LIST == viewType) {
            val view = inflater.inflate(R.layout.list_item_grid_product, parent, false)
            return SetViewHolder(view)
        } else if (AppConstant.ListingType.ADDRESS_LIST == viewType) {
            val view = inflater.inflate(R.layout.list_item_address, parent, false)
            return AddressViewHolder(view)
        } else if (AppConstant.ListingType.THEME_LIST == viewType) {
            val view = inflater.inflate(R.layout.layout_item_theme, parent, false)
            return ThemeViewHolder(view)
        } else if (AppConstant.ListingType.ACCOUNT_CHOOSER_LIST == viewType) {
            val view = inflater.inflate(R.layout.list_item_account_chooser, parent, false)
            return AccountViewHolder(view)
        } else if (AppConstant.ListingType.PAYMENT_TYPES == viewType) {
            val view = inflater.inflate(R.layout.list_item_payment, parent, false)
            return PaymentTypeHolder(view)
        } else if (AppConstant.ListingType.MY_ORDER_LIST == viewType) {
            val view = inflater.inflate(R.layout.list_item_my_order_details, parent, false)
            return ProductViewHolder(view)
        } else if (AppConstant.ListingType.ORDER_TIME_LINE_LIST == viewType) {
            val view = inflater.inflate(R.layout.list_item_order_time_line, parent, false)
            return OrderTimelineHolder(view)
        } else if (AppConstant.ListingType.NOTIFICATION_LIST == viewType) {
            val view = inflater.inflate(R.layout.list_item_notification, parent, false)
            return NotificationHolder(view)
        } else if (AppConstant.ListingType.CATEGORY_LIST == viewType) {
            val view = inflater.inflate(R.layout.list_item_subcategory, parent, false)
            return CategoryViewHolder(view)
        } else if (AppConstant.ListingType.LOCATION_SEARCH == viewType) {
            val view = inflater.inflate(R.layout.list_item_location_search, parent, false)
            return LocationViewHolder(view)
        } else if (AppConstant.ListingType.TRANSACTION_LIST == viewType) {
            val view = inflater.inflate(R.layout.list_item_transaction, parent, false)
            return TransactionViewHolder(view)
        } else if (AppConstant.ListingType.REVIEW_LIST == viewType) {
            val view = inflater.inflate(R.layout.list_item_review, parent, false)
            return ReviewViewHolder(view)
        } else if (AppConstant.ListingType.SHIPPING_METHOD_LIST == viewType || AppConstant.ListingType.SHIPMENT_METHOD_SELECT_LIST == viewType) {
            val view = inflater.inflate(R.layout.list_item_single_select, parent, false)
            return ShippingMethodHolder(view)
        }
        else if(AppConstant.ListingType.LOCALE_LIST == viewType){
            val view = inflater.inflate(R.layout.list_item_single_select, parent, false)
            return LocaleHolder(view)
        }
        else if(AppConstant.ListingType.STORE_FEEDS == viewType || AppConstant.ListingType.STORE_FEEDS_BLOCK == viewType){
            val view = inflater.inflate(R.layout.list_item_accounts_feeds, parent, false)
            return StoreFeedViewHolder(view)
        }
        else if (AppConstant.ListingType.ATTACHMENT_LIST == viewType) {
            val view = inflater.inflate(R.layout.list_item_attachmet, parent, false)
            return AttachmentHolder(view)
        }
        else if(AppConstant.ListingType.PRODUCT_LIST ==viewType){
            val view = inflater.inflate(R.layout.list_item_grid_product, parent, false)
            return ListingViewHolder(view,ViewGroup.LayoutParams.MATCH_PARENT,0)
        }
        if(AppConstant.ListingType.SIMILAR_PRODUCT_LIST ==viewType){
            val view = inflater.inflate(R.layout.list_item_grid_product, parent, false)
            return ListingViewHolder(view, Utils.getPixel(ctx,200), Utils.getPixel(ctx,8))
        }
        if(AppConstant.ListingType.AD_VIEW ==viewType){
            val view = inflater.inflate(R.layout.item_layout_ad, parent, false)
            return AdViewHolder(view)
        }
        else {
            val view = inflater.inflate(R.layout.list_item_group, parent, false)
            return GroupListingViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            AppConstant.ListingType.GROUP_TAG_PRODUCT -> bindViewGroup(
                holder as? GroupListingViewHolder,
                list,
                position
            )
            AppConstant.ListingType.GROUP_FOLLOW -> bindViewGroupGrid(
                holder as? GroupGridViewHolder,
                list,
                position
            )
            AppConstant.ListingType.STORE_LIST -> bindViewStoreGrid(
                holder as? StoreGridViewHolder,
                list,
                position
            )
            AppConstant.ListingType.CHAT_ROOM -> bindViewChatRoom(
                holder as? ChatRoomViewHolder,
                list,
                position
            )
            AppConstant.ListingType.CART_LIST -> bindViewCart(
                holder as CartViewHolder,
                list,
                position
            )
            AppConstant.ListingType.VARIANT, AppConstant.ListingType.SET_VARIANT_LIST -> bindViewVariant(
                holder as VariantViewHolder,
                list,
                position
            )
            AppConstant.ListingType.SET_LIST -> bindViewSetGrid(
                holder as SetViewHolder,
                list,
                position
            )
            AppConstant.ListingType.ADDRESS_LIST -> bindViewAddress(
                holder as AddressViewHolder,
                list,
                position
            )
            AppConstant.ListingType.THEME_LIST -> bindViewTheme(
                holder as ThemeViewHolder,
                list,
                position
            )
            AppConstant.ListingType.SPECIFICATION -> bindViewSpecification(
                holder as SpecificationViewHolder,
                list,
                position
            )
            AppConstant.ListingType.ACCOUNT_CHOOSER_LIST -> bindAccount(
                holder as AccountViewHolder,
                list,
                position
            )
            AppConstant.ListingType.PAYMENT_TYPES -> bindPaymentType(
                holder as PaymentTypeHolder,
                list,
                position
            )
            AppConstant.ListingType.MY_ORDER_LIST -> bindOrder(
                holder as ProductViewHolder,
                list[position] as OrderedProductEntity
            )
            AppConstant.ListingType.ORDER_TIME_LINE_LIST -> bindTimeline(
                holder as OrderTimelineHolder,
                list[position] as OrderStatusEntity,
                position
            )
            AppConstant.ListingType.NOTIFICATION_LIST -> bindNotification(
                holder as NotificationHolder,
                list[position] as Notification,
                position
            )
            AppConstant.ListingType.CATEGORY_LIST -> bindCategory(
                holder as CategoryViewHolder,
                list[position] as Category,
                position
            )
            AppConstant.ListingType.LOCATION_SEARCH -> bindLocationSearch(
                holder as LocationViewHolder,
                list[position] as Address,
                position
            )
            AppConstant.ListingType.TRANSACTION_LIST -> bindTransactionView(
                holder as TransactionViewHolder,
                list[position] as Transaction,
                position
            )
            AppConstant.ListingType.REVIEW_LIST -> bindReviews(
                holder as ReviewViewHolder,
                list[position] as Review
            )
            AppConstant.ListingType.SHIPPING_METHOD_LIST,
            AppConstant.ListingType.SHIPMENT_METHOD_SELECT_LIST -> bindShippingMethod(
                holder as ShippingMethodHolder,
                list[position] as ShippingMethod
            )
            AppConstant.ListingType.LOCALE_LIST-> bindLocale(holder as LocaleHolder,list[position] as Locale)
            AppConstant.ListingType.ATTACHMENT_LIST-> bindAttachment(holder as AttachmentHolder,list[position] as FileInfo)
            AppConstant.ListingType.PRODUCT_LIST,
            AppConstant.ListingType.SIMILAR_PRODUCT_LIST-> bindViewProduct(holder as ListingViewHolder,list[position] as Product)
            AppConstant.ListingType.STORE_FEEDS,
            AppConstant.ListingType.STORE_FEEDS_BLOCK-> bindStoreFeed( holder as StoreFeedViewHolder,list[position] as Store,position)
            AppConstant.ListingType.AD_VIEW -> bindViewAd(holder as AdViewHolder)
        }
    }

    class ChatRoomViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var chatProfile: CircleImageView? = null
        var receivedTime: TextView? = null
        var userName: TextView? = null
        var messageIcon: ImageView? = null
        var receivedCount: TextView? = null
        var lastMessage: TextView? = null
        var iconDeliveryStatus:ImageView?=null

        init {
            chatProfile = item.findViewById(R.id.chatProfile)
            receivedTime = item.findViewById(R.id.receivedTime)
            userName = item.findViewById(R.id.userName)
            messageIcon = item.findViewById(R.id.messageIcon)
            receivedCount = item.findViewById(R.id.receivedCount)
            lastMessage = item.findViewById(R.id.lastMessage)
            iconDeliveryStatus = item.findViewById(R.id.negotiationIconDeliveryStatus)
        }
    }

    class GroupGridViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var txtOne: TextView? = null
        var imageOne: CircularImageView? = null
        var txtTwo: TextView? = null
        var actionBtn: FrameLayout? = null
        var txtActionBtn: TextView? = null
        var imgTwo: CircleImageView? = null
        var parentLayout: ConstraintLayout? = null

        init {
            txtOne = item.findViewById(R.id.txtOne)
            imageOne = item.findViewById(R.id.imgOne)
            txtTwo = item.findViewById(R.id.txtTwo)
            actionBtn = item.findViewById(R.id.actionBtn)
            txtActionBtn = item.findViewById(R.id.txtActionBtn)
            imgTwo = item.findViewById(R.id.imgTwo)
            parentLayout = item.findViewById(R.id.parentLayout)

        }
    }

    class StoreGridViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var txtOne: TextView? = null
        var imageOne: CircularImageView? = null
        var txtTwo: TextView? = null
        var actionBtn: FrameLayout? = null
        var txtActionBtn: TextView? = null
        var imgTwo: CircleImageView? = null
        var parentLayout: ConstraintLayout? = null

        init {
            txtOne = item.findViewById(R.id.txtOne)
            imageOne = item.findViewById(R.id.imgOne)
            txtTwo = item.findViewById(R.id.txtTwo)
            actionBtn = item.findViewById(R.id.actionBtn)
            txtActionBtn = item.findViewById(R.id.txtActionBtn)
            imgTwo = item.findViewById(R.id.imgTwo)
            parentLayout = item.findViewById(R.id.parentLayout)
        }
    }


    class GroupListingViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var grpName: TextView? = null
        var grpOwner: TextView? = null
        var txtMembers: TextView? = null
        var grpProfile: CircleImageView? = null
        var btnJoin: FrameLayout? = null
        var checkBox: CheckBox? = null
        var divider: View? = null
        var txtActionBtn: TextView? = null

        init {
            grpName = item.findViewById(R.id.grpName)
            grpOwner = item.findViewById(R.id.grpOwner)
            txtMembers = item.findViewById(R.id.txtMembers)
            grpProfile = item.findViewById(R.id.grpProfile)
            checkBox = item.findViewById(R.id.checkbox)
            btnJoin = item.findViewById(R.id.actionBtn)
            divider = item.findViewById(R.id.divider)
            txtActionBtn = item.findViewById(R.id.txtActionBtn)
        }
    }

    class StoreViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var storeName: TextView? = null
        var userName: TextView? = null
        var txtMembers: TextView? = null
        var storeProfile: CircleImageView? = null
        var btnJoin: FrameLayout? = null
        var checkBox: CheckBox? = null
        var divider: View? = null
        var txtActionBtn: TextView? = null
        var userPic: CircleImageView? = null

        init {
            storeName = item.findViewById(R.id.grpName)
            userName = item.findViewById(R.id.grpOwner)
            txtMembers = item.findViewById(R.id.txtMembers)
            storeProfile = item.findViewById(R.id.grpProfile)
            userPic = item.findViewById(R.id.userPic)
            checkBox = item.findViewById(R.id.checkbox)
            btnJoin = item.findViewById(R.id.actionBtn)
            divider = item.findViewById(R.id.divider)
            txtActionBtn = item.findViewById(R.id.txtActionBtn)
        }
    }

    class CartViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var imgProduct: PorterShapeImageView? = null
        var productTitle: TextView? = null
        var txtPrice: TextView? = null
        var txtActualPrice: TextView? = null
        var txtOffer: TextView? = null
        var actionRemove: TextView? = null
        var qtySpinner: Spinner? = null
        var qtyValue: TextView? = null
        var qtyLayout: RelativeLayout? = null

        init {
            imgProduct = item.findViewById(R.id.imgProduct)
            productTitle = item.findViewById(R.id.productTitle)
            txtPrice = item.findViewById(R.id.txtPrice)
            txtActualPrice = item.findViewById(R.id.txtActualPrice)
            txtOffer = item.findViewById(R.id.txtOffer)
            actionRemove = item.findViewById(R.id.actionRemove)
            qtySpinner = item.findViewById(R.id.qtySpinner)
            qtyValue = item.findViewById(R.id.qtyValue)
            qtyLayout = item.findViewById(R.id.qtyLayout)
        }
    }

    class VariantViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var vTypeTxt: TextView? = null
        var vTypeTxtValue: TextView? = null
        var horzRecycler: RecyclerView? = null

        init {
            vTypeTxt = item.findViewById(R.id.vType)
            vTypeTxtValue = item.findViewById(R.id.vTypeValue)
            horzRecycler = item.findViewById(R.id.horzRecycler)
        }
    }

    class SpecificationViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var txtTitle: TextView? = null
        var txtValue: TextView? = null

        init {
            txtTitle = item.findViewById(R.id.txtTitle)
            txtValue = item.findViewById(R.id.txtValue)
        }
    }

    class SetViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var imgProduct: ImageView? = null
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

    class AddressViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var checkBox: CheckBox? = null
        var txtName: TextView? = null
        var actionEdit: ImageView? = null
        var txtAddress: TextView? = null
        var txtPhone: TextView? = null

        init {
            checkBox = item.findViewById(R.id.checkbox)
            txtName = item.findViewById(R.id.txtName)
            actionEdit = item.findViewById(R.id.actionEdit)
            txtAddress = item.findViewById(R.id.txtAddress)
            txtPhone = item.findViewById(R.id.txtPhone)
        }
    }

    class ThemeViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var image: ImageView? = null
        var parentLayout: FrameLayout? = null

        init {

            image = item.findViewById(R.id.paymentStatusImage)
            parentLayout = item.findViewById(R.id.parentLayout)
        }
    }

    class AccountViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var image: CircleImageView? = null
        var txtAccount: TextView? = null

        init {

            image = item.findViewById(R.id.image)
            txtAccount = item.findViewById(R.id.txtAccount)
        }
    }

    class PaymentTypeHolder(item: View) : RecyclerView.ViewHolder(item) {
        var checkbox: CheckBox? = null
        var txtName: TextView? = null

        init {
            checkbox = item.findViewById(R.id.checkbox)
            txtName = item.findViewById(R.id.paymentName)
        }
    }

    class ProductViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var image: RoundedImageView? = null
        var name: AppCompatTextView? = null
        var description: AppCompatTextView? = null
        var units: AppCompatTextView? = null
        var price: TextView? = null
        var textProductReview: AppCompatTextView
        var productDivider: View

        init {
            image = item.findViewById(R.id.imageOrderProduct)
            name = item.findViewById(R.id.textProductName)
            description = item.findViewById(R.id.textProductDescription)
            units = item.findViewById(R.id.textProductUnits)
            price = item.findViewById(R.id.textProductPrice)
            textProductReview = item.findViewById(R.id.textProductReview)
            productDivider = item.findViewById(R.id.productDivider)
        }
    }

    class OrderTimelineHolder(item: View) : RecyclerView.ViewHolder(item) {
        var statusDot: View? = null
        var statusLine: View? = null
        var title: AppCompatTextView? = null
        var description: AppCompatTextView? = null
        var date: AppCompatTextView? = null
        var time: AppCompatTextView? = null

        init {
            statusDot = item.findViewById(R.id.viewCircle)
            statusLine = item.findViewById(R.id.viewLine)
            title = item.findViewById(R.id.textTitle)
            description = item.findViewById(R.id.textDescription)
            date = item.findViewById(R.id.textDate)
            time = item.findViewById(R.id.textTime)
        }


    }


    class NotificationHolder(item: View) : RecyclerView.ViewHolder(item) {

        var notificationImage: CircleImageView = item.findViewById(R.id.notificationImageView)
        var notificationTitle: TextView = item.findViewById(R.id.notificationTitle)
        var notificationTime: TextView = item.findViewById(R.id.notificationTime)
        var divider: View = item.findViewById(R.id.divider)
    }

    class CategoryViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var innerRecyclerView: RecyclerView = item.findViewById(R.id.innerRecyclerView)
        var categoryTitle: TextView = item.findViewById(R.id.txtCategory)
        var iconDropDown: ImageView = item.findViewById(R.id.iconDropDown)
    }

    class LocationViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val txtTitle: TextView = item.findViewById(R.id.txtTitle)
        val txtDesc: TextView = item.findViewById(R.id.txtDesc)
    }

    class TransactionViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val txtTransactionRefId: TextView = item.findViewById(R.id.txtTransactionRefId)
        val txtTransactionCreatedTime: TextView = item.findViewById(R.id.txtTransactionCreatedTime)
        val txtTransactionAmount: TextView = item.findViewById(R.id.txtTransactionAmount)
        val tvTransactionInfo:TextView = item.findViewById(R.id.tvTransactionInfo)
    }

    class ReviewViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val userProfile: CircleImageView = item.findViewById(R.id.userProfile)
        val reviewerName: CustomTextView = item.findViewById(R.id.reviewerName)
        val ratingBar: RatingBar = item.findViewById(R.id.ratingBar)
        val reviewedTimeTxt: CustomTextView = item.findViewById(R.id.reviewedTimeTxt)
        val reviewContent: CustomTextView = item.findViewById(R.id.reviewContent)
        val reviewTitle: CustomTextView = item.findViewById(R.id.reviewTitle)
        val iconReviewLike: ImageView = item.findViewById(R.id.iconReviewLike)
        val bottomDivider:View = item.findViewById(R.id.bottomDivider)
        val reviewedPhotoContainer:LinearLayout = item.findViewById(R.id.reviewedPhotoContainer)
        val reviewedPhotoScrollView:HorizontalScrollView = item.findViewById(R.id.reviewedPhotoScrollView)
        val reviewImageSize:Int = Utils.getPixel(item.context,80)
    }

    class ShippingMethodHolder(item: View) : RecyclerView.ViewHolder(item) {
        val checkbox: CheckBox = item.findViewById(R.id.checkbox)
        val txtViewName: TextView = item.findViewById(R.id.txtViewName)
        val parentLayout: ConstraintLayout = item.findViewById(R.id.parent_layout)
    }

    class LocaleHolder(item: View): RecyclerView.ViewHolder(item) {
        val checkBox:CheckBox = item.findViewById(R.id.checkbox)
        val localeName:TextView = item.findViewById(R.id.txtViewName)
    }

    class StoreFeedViewHolder(item: View) : RecyclerView.ViewHolder(item){
        val ivAccountProfile:CircleImageView = item.findViewById(R.id.ivAccountProfile)
        val tvAccountName: CustomTextView = item.findViewById(R.id.tvAccountName)
        val tvAccountCategory: CustomTextView = item.findViewById(R.id.tvAccountCategory)
        val tvAccountAddress: CustomTextView = item.findViewById(R.id.tvAccountAddress)
        val tvViewLocation: CustomTextView = item.findViewById(R.id.tvViewLocation)
        val btnBlock:FrameLayout = item.findViewById(R.id.btnBlock)
        val ivLocationIcon:ImageView = item.findViewById(R.id.ivLocationIcon)

    }

    class AttachmentHolder(item: View) : RecyclerView.ViewHolder(item) {
        val fileName: TextView = item.findViewById(R.id.attachment_name)
        val fileSize: TextView = item.findViewById(R.id.attachment_size)
        val attachmentImage: ImageView = item.findViewById(R.id.attachment_image)
        val deleteAttachment: ImageView = item.findViewById(R.id.attachment_delete)
        val attachmentParentLayout:ConstraintLayout = item.findViewById(R.id.attachment_parent_layout)
    }

    class AdViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val adViewLayout: FrameLayout = item.findViewById(R.id.adViewLayout)
    }

    class ListingViewHolder(item: View, listItemWidth:Int,margin:Int) : RecyclerView.ViewHolder(item) {
        var imgProduct: ImageView? = null
        var txtProductName: TextView? = null
        var txtFinalPrice: TextView? = null
        var txtPrice: TextView? = null
        var txtOffer: TextView? = null
        var actionLayout:FrameLayout? = null
        var iconAction:ImageView?=null
        var layoutSold:FrameLayout?=null
        var txtProductStatus: TextView
        var getProducts:GetProducts

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
            val parseProductDataSource = ProductDataSourceImpl()
            val productRepository = ProductRepository(parseProductDataSource)
            getProducts = GetProducts(productRepository)
            val parentLayout =  item.findViewById<CardView>(R.id.parent_layout)
            val layoutParam = parentLayout.layoutParams as ViewGroup.MarginLayoutParams
            layoutParam.width = listItemWidth
            if(margin!=0){
                layoutParam.setMargins(margin,0,margin,0)
            }
            parentLayout.layoutParams = layoutParam
        }
    }

    private fun bindStoreFeed(holder: StoreFeedViewHolder,store: Store,position: Int){
        ImageHelper.getInstance().showImage(ctx,store.storePic,holder.ivAccountProfile,R.drawable.ic_store_placeholder,R.drawable.ic_store_placeholder)
        if (store.geoPoint.latitude != 0.0) {
            holder.ivLocationIcon.setVisible()
            holder.tvViewLocation.setVisible()
            holder.tvAccountAddress.apply {
                if (store.address.city.isNotEmpty()) {
                    text = store.address.city
                    setVisible()
                } else {
                    setGone()
                }
            }
            holder.tvViewLocation.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        } else {
            holder.ivLocationIcon.setGone()
            holder.tvViewLocation.setGone()
            holder.tvAccountAddress.setGone()
        }

        holder.tvAccountCategory.apply {
            text = store.category?.name
            visibility = if(store.category?.name.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
        holder.tvAccountName.text = store.storeName
        if(holder.itemViewType == AppConstant.ListingType.STORE_FEEDS_BLOCK){
            val outLineBackground = Utils.getDrawable(ctx,1,colorPrimary,colorSoupBubble,radius = 8f)
            val btnOutlinedBg = Utils.getRippleDrawable(ContextCompat.getColor(ctx,R.color.defaultRippleGreen),outLineBackground)
            holder.btnBlock.apply {
                background = btnOutlinedBg
                setVisible()
                setOnClickListener {
                    click(holder.adapterPosition,store)
                }
            }
        } else {
            holder.itemView.setOnClickListener {
                click(holder.adapterPosition, store)
            }
        }
        holder.tvViewLocation.setOnClickListener { Utils.showGoogleMap(store.geoPoint.latitude,store.geoPoint.longitude) }
    }

    private fun bindTimeline(
        orderTimelineHolder: OrderTimelineHolder,
        orderStatusEntity: OrderStatusEntity,
        position: Int
    ) {
        if (orderStatusEntity.inProgress) {
            orderTimelineHolder.statusDot?.setBackgroundResource(R.drawable.bg_glow_green_gradient_circle)
            //orderTimelineHolder.statusLine?.setBackgroundResource(R.color.colorCircleStartGreen)
        } else if (!orderStatusEntity.inProgress) {
            orderTimelineHolder.statusDot?.setBackgroundResource(R.drawable.bg_green_gradient_circle)
            // orderTimelineHolder.statusLine?.setBackgroundResource(R.color.colorCircleStartGreen)
        } else {
            orderTimelineHolder.statusDot?.setBackgroundResource(R.drawable.bg_grey_gradient_circle)
            //orderTimelineHolder.statusLine?.setBackgroundResource(R.color.colorDotGrey)
        }
        orderTimelineHolder.title?.text =  if(orderStatusEntity.status== AppConstant.OrderStatus.ORDER_STATUS_CANCELED_BY_CUSTOMER){
            ctx.getString(R.string.orderdetail_order_cancelled)
        }
        else{
            val orderStatusRes = Utils.getOrderStatus(orderStatusEntity.status)
            if(orderStatusRes != 0) ctx.getString(orderStatusRes) else AppConstant.EMPTY
        }
        orderTimelineHolder.description?.text = orderStatusEntity.description
        orderTimelineHolder.date?.text = DateTimeHelper.getDateFromTimeMillis(orderStatusEntity.date, FORMAT_DATE_DD_MM_YYYY)
        orderTimelineHolder.time?.text = DateTimeHelper.getDateFromTimeMillis(java.util.Locale("en"),orderStatusEntity.time, FORMAT_TIME_AM_PM)


        if (list.size - 1 == position) {
            orderTimelineHolder.statusLine?.visibility = View.GONE
        } else {
            orderTimelineHolder.statusLine?.visibility = View.VISIBLE
        }
    }


    private fun bindNotification(
        holder: NotificationHolder,
        notification: Notification,
        position: Int
    ) {
        holder.notificationTitle.text = Utils.getNotificationMessage(ctx,notification)
        holder.notificationImage.clearColorFilter()
        when (notification.referenceType) {
            AppConstant.NotificationType.ACTIVITY_REFERENCE_TYPE_ORDER -> {
                holder.notificationImage.setImageResource(R.drawable.ic_product_box)
                holder.notificationImage.setColorFilter(
                    ContextCompat.getColor(
                        ctx,
                        R.color.colorBlueLight
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
            AppConstant.NotificationType.ACTIVITY_REFERENCE_TYPE_LISTING -> {
                holder.notificationImage.setImageByUrl(
                    ctx,
                    notification.product.images[0],
                    R.drawable.placeholder_image
                )
            }
            AppConstant.NotificationType.ACTIVITY_TYPE_ACCOUNT_FOLLOW -> {
                holder.notificationImage.setImageByUrl(
                    ctx,
                    notification.store.storePic,
                    R.drawable.ic_store_placeholder
                )
            }
        }
        holder.notificationTime.text = DateTimeHelper.getDateFromTimeMillis(
            notification.createdAt,
            DateTimeHelper.FORMAT_DATE_DD_MM_YYYY_HH_MM
        )

        if (list.size - 1 == position) {
            holder.divider.visibility = View.GONE
        } else {
            holder.divider.visibility = View.VISIBLE
        }

        holder.itemView.safeClickListener {
            click(holder.adapterPosition, notification)
        }
    }

    private fun bindCategory(holder: CategoryViewHolder, category: Category, position: Int) {
        holder.iconDropDown.visibility =
            if (category.subCategory.isNotEmpty()) View.VISIBLE else View.GONE
        holder.categoryTitle.text = category.name
        holder.iconDropDown.safeClickListener {
            if (category.isExpanded) {
                category.isExpanded = false
            } else {
                category.isExpanded = true
                val llManager = LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false)
                holder.innerRecyclerView.layoutManager = llManager
                val adapter = ListingAdapter(
                    ctx,
                    category.subCategory,
                    AppConstant.ListingType.CATEGORY_LIST,
                    holder.innerRecyclerView
                ) { position, obj ->
                    click(position, obj)
                }
                holder.innerRecyclerView.adapter = adapter
                holder.innerRecyclerView.visibility = View.VISIBLE
            }
        }

        if (category.isExpanded) {
            holder.innerRecyclerView.visibility = View.VISIBLE
        } else {
            holder.innerRecyclerView.visibility = View.GONE
        }

        holder.categoryTitle.safeClickListener {
            click(holder.adapterPosition, category)
        }

    }

    private fun bindLocationSearch(holder: LocationViewHolder, item: Address, position: Int) {
        holder.txtTitle.text =
            if (item.formattedAddress.isNotEmpty()) item.formattedAddress.split(",")[0] else Constant.EMPTY
        holder.txtDesc.text = item.formattedAddress
        holder.itemView.safeClickListener {
            click(holder.adapterPosition, list[holder.adapterPosition])
        }
    }

    private fun bindTransactionView(
        holder: TransactionViewHolder,
        transaction: Transaction,
        position: Int
    ) {
        holder.txtTransactionRefId.text =
            String.format(ctx.getString(R.string.hast_tag), transaction.transactionNumber)
        holder.txtTransactionCreatedTime.text = DateTimeHelper.getDateFromTimeMillis(
            transaction.createdAt,
            DateTimeHelper.FORMAT_DATE_D_MMM_YYYY_HH_MM
        )
        val price = transaction.amount
        holder.txtTransactionAmount.text = price.displayCurrency
        if (price.amount < 0) {
            holder.txtTransactionAmount.setTextColor(ContextCompat.getColor(ctx, R.color.colorRed))
        } else {
            holder.txtTransactionAmount.setTextColor(
                ContextCompat.getColor(
                    ctx,
                    R.color.colorGreen
                )
            )
        }

        val transactionInfoRes = Utils.getTransactionTypeInfo(transaction.type.toString())
        if(transactionInfoRes!=0){
            holder.tvTransactionInfo.text = ctx.getString(transactionInfoRes)
            holder.tvTransactionInfo.setVisible()
        }
        else{
            holder.tvTransactionInfo.setGone()
        }

        holder.itemView.setOnClickListener {
            click(position,transaction)
        }
    }

    private fun bindReviews(holder: ReviewViewHolder, review: Review) {
        holder.userProfile.setImageByUrl(ctx, review.user.profilePic, R.drawable.ic_user_placeholder)
        holder.reviewerName.text = review.user.name
        holder.ratingBar.rating = review.rating.toFloat()
        holder.reviewTitle.text = review.title
        holder.reviewTitle.visibility = if(review.title.isNotEmpty())View.VISIBLE else View.GONE
        holder.reviewContent.text = review.content
        holder.reviewedTimeTxt.text = DateTimeHelper.getDateFromTimeMillis(
            review.createdAt,
            DateTimeHelper.FORMAT_DATE_MMM_YYYY
        )
        holder.iconReviewLike.setColorFilter(
            ContextCompat.getColor(
                ctx,
                if (review.likeStatus == 1) colorPrimary else R.color.colorMediumGrey
            ), android.graphics.PorterDuff.Mode.SRC_IN
        )
        holder.iconReviewLike.safeClickListener {
            if (NetworkUtil.isConnectingToInternet()) {
                if (review.likeStatus == 1) {
                    review.likeStatus = 2
                } else {
                    review.likeStatus = 1
                }
                holder.iconReviewLike.setColorFilter(
                    ContextCompat.getColor(
                        ctx,
                        if (review.likeStatus == 1) colorPrimary else R.color.colorMediumGrey
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
                click(holder.adapterPosition, review)
            }
        }
        val reviewImages = review.images
        if(reviewImages.isNotEmpty()){
            holder.reviewedPhotoScrollView.visibility = View.VISIBLE
            holder.reviewedPhotoContainer.removeAllViews()
            for(index in reviewImages.indices){
                val image = RoundedImageView(ctx).apply {
                    borderAlpha = 0.0f
                    radius = 8
                    setSquare(true)
                    borderWidth = 0
                }
                val param = LinearLayout.LayoutParams(holder.reviewImageSize,holder.reviewImageSize)
                param.marginEnd = Utils.getPixel(ctx,8)
                image.layoutParams = param
                ImageHelper.getInstance().showImage(ctx,reviewImages[index],image,R.drawable.placeholder_image,R.drawable.placeholder_image,true,false)
                holder.reviewedPhotoContainer.addView(image)
                image.safeClickListener {
                    val bundle = Bundle()
                    bundle.putInt(AppConstant.BundleProperty.POSITION,index)
                    bundle.putString(AppConstant.BundleProperty.IMAGES,reviewImages.toJson<List<String>>())
                    ctx.startActivity(ImageViewerActivity::class.java,bundle)
                }
            }
        }
        else{
            holder.reviewedPhotoScrollView.visibility = View.GONE
        }

        holder.bottomDivider.visibility = if(list.size-1==holder.adapterPosition)View.INVISIBLE else View.VISIBLE
    }

    private fun bindShippingMethod(holder: ShippingMethodHolder, shippingMethod: ShippingMethod) {
        holder.txtViewName.text = shippingMethod.name
        holder.checkbox.isChecked = shippingMethod.default
        holder.checkbox.isClickable = false
        holder.parentLayout.setOnClickListener {
            if (AppConstant.ListingType.SHIPMENT_METHOD_SELECT_LIST == isFor) {
                list.forEach { (it as ShippingMethod).default = false }
                (list[holder.adapterPosition] as ShippingMethod).default = true
                click(holder.adapterPosition, list[holder.adapterPosition])
            } else {
                (list[holder.adapterPosition] as ShippingMethod).apply {
                    this.default = !this.default
                }
            }
            notifyDataSetChanged()
        }
    }

    private fun bindLocale(holder:LocaleHolder,locale:Locale){
        holder.localeName.text = locale.name
        with(holder.checkBox){
            isClickable = false
            visibility = View.VISIBLE
            isChecked = locale.default
        }
        holder.itemView.setOnClickListener {
            holder.checkBox.isChecked = true
            list.forEach { (it as Locale).default = false}
            (list[holder.adapterPosition] as Locale).default = true
            notifyDataSetChanged()
        }
    }

    private fun bindAttachment(holder:AttachmentHolder, fileInfo: FileInfo){
        if(fileInfo.fileUri!!.startsWith(AppConstant.UrlScheme.HTTP)){
            holder.fileName.text = URLUtil.guessFileName(fileInfo.fileUri,null,null)
            holder.fileSize.setGone()
            holder.attachmentImage.setGone()
        }
        else{
            val file = File(fileInfo.fileUri!!)
            holder.fileName.text = file.name
            holder.fileSize.text = FileHelper.getFileSize(file)
            val mimeType = FileHelper.getMimeType(fileInfo.fileUri)
            holder.attachmentImage.setImageResource(FileHelper().getFileImage(mimeType))
            holder.attachmentImage.setColorFilter(ContextCompat.getColor(ctx, colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN)
        }
        holder.attachmentParentLayout.background = Utils.getDrawable(ctx,1,R.color.colorVeryBlueLight,R.color.colorLightGrey)
        holder.deleteAttachment.setOnClickListener {
            click(holder.adapterPosition,fileInfo)

        }
        holder.itemView.safeClickListener {
            if(fileInfo.fileUri!!.startsWith(AppConstant.UrlScheme.HTTP)){
                Utils.openUrlInBrowser(fileInfo.fileUri!!)
            }
            else{
                val bundle = Bundle().apply { putString(AppConstant.BundleProperty.IMAGES, listOf(fileInfo.fileUri!!).toJson<List<String>>()) }
                ctx.startActivity(ImageViewerActivity::class.java,bundle)
            }
        }
    }

    private fun bindViewAd(holder:AdViewHolder){
       /* val adView = AdView(ctx)
        holder.adViewLayout.addView(adView)
        val adRequest = AdRequest.Builder().build()
        adView.adUnitId = AppConfigHelper.getConfigKey<String>(AppConfigHelper.Keys.LISTINGS_ADMOB_ID)
        adView.adSize = AdSize.MEDIUM_RECTANGLE
        adView.loadAd(adRequest)*/
    }

    private fun bindViewProduct(holder: ListingViewHolder?, product: Product) {
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
            ctx.startActivity(intent)
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
        holder?.iconAction?.setImageResource(if(product.isLiked)R.drawable.ic_favorite_black_24dp else R.drawable.ic_favorite_border_black_24dp)
        holder?.iconAction?.setColorFilter(ContextCompat.getColor(ctx, colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN)
        holder?.iconAction?.visibility = View.VISIBLE
        holder?.actionLayout?.safeClickListener {
            likeAction(holder.adapterPosition,holder)
        }
    }

    private fun bindViewGroupGrid(holder: GroupGridViewHolder?, list: List<Any>, position: Int) {
        val item = list[position] as? Group
        holder?.txtOne?.text = item?.groupName
        holder?.imgTwo?.visibility = View.GONE
        holder?.txtTwo?.text =
            String.format(ctx.getString(R.string.group_members), item?.membersCount)
        holder?.txtActionBtn?.text = ctx.getString(R.string.group_join)
        item?.joined?.let {
            holder?.actionBtn?.setBackgroundResource(if (item.joined) R.drawable.bg_button_outline else btnJoin)
            holder?.txtActionBtn?.text = if (item.joined) txtJoined else txtJoin
            holder?.txtActionBtn?.setTextColor(
                ContextCompat.getColor(
                    ctx,
                    if (item.joined) colorPrimary else R.color.colorWhite
                )
            )
        }

        holder?.actionBtn?.setOnClickListener {
            if (NetworkUtil.isConnectingToInternet()) {
                synchronized(this) {
                    item?.joined?.let {
                        if (item.joined) {
                            holder.actionBtn?.setBackgroundResource(btnJoin)
                            (list[position] as Group).joined = false
                            holder.txtActionBtn?.text = txtJoin
                            holder.txtActionBtn?.setTextColor(
                                ContextCompat.getColor(
                                    ctx,
                                    R.color.colorWhite
                                )
                            )

                        } else {
                            holder.actionBtn?.setBackgroundResource(R.drawable.bg_button_outline)
                            (list[position] as Group).joined = true
                            holder.txtActionBtn?.text = txtJoined
                            holder.txtActionBtn?.setTextColor(
                                ContextCompat.getColor(
                                    ctx,
                                    colorPrimary
                                )
                            )
                        }
                        click(holder.adapterPosition, item)
                    }
                }
            }
        }
        ImageHelper.getInstance().showImage(
            ctx,
            item?.groupPic,
            holder?.imageOne,
            R.drawable.placeholder_image,
            R.drawable.placeholder_image
        )
        holder?.itemView?.setOnClickListener {
            val intent = Intent(ctx, GroupDetailActivity::class.java)
            intent.putExtra("groupId", item?.id)
            ctx.startActivity(intent)
        }
    }

    private fun bindViewStoreGrid(holder: StoreGridViewHolder?, list: List<Any>, position: Int) {
        val item = list[position] as? Store
        holder?.txtOne?.text = item?.storeName
        holder?.imgTwo?.visibility = View.VISIBLE
        holder?.txtTwo?.visibility = View.GONE
        holder?.txtActionBtn?.text = ctx.getString(R.string.storelist_follow)
        ImageHelper.getInstance().loadThumbnailThenMain(
            ctx,
            item?.userPic.getValue(),
            holder?.imgTwo,
            R.drawable.ic_user_placeholder,
            R.drawable.ic_user_placeholder
        )
        item?.followed?.let {
            holder?.actionBtn?.setBackgroundResource(if (item.followed) R.drawable.bg_button_outline else btnJoin)
            holder?.txtActionBtn?.text = if (item.followed) txtFollowing else txtFollow
            holder?.txtActionBtn?.setTextColor(
                ContextCompat.getColor(
                    ctx,
                    if (item.followed) colorPrimary else R.color.colorWhite
                )
            )
        }

        holder?.actionBtn?.setOnClickListener {
            if (NetworkUtil.isConnectingToInternet()) {
                synchronized(this) {
                    item?.followed?.let {
                        if (item.followed) {
                            holder.actionBtn?.setBackgroundResource(btnJoin)
                            (list[position] as Store).followed = false
                            holder.txtActionBtn?.text = txtFollow
                            holder.txtActionBtn?.setTextColor(
                                ContextCompat.getColor(
                                    ctx,
                                    R.color.colorWhite
                                )
                            )

                        } else {
                            holder.actionBtn?.setBackgroundResource(R.drawable.bg_button_outline)
                            (list[position] as Store).followed = true
                            holder.txtActionBtn?.text = txtFollowing
                            holder.txtActionBtn?.setTextColor(
                                ContextCompat.getColor(
                                    ctx,
                                    colorPrimary
                                )
                            )
                        }
                        click(holder.adapterPosition, item)
                    }
                }
            }
        }
        ImageHelper.getInstance().showImage(
            ctx,
            item?.storePic,
            holder?.imageOne,
            R.drawable.placeholder_image,
            R.drawable.placeholder_image
        )
        holder?.itemView?.setOnClickListener {
            val intent = Intent(ctx, StoreDetailActivity::class.java)
            intent.putExtra("storeName", item?.storeName)
            intent.putExtra("id", item?.id)
            ctx.startActivity(intent)
        }
    }

    private fun bindViewStore(holder: StoreViewHolder?, list: List<Any>, position: Int) {
        val store = list[position] as? Store
        ImageHelper.getInstance().showImage(
            ctx,
            store?.storePic,
            holder?.storeProfile,
            R.drawable.ic_store_placeholder,
            R.drawable.ic_store_placeholder
        )
        ImageHelper.getInstance().showImage(
            ctx,
            store?.userPic,
            holder?.userPic,
            R.drawable.ic_user_placeholder,
            R.drawable.ic_user_placeholder
        )
        holder?.btnJoin?.visibility = View.VISIBLE
        holder?.userPic?.visibility = View.VISIBLE
        holder?.storeName?.text = store?.storeName
        holder?.userName?.text = store?.userName
        holder?.txtMembers?.text =
            String.format(ctx.getString(R.string.group_following_count), store?.followersCount)
        store?.followed?.let {
            holder?.btnJoin?.setBackgroundResource(if (store.followed) R.drawable.bg_button_outline else btnJoin)
            holder?.txtActionBtn?.text = if (store.followed) txtFollowing else txtFollow
            holder?.txtActionBtn?.setTextColor(
                ContextCompat.getColor(
                    ctx,
                    if (store.followed) colorPrimary else R.color.colorWhite
                )
            )
        }

        holder?.btnJoin?.setOnClickListener {
            if (NetworkUtil.isConnectingToInternet()) {
                synchronized(this) {
                    store?.followed?.let {
                        if (store.followed) {
                            holder.btnJoin?.setBackgroundResource(btnJoin)
                            (list[position] as Store).followed = false
                            holder.txtActionBtn?.text = txtFollow
                            holder.txtActionBtn?.setTextColor(
                                ContextCompat.getColor(
                                    ctx,
                                    R.color.colorWhite
                                )
                            )

                        } else {
                            holder.btnJoin?.setBackgroundResource(R.drawable.bg_button_outline)
                            (list[position] as Store).followed = true
                            holder.txtActionBtn?.text = txtFollowing
                            holder.txtActionBtn?.setTextColor(
                                ContextCompat.getColor(
                                    ctx,
                                    colorPrimary
                                )
                            )
                        }
                        click(holder.adapterPosition, store)
                    }
                }
            }
        }
        holder?.itemView?.setOnClickListener {
            val intent = Intent(ctx, StoreDetailActivity::class.java)
            intent.putExtra("storeId", store?.id)
            intent.putExtra("id", store?.storeName)
            ctx.startActivity(intent)
        }
    }

    private fun bindViewGroup(holder: GroupListingViewHolder?, list: List<Any>, position: Int) {
        val group = list[position] as? Group
        holder?.grpName?.text = group?.groupName
        holder?.grpOwner?.visibility = View.GONE
        ImageHelper.getInstance().showImage(
            ctx,
            group?.groupPic,
            holder?.grpProfile,
            R.drawable.ic_group_placeholder,
            R.drawable.ic_group_placeholder
        )
        if (isFor == AppConstant.ListingType.GROUP_TAG_PRODUCT) {
            holder?.checkBox?.visibility = View.VISIBLE
            holder?.btnJoin?.visibility = View.GONE
            holder?.checkBox?.isChecked = group?.isSelected ?: false
            holder?.grpName?.text = group?.groupName
            holder?.txtMembers?.text =
                String.format(
                    ctx.getString(
                        R.string.group_following_count,
                        group?.membersCount.toString()
                    )
                )
            holder?.checkBox?.setOnCheckedChangeListener { compoundButton, b ->
                (list[position] as Group).isSelected = b
            }
        } else if (isFor == AppConstant.ListingType.GROUP_FOLLOW) {
            holder?.checkBox?.visibility = View.GONE
            holder?.btnJoin?.visibility = View.VISIBLE
            holder?.grpName?.text = group?.groupName
            group?.joined?.let {
                holder?.btnJoin?.setBackgroundResource(if (group.joined) R.drawable.bg_button_outline else btnJoin)
                holder?.txtActionBtn?.text = if (group.joined) txtJoined else txtJoin
                holder?.txtActionBtn?.setTextColor(
                    ContextCompat.getColor(
                        ctx,
                        if (group.joined) colorPrimary else R.color.colorWhite
                    )
                )
            }
            holder?.txtMembers?.text =
                String.format(ctx.getString(R.string.group_members, group?.membersCount.toString()))
            holder?.btnJoin?.setOnClickListener {
                if (NetworkUtil.isConnectingToInternet()) {
                    synchronized(this) {
                        group?.joined?.let {
                            if (group.joined) {
                                holder.btnJoin?.setBackgroundResource(btnJoin)
                                (list[position] as Group).joined = false
                                holder.txtActionBtn?.text = txtJoin
                                holder.txtActionBtn?.setTextColor(
                                    ContextCompat.getColor(
                                        ctx,
                                        R.color.colorWhite
                                    )
                                )

                            } else {
                                holder.btnJoin?.setBackgroundResource(R.drawable.bg_button_outline)
                                (list[position] as Group).joined = true
                                holder.txtActionBtn?.text = txtJoined
                                holder.txtActionBtn?.setTextColor(
                                    ContextCompat.getColor(
                                        ctx,
                                        colorPrimary
                                    )
                                )
                            }
                            click(holder.adapterPosition, group)
                        }
                    }
                }
            }

            holder?.itemView?.setOnClickListener {
                val intent = Intent(ctx, GroupDetailActivity::class.java)
                intent.putExtra("groupId", group?.id)
                ctx.startActivity(intent)
            }
        }

        holder?.divider?.visibility = View.VISIBLE
        if (position == list.count() - 1) {
            holder?.divider?.visibility = View.GONE
        }
    }

    private fun bindViewChatRoom(holder: ChatRoomViewHolder?, list: List<Any>, position: Int) {
        val chatRoom = list[position] as ChatRoom
        ImageHelper.getInstance().showImage(
            ctx,
            chatRoom.profilePic,
            holder?.chatProfile,
            R.drawable.ic_user_placeholder,
            R.drawable.ic_user_placeholder
        )
        holder?.userName?.text = chatRoom.receiver
        holder?.receivedTime?.text =
            DateTimeHelper.TimeAgo.getTimeAgo(ctx, chatRoom.lastUpdated.toString())
        holder?.receivedCount?.text = chatRoom.count.toString()
        when (chatRoom.mimeType) {
            MessageHelper.Companion.Type.MSG -> {
                holder?.messageIcon?.visibility = View.GONE
                holder?.lastMessage?.text = chatRoom.lastMessage
            }
            MessageHelper.Companion.Type.IMAGE -> {
                holder?.messageIcon?.visibility = View.VISIBLE
                holder?.messageIcon?.setImageResource(R.drawable.ic_camera_alt_black_24dp)
                holder?.lastMessage?.text = ctx.getString(R.string.chatdetail_photo)
            }
            MessageHelper.Companion.Type.AUDIO -> {
                holder?.messageIcon?.visibility = View.VISIBLE
                holder?.messageIcon?.setImageResource(R.drawable.ic_audiotrack_black_24dp)
                holder?.lastMessage?.text = ctx.getString(R.string.chatdetail_audio)
            }
            MessageHelper.Companion.Type.VIDEO -> {
                holder?.messageIcon?.visibility = View.VISIBLE
                holder?.messageIcon?.setImageResource(R.drawable.ic_videocam_black_24dp)
                holder?.lastMessage?.text = ctx.getString(R.string.chatdetail_video)
            }
            MessageHelper.Companion.Type.PRODUCT -> {
                holder?.messageIcon?.visibility = View.VISIBLE
                holder?.messageIcon?.setImageResource(R.drawable.ic_product_box)
                holder?.lastMessage?.text = ctx.getString(R.string.chatdetail_product)
            }
            MessageHelper.Companion.Type.MAKE_OFFER->{
                holder?.messageIcon?.visibility = View.VISIBLE
                holder?.messageIcon?.setImageResource(R.drawable.ic_discount)
                val message = when(chatRoom.offerStatus){
                    MessageHelper.Companion.Type.MakeOfferType.RECEIVED_REQUEST,
                        MessageHelper.Companion.Type.MakeOfferType.SENT_REQUEST->{
                        R.string.chat_requested_an_offer
                    }
                    MessageHelper.Companion.Type.MakeOfferType.DENIED_REQUEST-> R.string.chat_offer_declined
                    MessageHelper.Companion.Type.MakeOfferType.READY_TO_BUY,
                    MessageHelper.Companion.Type.MakeOfferType.ACCEPTED_REQUEST->R.string.chat_accepted
                    else->0
                }
                if (message != 0) {
                    holder?.lastMessage?.text = ctx.getString(message)
                }
            }
            else -> {
                holder?.messageIcon?.visibility = View.VISIBLE
                holder?.messageIcon?.setImageResource(R.drawable.ic_document_24dp)
                holder?.lastMessage?.text = ctx.getString(R.string.chatdetail_file)
            }
        }
        if(chatRoom.updatedBy==currentUser?.id){
            holder?.receivedCount?.visibility = View.GONE
            holder?.iconDeliveryStatus?.visibility = View.VISIBLE
            MessageHelper.setReadStatus(holder?.iconDeliveryStatus,chatRoom.deliveryStatus)
        }
        else{
            holder?.receivedCount?.visibility = if (chatRoom.count == 0) View.GONE else View.VISIBLE
            holder?.iconDeliveryStatus?.visibility = View.GONE
        }
        holder?.itemView?.setOnClickListener {
            click(holder.adapterPosition, chatRoom)
        }
    }

    private fun bindViewCart(holder: CartViewHolder, list: List<Any>, position: Int) {
        val cartItem = list[position] as CartItem
        val product = cartItem.listing
        holder.productTitle?.text = product.title
        if (product.offerPercent ==0) {
            holder.txtActualPrice?.visibility = View.GONE
            holder.txtOffer?.visibility = View.GONE
            holder.txtPrice?.visibility = View.VISIBLE
            holder.txtPrice?.text = cartItem.offerPrice.displayCurrency
        } else {
            holder.txtPrice?.text = cartItem.offerPrice.displayCurrency
            holder.txtActualPrice?.text = cartItem.totalPrice.displayCurrency
            holder.txtActualPrice?.visibility = View.VISIBLE
            holder.txtPrice?.visibility = View.VISIBLE
            if (AppConfigHelper.getConfigKey(AppConfigHelper.Keys.HIDE_OFFER_PERCENT)){
                holder.txtOffer?.visibility = View.VISIBLE
                holder.txtOffer?.text = String.format(ctx.getString(R.string.off_data), product.offerPercent.toString())
            }
            else{
                holder.txtOffer?.visibility = View.GONE
            }
            holder.txtActualPrice?.let {
                it.paintFlags = it.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
        }

        holder.actionRemove?.setOnClickListener {
            click(holder.adapterPosition, cartItem)
        }

        if (product.images.count() > 0) {
            ImageHelper.getInstance().loadThumbnailThenMain(
                ctx,
                product.images[0],
                holder.imgProduct,
                R.drawable.placeholder_image,
                R.drawable.placeholder_image
            )
        }
        holder.qtyValue?.text = cartItem.quantity.toString()
        val qtyList = mutableListOf<String>()
        if (product.maxQuantity >1) {
            holder.qtyLayout?.visibility = View.VISIBLE
            if (product.maxQuantity > 5) {
                qtyList.addAll(minQtyList)
            } else {
                for (i in 1..product.maxQuantity) {
                    qtyList.add(i.toString())
                }
                qtyList.add("default")
            }
            val adapter = QuantityListAdapter(qtyList, ctx)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            holder.qtySpinner?.adapter = adapter
            holder.qtySpinner?.setSelection(qtyList.count() - 1) //Don't change this index...
            holder.qtySpinner?.setOnItemSelectedListener(object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    if ((qtyList.count() - 1) != position) {
                        val stockEnabled = AppConfigHelper.getConfigKey<Boolean>(AppConfigHelper.Keys.STOCK_ENABLED)
                        if (product.maxQuantity > 5) {
                            if (position == 5) {
                                Utils.showQtyDialog(ctx, stockEnabled,product.stock,product.maxQuantity.toString()) {
                                    cartItem.quantity = Integer.parseInt(it as String)
                                    click(holder.adapterPosition, it)
                                }
                                return
                            }
                        }
                        val quantity = qtyList[position].toInt()
                        if(stockEnabled){
                            if(quantity>product.stock){
                                ctx.showToast(R.string.cart_alert_stock_not_available)
                                return
                            }
                        }
                        holder.qtyValue?.text = qtyList[position]
                        cartItem.quantity = quantity
                        click(holder.adapterPosition, qtyList[position])
                        holder.qtySpinner?.onItemSelectedListener = null
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            })
            holder.qtyLayout?.setOnClickListener {
                holder.qtySpinner?.performClick()
            }
        }
        else{
            holder.qtyLayout?.visibility = View.INVISIBLE
        }
        holder.actionRemove?.setOnClickListener {
            click(position, cartItem)
        }
        holder.itemView?.setOnClickListener {
            val intent = Intent(ctx, ProductDetailActivity::class.java)
            intent.putExtra("productId", product.id)
            ctx.startActivity(intent)
        }
    }

    private fun bindViewVariant(holder: VariantViewHolder, list: List<Any>, position: Int) {
        val variant = list[position] as Variant
        if (isFor == AppConstant.ListingType.VARIANT) {
            holder.vTypeTxt?.text =
                String.format(ctx.getString(R.string.two_data), variant.variantName, ":")
            holder.vTypeTxtValue?.text = variant.variantValue
        } else {
            holder.vTypeTxt?.text =
                String.format(ctx.getString(R.string.available), variant.variantName)
        }
        val layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)
        val adapter = VariantAdapter(ctx, isFor, variant.values) { pos, obj ->
            val mVariant = list[holder.adapterPosition] as Variant
            mVariant.variantValue = obj.variantValue
            notifyDataSetChanged()
            click(holder.adapterPosition, variant)
        }
        holder.horzRecycler?.layoutManager = layoutManager
        holder.horzRecycler?.adapter = adapter

    }


    private fun bindViewSetGrid(holder: SetViewHolder, list: List<Any>, position: Int) {
        val set = list[position] as Set
        holder.txtProductName?.text = set.title
        holder.txtFinalPrice?.text =
            String.format(ctx.getString(R.string.two_data), "", set.offerPrice.toString())
        holder.txtOffer?.text =
            String.format(ctx.getString(R.string.off_data), set.offerPercent.toString())
        holder.txtPrice?.text =
            String.format(ctx.getString(R.string.two_data), "", set.listPrice.toString())
        holder.txtPrice?.let {
            it.paintFlags = it.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }

        var productImage = Constant.EMPTY
        if (set.variants.count() > 0) {
            productImage =
                if (set.variants[0].images.count() > 0) set.variants[0].images[0] else set.defaultImage
        }
        ImageHelper.getInstance().showImage(
            ctx,
            productImage,
            holder.imgProduct,
            R.drawable.placeholder_image,
            R.drawable.placeholder_image
        )
        holder.itemView.setOnClickListener {
            click(holder.adapterPosition, set)
        }
    }

    private fun bindViewAddress(holder: AddressViewHolder, list: List<Any>, position: Int) {
        val address = list[position] as ShippingAddress
        holder.checkBox?.isChecked = false
        if (address.defaultAddress) {
            holder.checkBox?.isChecked = true
        }
        holder.txtName?.text = address.name
        holder.txtAddress?.text = ShippingPresenter.getFormedAddress(address)
        holder.txtPhone?.text = address.phoneNumber
        holder.checkBox?.setOnClickListener {
            click(position, address)
        }
        holder.actionEdit?.setOnClickListener {
            val intent = Intent(ctx, ManageShippingAddressActivity::class.java)
            intent.putExtra("address", Gson().toJson(address))
            intent.putExtra("isFor", AppConstant.ManageAction.EDIT)
            intent.putExtra(AppConstant.BundleProperty.SHIPMENT_TYPE,address.type)
            intent.putExtra(AppConstant.BundleProperty.IS_FOR_PICK_ADDRESS, isForPickupAddress)
            (ctx as? AddressListActivity)?.startActivityForResult(intent, 100)
        }
    }

    private fun bindViewSpecification(
        holder: SpecificationViewHolder,
        list: List<Any>,
        position: Int
    ) {
        val attribute = list[position] as Attribute
        holder.txtTitle?.text = attribute.name
        if (attribute.fieldType == 1 || attribute.fieldType == 2) {
            val values = attribute.attributeValues
            val mList = values.map { it.name }
            holder.txtValue?.text = mList.joinToString(" , ")
        } else {
            holder.txtValue?.text = attribute.attributeValueList.joinToString(separator = " , ")
        }
    }


    private fun bindViewTheme(holThemeViewHolder: ThemeViewHolder, list: List<Any>, position: Int) {
        val theme = list[position] as Theme
        holThemeViewHolder.image?.setColorFilter(
            ContextCompat.getColor(ctx, theme.color),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        holThemeViewHolder.image?.setImageResource(if (theme.isSelected) R.drawable.ic_circle_check else R.drawable.ic_unchecked_circle)
        holThemeViewHolder.parentLayout?.setOnClickListener {
            click(holThemeViewHolder.adapterPosition, theme)
        }
    }

    private fun bindAccount(
        holderAccountHolder: AccountViewHolder,
        list: List<Any>,
        position: Int
    ) {
        val category = list[position] as Category
        holderAccountHolder.txtAccount?.text = category.name
        ImageHelper.getInstance().showImage(
            ctx,
            category.imagePath,
            holderAccountHolder.image,
            R.drawable.placeholder_image,
            R.drawable.placeholder_image
        )
        holderAccountHolder.itemView.safeClickListener { click(position, category) }
    }

    private fun bindPaymentType(holderPayment: PaymentTypeHolder, list: List<Any>, position: Int) {
        val payment = list[position] as Payment
        holderPayment.txtName?.text = payment.name
        holderPayment.checkbox?.isChecked = payment.default
        holderPayment.itemView.safeClickListener {
            list.forEach { (it as Payment).default = false }
            (list[position] as Payment).default = true
            click(position, payment)
            notifyDataSetChanged()
        }
    }

    private fun bindOrder(
        holderProduct: ProductViewHolder,
        orderedProduct: OrderedProductEntity
    ) {
        ImageHelper.getInstance().loadThumbnailThenMain(
            holderProduct.itemView.context,
            orderedProduct.image,
            holderProduct.image,
            R.drawable.placeholder_image,
            R.drawable.placeholder_image
        )

        holderProduct.name?.text = orderedProduct.name
        holderProduct.description?.text = orderedProduct.description
        holderProduct.units?.text = orderedProduct.units
        holderProduct.price?.text = orderedProduct.price
        holderProduct.price?.setTextColor(ContextCompat.getColor(ctx, colorPrimary))

        holderProduct.itemView.safeClickListener {
            val productId = (list[holderProduct.adapterPosition] as OrderedProductEntity).id
            ctx.startActivity(ProductDetailActivity::class.java,
                Bundle().apply {
                    putString(
                        AppConstant.BundleProperty.PRODUCT_ID,
                        productId.toString()
                    )
                })
        }

        holderProduct.textProductReview.visibility =
            if (orderedProduct.reviewStatus) View.GONE else View.VISIBLE
        holderProduct.productDivider.visibility =
            if (orderedProduct.reviewStatus) View.GONE else View.VISIBLE
        if (!orderedProduct.reviewStatus) {
            holderProduct.textProductReview.safeClickListener {
                click(
                    holderProduct.adapterPosition,
                    (list[holderProduct.adapterPosition] as OrderedProductEntity)
                )
            }
        }
    }

    fun setPickupAddress(isForPickupAddress: Boolean) {
        this.isForPickupAddress = isForPickupAddress
    }

    override fun getItemViewType(position: Int): Int {
        if (isFor == AppConstant.ListingType.PRODUCT_LIST){
            if (list[position] is tradly.social.common.uiEntity.AdView){
                return AppConstant.ListingType.AD_VIEW
            }
        }
        return isFor
    }

    fun getSelectedList(): List<Group> {
        val mlist = list as List<Group>
        return mlist.filter { c -> c.isSelected }
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    @Synchronized
    fun likeAction(pos:Int , holder: ListingViewHolder){
        if(NetworkUtil.isConnectingToInternet()){
            if(AppController.appController.getUser() != null){
                val productItem = list[pos] as Product
                productItem.isLiked = !productItem.isLiked
                likeListing(productItem.id,productItem.isLiked,holder)
                notifyItemChanged(pos)
                ctx.showToast(if(productItem.isLiked)R.string.wishlist_item_added_to_wish_list else R.string.wishlist_item_deleted_to_wish_list)
            }
            else{
                ctx.startActivity(AuthenticationActivity::class.java)
            }
        }
    }
    private fun likeListing(productId:String, isForLike:Boolean, holder: ListingViewHolder){
        GlobalScope.launch(Dispatchers.IO) {
            holder.getProducts.likeProduct(productId, isForLike)
        }
    }
}