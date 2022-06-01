package tradly.social.event.addevent.ui.variant.viewmodel

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import tradly.social.common.base.AppConstant
import tradly.social.common.base.BaseViewModel
import tradly.social.common.base.SingleEvent
import tradly.social.domain.entities.Currency
import tradly.social.domain.entities.Variant
import tradly.social.domain.entities.VariantProperties
import tradly.social.domain.entities.VariantType

class SharedVariantViewModel:BaseViewModel() {

    private val _selectedVariantList by lazy { MutableLiveData<List<Variant>>() }
    val selectedVariantList:LiveData<List<Variant>> = _selectedVariantList

    private val _selectedVariant by lazy { MutableLiveData<Variant?>() }
    val selectedVariant:LiveData<Variant?> = _selectedVariant

    private val _selectedVariantValueList by lazy { MutableLiveData<List<VariantProperties>>() }
    val selectedVariantValueList:LiveData<List<VariantProperties>> = _selectedVariantValueList

    private val _selectedCurrency by lazy { MutableLiveData<Currency?>() }
    val selectedCurrency:LiveData<Currency?> = _selectedCurrency

    private val _onFinishLiveData by lazy { MutableLiveData<SingleEvent<Bundle>>() }
    val onFinishLiveData:LiveData<SingleEvent<Bundle>> = _onFinishLiveData

    private val _fragmentNavigationLiveData by lazy { MutableLiveData<SingleEvent<String>>() }
    val fragmentNavigationLiveData:LiveData<SingleEvent<String>> = _fragmentNavigationLiveData

    private val _fragmentPopupLiveData by lazy { MutableLiveData<SingleEvent<Unit>>() }
    val fragmentPopupLiveData:LiveData<SingleEvent<Unit>> =_fragmentPopupLiveData

    private val _variantTypeLiveData by lazy { MutableLiveData<List<VariantType>>() }
    val variantTypeLiveData: LiveData<List<VariantType>> = _variantTypeLiveData

    fun setSelectedCurrency(currency:Currency?){
        this._selectedCurrency.value = currency
    }

    fun isVariantAvailable(variantId:Int = -1,list: List<VariantType>):Boolean{
        val selectedValues = list.map { it.id }
        _selectedVariantList.value?.filterNot { it.id.toInt()!=variantId }?.filter { it.values.size == list.size}?.forEach { variant->
            val existingVariantValue = variant.values.map { it.id.toInt() }
            if (existingVariantValue.containsAll(selectedValues)){
                return true
            }
        }
        return false
    }

    fun setSelectedVariantValueList(list:List<VariantProperties>){
        _selectedVariantValueList.value = list
    }

    fun getSelectedVariantValueList() = this._selectedVariantValueList.value

    fun setVariantTypeLiveData(list: List<VariantType>) {
        this._variantTypeLiveData.value = list
    }

    fun setOnFinish(bundle: Bundle){
        this._onFinishLiveData.value = SingleEvent(bundle)
    }

    fun setVariantList(list: List<Variant>){
        this._selectedVariantList.value = list
    }

    fun setSelectedVariant(variant: Variant?){
        this._selectedVariant.value = variant
    }

    fun setFragmentNavigation(tag:String){
        this._fragmentNavigationLiveData.value = SingleEvent(tag)
    }

    fun popFragment(){
        this._fragmentPopupLiveData.value = SingleEvent(Unit)
    }

    fun getSelectedVariant() = this.selectedVariant.value

    fun getSelectedCurrency() = this.selectedCurrency.value
}