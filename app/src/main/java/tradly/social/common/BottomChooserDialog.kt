package tradly.social.common

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tradly.social.R
import tradly.social.common.BottomChooserDialog.Type.CAMERA
import tradly.social.common.BottomChooserDialog.Type.DOCUMENT
import tradly.social.common.BottomChooserDialog.Type.GALLERY
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_account_chooser.view.*
import kotlinx.android.synthetic.main.dialog_make_offer.view.*
import kotlinx.android.synthetic.main.dialog_active_account.view.*
import kotlinx.android.synthetic.main.dialog_bottom_review.view.*
import kotlinx.android.synthetic.main.list_item_my_order_details.view.*
import kotlinx.android.synthetic.main.list_item_review_photo.view.*
import kotlinx.android.synthetic.main.media_attachment_dialog.view.*
import tradly.social.adapter.ListingAdapter
import tradly.social.common.base.*
import tradly.social.common.cache.CurrencyCache
import tradly.social.common.util.parser.extension.toJson
import tradly.social.common.util.parser.extension.toList
import tradly.social.common.util.parser.extension.toObject
import tradly.social.domain.entities.ImageFeed
import tradly.social.domain.entities.Category
import tradly.social.domain.entities.OrderedProductEntity
import tradly.social.domain.entities.Store

class BottomChooserDialog:BottomSheetDialogFragment(), MediaHandler.View {

    object DialogType{
        const val MEDIA = 1
        const val MANAGE_LIST = 2
        const val ACCOUNT_CHOOSER = 3
        const val CATEGORY_CHOOSER = 4
        const val ADD_REVIEW = 5
        const val ACCOUNT_ACTIVATE = 6
        const val MAKE_OFFER = 7
        const val SEARCH_BY = 8
    }

    object Type{
        const val CAMERA = 1
        const val GALLERY = 2
        const val DOCUMENT =3
    }

    object ManageListAction{
        const val EDIT = 1
        const val MARK_AS_SOLD = 2
        const val DELETE = 3
    }

    companion object{
        const val ARG_CURRENCY = "arg_currency"
        const val ARG_PRODUCT_PRICE = "arg_product_price"
    }

