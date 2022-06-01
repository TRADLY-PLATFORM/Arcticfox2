package tradly.social.ui.variant

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import tradly.social.R
import tradly.social.adapter.ListingAdapter
import tradly.social.common.base.AppConstant
import tradly.social.common.BaseDialogFragment
import tradly.social.common.base.ImageHelper
import tradly.social.common.base.ThemeUtil
import tradly.social.common.base.ErrorHandler
import tradly.social.common.network.parseHelper.ParseManager
import tradly.social.domain.common.VariantParser
import tradly.social.domain.entities.*
import tradly.social.ui.cart.CartListActivity
import tradly.social.ui.cart.CartPresenter
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_variant_dialog.*
import kotlinx.android.synthetic.main.layout_variant_dialog.view.*

class DialogVariant : BaseDialogFragment(), CartPresenter.View {

    var listingAdapter: ListingAdapter? = null
    var cartPresenter:CartPresenter?=null
    var mView: View? = null
    var product: Product? = null
    var variantCollection: List<HashMap<String, Any?>>? = null
    val variantList = mutableListOf<Variant>()
    var colorPrimary: Int = 0
    var isInCart:Boolean=false
    var variant:Variant?=null
    var actionBtnListener: View.OnClickListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, ThemeUtil.getSelectedTheme())
        colorPrimary = ThemeUtil.getResourceValue(requireContext(), R.attr.colorPrimary)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.layout_variant_dialog, container, false)
        mView?.recycler_view?.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        cartPresenter = CartPresenter(this)
        listingAdapter = ListingAdapter(
            requireContext(),
            variantList,
            AppConstant.ListingType.VARIANT,
            mView?.recycler_view) { position, obj ->
            val map = variantCollection?.filter {
                (it["collections"] as List<String>).containsAll(
                    getSelectedVariant(variantList)
                )
            }?.singleOrNull()
            if (map != null) {
                mView?.combinationTxt?.visibility = View.GONE
                mView?.actionBtn?.setBackgroundColor(ContextCompat.getColor(requireContext(), colorPrimary))
                mView?.actionBtn?.setOnClickListener(actionBtnListener)
                setHeader(map["parent"] as? Variant)
            } else {
                mView?.combinationTxt?.visibility = View.VISIBLE
                mView?.actionBtn?.setOnClickListener(null)
                mView?.actionBtn?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorMediumGrey))
            }
        }
        mView?.recycler_view?.adapter = listingAdapter
        mView?.toolbar?.setNavigationOnClickListener {
            dialog?.dismiss()
        }
        actionBtnListener = object : View.OnClickListener {
            override fun onClick(p0: View?) {
                if(!isInCart){
                    cartPresenter?.addCartItem(variant?.id, AppConstant.CartType.CART_VARIANT)
                }
                else{
                    val intent = Intent(requireContext(), CartListActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        arguments?.let {
            product = Gson().fromJson(it.getString("product"), Product::class.java)
            variantList.clear()
            VariantParser.parseVariant(product?.variants) { propertyCollection, variantCollection ->
                setHeader(variantCollection[0]["parent"] as? Variant)
                variantList.clear()
                variantList.addAll(propertyCollection)
                listingAdapter?.notifyDataSetChanged()
                this.variantCollection = variantCollection
            }
        }
        return mView
    }

    private fun setHeader(variant: Variant?) {
        variant?.let {
            cartPresenter?.findItemInCart(it.id, AppConstant.CartType.CART_PRODUCT)
            this.variant = it
            var variantImage:String = if(it.images.count()>0) it.images[0] else product?.images?.let { it[0]  }?:run{""}
            ImageHelper.getInstance().showImage(
                requireContext(),
                variantImage,
                mView?.productImg,
                R.drawable.placeholder_image,
                R.drawable.placeholder_image
            )
            mView?.txtActualPrice?.text =
                String.format(getString(R.string.two_data), ParseManager.getInstance().getUserCurrency(), it.offerDisplayPrice)
            mView?.txtActualPrice?.let {
                it.paintFlags = it.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            mView?.txtOffer?.text = String.format(getString(R.string.off_data), variant?.offerPercent.toString())
            mView?.txtPrice?.text = String.format(
                requireContext().getString(R.string.two_data),
                ParseManager.getInstance().getUserCurrency(),
                variant?.offerDisplayPrice
            )
            mView?.actionBtn?.setOnClickListener(actionBtnListener)
        }

    }

    private fun getSelectedVariant(list: List<Variant>): List<String> {
        val mList = mutableListOf<String>()
        list.forEach { mList.add(it.variantValue) }
        return mList
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
        }
    }

    override fun isItemInCart(isInItem: Boolean) {
        contentLayout?.visibility = View.VISIBLE
        this.isInCart = isInItem
        btnTxt?.text = getString(if (isInItem) R.string.product_go_to_cart else R.string.product_add_to_cart)
    }
    override fun onCartAdded() {
        this.isInCart = true
        Toast.makeText(requireContext(),getString(R.string.cart_item_added_cart),Toast.LENGTH_SHORT).show()
        btnTxt?.text = getString(R.string.product_go_to_cart)
    }

    override fun onCartItemRemoved() {

    }

    override fun onFailure(appError: AppError) {
        ErrorHandler.handleError(requireContext(),appError)
    }

    override fun onSuccess(cartResult: Cart,isFor:Int) {

    }

    override fun showCurrentAddress(shippingAddress: ShippingAddress?) {

    }
    override fun showProgressDialog() {
        showLoader(R.string.please_wait)
    }

    override fun hideProgressDialog() {
        hideLoader()
    }

    override fun showShipmentList(list: List<ShippingMethod>) {

    }

    override fun showProgressLoader() {

    }

    override fun hideProgressLoader() {

    }

    override fun noNetwork() {

    }
}