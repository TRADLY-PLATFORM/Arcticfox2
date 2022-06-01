package tradly.social.common

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import tradly.social.R
import tradly.social.adapter.ListingAdapter
import tradly.social.common.network.parseHelper.ParseManager
import tradly.social.domain.common.VariantParser
import tradly.social.domain.entities.*
import tradly.social.domain.entities.Set
import tradly.social.ui.cart.CartListActivity
import tradly.social.ui.cart.CartPresenter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_set_variant_dialog.view.*
import tradly.social.common.base.AppConstant
import tradly.social.common.base.ImageHelper

class SetVariantDialog:BottomSheetDialogFragment(),CartPresenter.View {
    var mView:View?=null
    var set:Set?=null
    var isInCart:Boolean = false
    var listingAdapter:ListingAdapter?=null
    var dialog:ProgressDialog?=null
    var cartPresenter:CartPresenter?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.layout_set_variant_dialog, container, false)
        cartPresenter = CartPresenter(this)
        arguments?.let {
            set = Gson().fromJson(it.getString("set"),Set::class.java)
             isInCart = it.getBoolean("isInCart")
            mView?.btnTxt?.text = getString(if(isInCart)R.string.product_go_to_cart else R.string.product_add_to_cart)
        }
        set?.let {
            mView?.recycler_view?.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
            VariantParser.parseVariant(it.variants){propertyCollection, variantCollection ->
                listingAdapter = ListingAdapter(requireContext(),propertyCollection,
                    AppConstant.ListingType.SET_VARIANT_LIST,mView?.recycler_view){ position, obj ->
                }
            }

            mView?.recycler_view?.adapter = listingAdapter
        }
        set?.variants?.let {
            setHeader(it[0])
        }

        mView?.actionBtn?.setOnClickListener {
            if(isInCart){
                val intent = Intent(requireContext(),CartListActivity::class.java)
                startActivity(intent)
                dismiss()
            }
            else{
                cartPresenter?.addCartItem(set?.id, AppConstant.CartType.CART_SET)
            }
        }

        return mView
    }

    private fun setHeader(variant: Variant?){
        set?.let {
            var productImage:String? = Constant.EMPTY
            if(it.variants.count()>0){
                productImage = if(it.variants[0].images.count()>0)it.variants[0].images[0] else set?.defaultImage
            }
            ImageHelper.getInstance().showImage(requireContext(),productImage,mView?.productImg,R.drawable.placeholder_image,R.drawable.placeholder_image)
            mView?.txtActualPrice?.text = String.format(getString(R.string.two_data), ParseManager.getInstance().getUserCurrency(), variant?.offerDisplayPrice)
            mView?.txtActualPrice?.let {
                it.paintFlags = it.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            mView?.txtOffer?.text = String.format(getString(R.string.off_data), variant?.offerPercent.toString())
            mView?.txtPrice?.text = String.format(requireContext().getString(R.string.two_data), ParseManager.getInstance().getUserCurrency(), variant?.offerDisplayPrice)
        }

    }

    override fun showProgressDialog() {
        dialog = ProgressDialog(requireContext())
        dialog?.setCancelable(false)
        dialog?.setMessage(requireContext().getString(R.string.please_wait))
        dialog?.show()
    }

    override fun hideProgressDialog() {
        dialog?.let {
            it.hide()
            it.dismiss()
        }
    }

    override fun showShipmentList(list: List<ShippingMethod>) {

    }

    override fun onSuccess(cartResult: Cart, isFor: Int) {

    }

    override fun onFailure(appError: AppError) {

    }

    override fun noNetwork() {

    }
    override fun onCartItemRemoved() {

    }
    override fun showCurrentAddress(shippingAddress: ShippingAddress?) {

    }

    override fun showProgressLoader() {

    }

    override fun hideProgressLoader() {

    }

    override fun onCartAdded() {
        mView?.btnTxt?.text = getString(R.string.product_go_to_cart)
        isInCart = true
    }

    override fun isItemInCart(isInItem: Boolean) {

    }



}