    private val REVIEW_MAX_PHOTO = 3
    private var type:Int = DialogType.MEDIA
    private var listener: DialogListener? = null
    private lateinit var listingAdapter: ListingAdapter
    private var categoryList = mutableListOf<Category>()
    private var reviewImageList = mutableListOf<ImageFeed>()
    private var colorPrimary:Int=0
    private var mView:View?=null
    private var selectedOpinion:Int = 0
    val mediaHandler:MediaHandler by lazy { MediaHandler(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(type == DialogType.ACCOUNT_CHOOSER || type==DialogType.ADD_REVIEW || type == DialogType.ACCOUNT_ACTIVATE){
            setStyle(STYLE_NORMAL,R.style.BottomSheetDialogTheme)
        }
        else if (type == DialogType.MAKE_OFFER){
            setStyle(STYLE_NORMAL, ThemeUtil.getResourceValue(context,R.attr.bottomSheetDialogIsFloat))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(getInflateView(), container, false)
        colorPrimary = ThemeUtil.getResourceValue(context, R.attr.colorPrimary)
        prepareView(mView)
        setListener(mView)
        arguments?.let {setData(it)}
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
    private fun setListener(view: View?){
        when(type){
            DialogType.MEDIA,
            DialogType.MANAGE_LIST->{
                view?.actionOne?.setOnClickListener {
                    when(type){
                        DialogType.MEDIA->listener?.onClick(CAMERA)
                        DialogType.MANAGE_LIST->listener?.onClick(ManageListAction.EDIT)
                    }
                    dialog?.dismiss()
                }
                view?.actionTwo?.setOnClickListener {
                    when(type){
                        DialogType.MEDIA->listener?.onClick(GALLERY)
                        DialogType.MANAGE_LIST->listener?.onClick(ManageListAction.MARK_AS_SOLD)
                    }
                    dialog?.dismiss()
                }

                view?.actionThree?.setOnClickListener {
                    when(type){
                        DialogType.MEDIA->listener?.onClick(DOCUMENT)
                        DialogType.MANAGE_LIST->listener?.onClick(ManageListAction.DELETE)
                    }
                    dialog?.dismiss()
                }
            }
            DialogType.ADD_REVIEW->{
                view?.opinion1?.setOnClickListener {
                    resetOpinion()
                    selectedOpinion = 1
                    view.opinion1?.setTextColor(ContextCompat.getColor(context!!,R.color.white))
                    view.opinion1?.setChipBackgroundColorResource(colorPrimary)
                }
                view?.opinion2?.setOnClickListener {
                    resetOpinion()
                    selectedOpinion = 2
                    view.opinion2?.setTextColor(ContextCompat.getColor(context!!,R.color.white))
                    view.opinion2?.setChipBackgroundColorResource(colorPrimary)
                }
                view?.opinion3?.setOnClickListener {
                    resetOpinion()
                    selectedOpinion = 3
                    view.opinion3?.setTextColor(ContextCompat.getColor(context!!,R.color.white))
                    view.opinion3?.setChipBackgroundColorResource(colorPrimary)
                }
                view?.opinion4?.setOnClickListener {
                    resetOpinion()
                    selectedOpinion = 4
                    view.opinion4?.setTextColor(ContextCompat.getColor(context!!,R.color.white))
                    view.opinion4?.setChipBackgroundColorResource(colorPrimary)
                }
                view?.btnSendReview?.safeClickListener {
                    if(view.ratingBar?.rating!=0f){
                        val id:String = if(arguments.getStringOrEmpty(AppConstant.BundleProperty.TYPE) == AppConstant.ModuleType.LISTINGS){
                            arguments!!.getString(AppConstant.BundleProperty.PRODUCT_DETAIL).toObject<OrderedProductEntity>()!!.id.toString()
                        } else{
                            arguments!!.getString(AppConstant.BundleProperty.STORE).toObject<Store>()!!.id
                        }
                        val bundle = Bundle()
                        bundle.putString(AppConstant.BundleProperty.ID,id)
                        bundle.putString(AppConstant.BundleProperty.REVIEW_CONTENT,view.edReview.getString())
                        bundle.putInt(AppConstant.BundleProperty.REVIEW_TITLE,selectedOpinion)
                        bundle.putInt(AppConstant.BundleProperty.REVIEW_RATING,view.ratingBar.rating.toInt())
                        if(reviewImageList.size>0){
                            reviewImageList.removeAll { i->i.isAddItem }
                            bundle.putString(AppConstant.BundleProperty.IMAGES,reviewImageList.toJson<List<ImageFeed>>())
                        }
                        listener?.onClick(bundle)
                    }
                    else{
                        context.showToast(R.string.addreview_please_provide_rating)
                    }
                }
            }
            DialogType.ACCOUNT_ACTIVATE->{
                view?.btnInactive?.safeClickListener { listener?.onClick(!arguments!!.getBoolean(
                    AppConstant.BundleProperty.IS_ACTIVE)) }
                view?.btnCancel?.setOnClickListener{dismiss()}
            }
            DialogType.MAKE_OFFER->{
                view?.btnMakeOffer?.safeClickListener {
                    if (view.etAmount.getString().isNotEmpty()){
                        val productPrice = arguments!!.getDouble(ARG_PRODUCT_PRICE,0.0)
                        val currency = CurrencyCache.getDefaultCurrency()?.symbol
                        if (view.etAmount.getString().toInt()<arguments!!.getDouble(ARG_PRODUCT_PRICE,0.0)){
                            dismiss()
                            listener?.onClick(view.etAmount.getString())
                        }
                        else{
                            context?.showToast(getString(R.string.makeoffer_alert_less_amount,currency,productPrice.toString()))
                        }
                    }
                }
            }
        }
    }

    private fun resetOpinion(){
        mView?.let {
            with(it){
                opinion1?.setTextColor(ContextCompat.getColor(context!!,R.color.colorTextBlack))
                opinion2?.setTextColor(ContextCompat.getColor(context!!,R.color.colorTextBlack))
                opinion3?.setTextColor(ContextCompat.getColor(context!!,R.color.colorTextBlack))
                opinion4?.setTextColor(ContextCompat.getColor(context!!,R.color.colorTextBlack))
                opinion1?.setChipBackgroundColorResource(android.R.color.transparent)
                opinion2?.setChipBackgroundColorResource(android.R.color.transparent)
                opinion3?.setChipBackgroundColorResource(android.R.color.transparent)
                opinion4?.setChipBackgroundColorResource(android.R.color.transparent)
                opinion1?.chipStrokeWidth = 1f
                opinion2?.chipStrokeWidth = 1f
                opinion3?.chipStrokeWidth = 1f
                opinion4?.chipStrokeWidth = 1f
            }
        }
    }

    private fun prepareView(view:View?){
        when(type){
            DialogType.MEDIA->{
                view?.iconOne?.setImageResource(R.drawable.ic_photo_black_24dp)
                view?.txtOne?.setText(R.string.camera)
                view?.iconOne?.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark), android.graphics.PorterDuff.Mode.SRC_IN)

                view?.iconTwo?.setImageResource(R.drawable.ic_gallery_black_24dp)
                view?.iconTwo?.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.holo_purple), android.graphics.PorterDuff.Mode.SRC_IN)
                view?.txtTwo?.setText(R.string.gallery)

                arguments?.let {
                    if(it.getBoolean("hasDoc",false)){
                        view?.actionThree?.visibility = View.VISIBLE
                        view?.iconThree?.setImageResource(R.drawable.ic_document_24dp)
                        view?.txtThree?.setText(R.string.document)
                        view?.iconThree?.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark), android.graphics.PorterDuff.Mode.SRC_IN)

                    }
                }
            }
            DialogType.MANAGE_LIST->{
                view?.iconOne?.setImageResource(R.drawable.ic_mode_edit_black_24dp)
                view?.txtOne?.setText(R.string.product_edit)
                view?.iconOne?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorDarkGrey), android.graphics.PorterDuff.Mode.SRC_IN)

                view?.iconTwo?.setImageResource(R.drawable.ic_sold)
                view?.txtTwo?.setText(R.string.product_mark_as_sold)


                view?.actionThree?.visibility = View.VISIBLE
                view?.txtThree?.setText(R.string.product_delete)
                view?.iconThree?.setImageResource(R.drawable.ic_delete_black_24dp)
                view?.txtThree?.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorRed))
                view?.iconThree?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorDarkGrey), android.graphics.PorterDuff.Mode.SRC_IN)

            }
            DialogType.ACCOUNT_CHOOSER->{
                listingAdapter = ListingAdapter(requireContext(),categoryList,
                    AppConstant.ListingType.ACCOUNT_CHOOSER_LIST, view?.acctRecycler){ pos, obj ->
                    listener?.onClick(obj)
                    dismiss()
                }
                view?.acctRecycler?.layoutManager = GridLayoutManager(requireContext(), 4)
                view?.acctRecycler?.adapter = listingAdapter
            }
            DialogType.CATEGORY_CHOOSER->{
                val list = arguments!!.getString(AppConstant.BundleProperty.CATEGORY_LIST).toList<Category>()
                setCategoryList(view,list)
            }
            DialogType.ADD_REVIEW-> setHeaderDetail(view)
            DialogType.ACCOUNT_ACTIVATE-> setAccountInActiveDetail(view!!)
            DialogType.MAKE_OFFER->{
                CurrencyCache.getDefaultCurrency()?.let { currency ->
                    view?.etAmount?.hint = currency.symbol
                }?:run{
                    arguments?.let {
                        view?.etAmount?.hint = it.getString(ARG_CURRENCY)
                    }
                }
            }
        }
    }

    fun setDialogType(type:Int = 1){
        this.type = type
    }

    fun  setListener(listener: DialogListener){
        this.listener = listener
    }

    private fun setData(bundle: Bundle){
        when(type){
            DialogType.ACCOUNT_CHOOSER->{
                val list = bundle.getString("categoryList").toList<Category>()
                list?.let {
                    categoryList.clear()
                    categoryList.addAll(it)
                    listingAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun setCategoryList(view:View?,list: List<Category>){
        val recyclerView:RecyclerView? = view?.findViewById(R.id.recycler_view)
        recyclerView?.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        val adapter = ListingAdapter(requireContext(),list,
            AppConstant.ListingType.CATEGORY_LIST,recyclerView){ position, obj ->
            listener?.onClick(obj)
        }
        recyclerView?.adapter = adapter

    }

    private fun setHeaderDetail(view:View?){
        if(arguments.getStringOrEmpty(AppConstant.BundleProperty.TYPE) == AppConstant.ModuleType.LISTINGS){
            val orderedProductEntity = arguments!!.getString(AppConstant.BundleProperty.PRODUCT_DETAIL).toObject<OrderedProductEntity>()
            view?.productDetailHeader?.visibility = View.VISIBLE
            view?.imageOrderProduct?.setImageByUrl(context!!,orderedProductEntity!!.image,R.drawable.placeholder_image)
            view?.textProductName?.text = orderedProductEntity!!.name
            view?.textProductDescription?.text = orderedProductEntity.description
            view?.textProductUnits?.text = orderedProductEntity.units
            view?.textProductPrice?.text = orderedProductEntity.price
            view?.textProductPrice?.setTextColor(ContextCompat.getColor(requireContext(), colorPrimary))
            view?.productDivider?.visibility = View.GONE
            view?.textProductReview?.visibility = View.GONE
        }
        else if(arguments.getStringOrEmpty(AppConstant.BundleProperty.TYPE) == AppConstant.ModuleType.ACCOUNTS){
            view?.image?.visibility = View.VISIBLE
            view?.title?.visibility = View.VISIBLE
            view?.description?.visibility = View.VISIBLE
            val store = arguments!!.getString(AppConstant.BundleProperty.STORE).toObject<Store>()
            view?.image?.setImageByUrl(requireContext(),store!!.storePic,R.drawable.placeholder_image)
            view?.title?.text = store!!.storeName
            view?.description?.text = store.storeDescription
        }
        initReviewLayout()
    }

    private fun setAccountInActiveDetail(view: View){
        val active = arguments!!.getBoolean(AppConstant.BundleProperty.IS_ACTIVE,false)
        val actionMsg = getString(if(active) R.string.storedetail_inactive else R.string.storedetail_active)
        view.txtBtnInactive.text = actionMsg
        view.txtInActiveStore.text = String.format(getString(R.string.storedetail_active_inactive_your_store),actionMsg)
        view.txtInactiveDesc.text = String.format(getString(R.string.storedetail_active_info),actionMsg)
        val drawable = ContextCompat.getDrawable(context!!,R.drawable.bg_full_green_round_rect) as GradientDrawable
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.cornerRadius = 8f
        view.btnInactive.background = Utils.getRippleDrawable(ContextCompat.getColor(requireContext(),R.color.defaultRipple),drawable)
        view.btnCancel.background = Utils.getRippleDrawable(requireActivity(),1,colorPrimary,R.color.white,8f,ContextCompat.getColor(requireContext(),R.color.defaultRipple))
        view.txtBtnCancel?.setTextColor(ContextCompat.getColor(requireContext(), colorPrimary))
    }

    private fun getInflateView() =
        when(type){
            DialogType.MEDIA,
            DialogType.MANAGE_LIST-> R.layout.media_attachment_dialog
            DialogType.ACCOUNT_CHOOSER->R.layout.dialog_account_chooser
            DialogType.CATEGORY_CHOOSER->R.layout.dialog_category_chooser
            DialogType.ADD_REVIEW->R.layout.dialog_bottom_review
            DialogType.ACCOUNT_ACTIVATE->R.layout.dialog_active_account
            DialogType.MAKE_OFFER-> R.layout.dialog_make_offer
            else->R.layout.media_attachment_dialog
        }

    private fun initReviewLayout(){
        reviewImageList.clear()
        reviewImageList.add(ImageFeed(isAddItem = true))
        updateReviewImages()
    }

    private fun updateReviewImages(){
        mView?.reviewPhotoLayout?.removeAllViews()
        for(index in reviewImageList.indices){
            val item = reviewImageList[index]
            mView?.reviewPhotoLayout?.addView(getChildItem(item,item.isAddItem).also { it.tag = index })
        }
    }

    private fun getChildItem(imageFeed: ImageFeed, isAddPhoto:Boolean):ConstraintLayout{
        val child = layoutInflater.inflate(R.layout.list_item_review_photo,null) as ConstraintLayout
        val layoutParam = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT)
        layoutParam.weight = 1f
        layoutParam.marginStart = 6
        layoutParam.marginEnd = 6
        if(isAddPhoto){
            val drawable = ContextCompat.getDrawable(context!!,R.drawable.bg_dotted_line) as GradientDrawable
            drawable.shape = GradientDrawable.RECTANGLE
            drawable.cornerRadius = 8f
            drawable.setStroke(Utils.getPixel(requireContext(),1),ContextCompat.getColor(requireContext(),R.color.colorMediumGrey),10f,10f)
            child.background = drawable
            child.imageAddPlaceHolder?.visibility = View.VISIBLE
            child.iconClear?.visibility = View.GONE
            child.iconClear?.setOnClickListener(null)
            child.setOnClickListener {
                Utils.showMediaChooseDialog(requireContext()){ selectedOpinion->
                    if(selectedOpinion == R.id.clPhotoView){
                        mediaHandler.openCamera(this)
                    }
                    else{
                        mediaHandler.openGallery(this)
                    }
                }
            }
        }
        else{
            child.imageAddPlaceHolder?.visibility = View.GONE
            child.reviewImage?.visibility = View.VISIBLE
            child.iconClear?.visibility = View.VISIBLE
            child.iconClear?.setOnClickListener {
                reviewImageList.removeAt(child.tag as Int)
                if(reviewImageList.find { i->i.isAddItem } == null){
                    reviewImageList.add(reviewImageList.size-1, ImageFeed(isAddItem = true))
                }
                updateReviewImages()
            }
            ImageHelper.getInstance().showImage(context!!,imageFeed.filePath,child.reviewImage,R.drawable.placeholder_image,R.drawable.placeholder_image)
        }
        child.layoutParams = layoutParam
        return child
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mediaHandler.onRequestPermissionsResult(this,requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mediaHandler.onActivityResult(this,requestCode, resultCode, data)
    }

    override fun onMediaResult(filePath: String) {
        reviewImageList.add(reviewImageList.size-1, ImageFeed(filePath))
       if(reviewImageList.size== REVIEW_MAX_PHOTO+1){
           reviewImageList.removeAt(reviewImageList.size-1)
       }
        updateReviewImages()
    }
}