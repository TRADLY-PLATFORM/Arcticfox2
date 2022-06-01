package tradly.social.common.event.addEvent.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import tradly.social.common.base.BaseViewModel
import tradly.social.common.base.FileHelper
import tradly.social.common.base.SingleEvent
import tradly.social.common.base.UIState
import tradly.social.common.network.RequestID
import tradly.social.common.network.feature.addEvent.datasource.VariantDataSourceImpl
import tradly.social.common.network.feature.common.datasource.ParseMediaDataSource
import tradly.social.domain.entities.*
import tradly.social.domain.repository.MediaRepository
import tradly.social.domain.repository.VariantRepository
import tradly.social.domain.usecases.*
import java.io.File

class VariantViewModel : BaseViewModel() {

    private val variantRepository by lazy { VariantRepository(VariantDataSourceImpl()) }
    private val getVariantTypeUC = GetVariantTypeUc(variantRepository)
    private val addVariantUc = AddVariantUc(variantRepository)
    private val deleteVariantUC = DeleteVariantUc(variantRepository)
    private val updateVariantUC = UpdateVariantUseCase(variantRepository)
    private val imageMediaUC = UploadMedia(MediaRepository(ParseMediaDataSource()))
    private val _variantTypeLiveData by lazy { MutableLiveData<List<VariantType>>() }
    val variantTypeLiveData: LiveData<List<VariantType>> = _variantTypeLiveData
    private val _variantTypeResultLiveData by lazy { MutableLiveData<SingleEvent<Unit>>() }
    val variantTypeResultLiveData: LiveData<SingleEvent<Unit>> = _variantTypeResultLiveData
    private val _onUploadImageLiveData by lazy { MutableLiveData<List<String>>() }
    val onUploadImageLiveData:LiveData<List<String>> = _onUploadImageLiveData
    private val _onDeleteVariantLiveData by lazy { MutableLiveData<SingleEvent<Unit>>() }
    val onDeleteVariantLiveData:LiveData<SingleEvent<Unit>> = _onDeleteVariantLiveData
    private val _onUpdateVariantLiveData by lazy { MutableLiveData<SingleEvent<Unit>>() }
    val onUpdateVariantLiveData:LiveData<SingleEvent<Unit>> = _onUpdateVariantLiveData

    private val _onAddVariantLiveData by lazy { MutableLiveData<SingleEvent<Variant>>() }
    val onAddVariantLiveData:LiveData<SingleEvent<Variant>> = _onAddVariantLiveData


    fun getVariantTypes() {
        getApiResult(viewModelScope, { getVariantTypeUC.execute() }, { result ->
            _variantTypeLiveData.value = result
            _variantTypeResultLiveData.value = SingleEvent(Unit)
        }, true, RequestID.GET_VARIANT_TYPES)
    }

    fun addVariant(eventId: String,variant: Variant){
        getApiResult(viewModelScope,{ addVariantUc.addVariant(eventId,variant)},{ variant ->
            _onAddVariantLiveData.value = SingleEvent(variant)
        },true,RequestID.ADD_VARIANT)
    }

    fun updateVariant(variant:Variant,listingId:String){
        getApiResult(viewModelScope,{updateVariantUC.updateVariant(listingId,variant)},{
            _onUpdateVariantLiveData.value = SingleEvent(Unit)
        },true,RequestID.UPDATE_VARIANT)
    }

    fun deleteVariant(variantId:String,listingId:String){
        getApiResult(viewModelScope,{deleteVariantUC.deleteVariant(listingId,variantId)},{
            _onDeleteVariantLiveData.value = SingleEvent(Unit)
        },true,RequestID.DELETE_VARIANT)
    }

    fun uploadImages(imageFeedList: List<ImageFeed>){
        val fileList = imageFeedList.filter { !it.filePath.startsWith("http") }.map {
            FileInfo(
                fileUri = it.filePath,
                type = FileHelper.getMimeType(it.filePath),
                name = File(it.filePath).name
            )
        }
        getApiResult(viewModelScope,{imageMediaUC.invoke(fileList,true)},{ resultList->
            val imageList = imageFeedList.filter { it.filePath.startsWith("http") }.map { it.filePath }.toMutableList()
            imageList.addAll(resultList.map { it.uploadedUrl })
            _onUploadImageLiveData.value = imageList
        },true,RequestID.UPLOAD_IMAGES)
    }

    override fun showAPIProgress(show: Boolean, apiId: Int) {
        _uiState.value = SingleEvent(UIState.Loading(show, apiId))
    }

    override fun onFailure(apiError: AppError) {
        showAPIProgress(false, apiError.apiId)
        _uiState.value = SingleEvent(UIState.Failure(apiError))
    }
}