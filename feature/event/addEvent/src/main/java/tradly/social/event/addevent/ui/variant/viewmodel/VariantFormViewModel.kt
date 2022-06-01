package tradly.social.event.addevent.ui.variant.viewmodel

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import tradly.social.common.base.BaseViewModel
import tradly.social.common.base.SingleEvent
import tradly.social.common.base.getOrDefault
import tradly.social.common.resources.CommonResourceString
import tradly.social.domain.entities.*
import tradly.social.event.addevent.R
import tradly.social.event.explore.common.CommonBaseResourceId

class VariantFormViewModel:BaseViewModel() {

    private var onFocusChangeListener: View.OnFocusChangeListener?=null
    private val _onFieldErrorLiveData by lazy { MutableLiveData<SingleEvent<Pair<Int,Int>>>() }
    val onFieldErrorLiveData:LiveData<SingleEvent<Pair<Int,Int>>> = _onFieldErrorLiveData
    private val _onValidationSuccess by lazy { MutableLiveData<Variant>() }
    val onValidationSuccess:LiveData<Variant> = _onValidationSuccess

    init{

    }

    fun getFocusChangeListener() = this.onFocusChangeListener

    private fun setError(fieldId:Int,errorResId:Int){
        this._onFieldErrorLiveData.value = SingleEvent(Pair(fieldId,errorResId))
    }

    private fun onValidationSuccess(currency:Currency?,imageList:List<ImageFeed>,title:String,description:String,price:String,offer:Int,stock:Int){
        val variant = Variant(
            variantName = title,
            variantDescription = description,
            offerDisplayPrice = currency?.symbol?.getOrDefault<String>()+price,
            quantity = stock,
            offerPercent = offer,
            listPrice = Price(amount = price.toDouble()),
            images = imageList.map { it.filePath }
        )
        _onValidationSuccess.value = variant
    }

    fun isValid(currency:Currency?,imageList:List<ImageFeed>,title:String,description:String,price:String,offer:Int,stock:Int,variantValueList:List<VariantProperties>?){
        when{
            imageList.isEmpty()-> setError(R.id.addViewCard,CommonResourceString.add_variant_enter_event_image_empty_error)
            title.isEmpty()-> setError(R.id.textInputEventTitle,CommonResourceString.add_variant_enter_event_title_error)
            description.isEmpty() ->  setError(R.id.textInputEventDescription,CommonResourceString.add_variant_enter_event_description_error)
            price.isEmpty() ->setError(CommonBaseResourceId.textInputEdDropDown,CommonResourceString.add_variant_enter_event_price_empty_error)
            price.toInt() == 0-> setError(CommonBaseResourceId.textInputEdDropDown,CommonResourceString.add_variant_enter_event_price_error)
            offer>100 -> setError(R.id.textInputOffer,CommonResourceString.add_variant_enter_event_offer_invalid_error)
            variantValueList.isNullOrEmpty()-> setError(R.id.btnAddVariant,CommonResourceString.add_variant_select_variant)
            else-> onValidationSuccess(currency,imageList, title, description, price, offer, stock)
        }
    }
}