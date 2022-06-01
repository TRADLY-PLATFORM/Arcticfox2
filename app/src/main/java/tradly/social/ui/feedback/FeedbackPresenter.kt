package tradly.social.ui.feedback

import kotlinx.coroutines.*
import tradly.social.R
import tradly.social.common.base.FileHelper
import tradly.social.common.base.NetworkUtil
import tradly.social.common.base.Utils
import tradly.social.domain.entities.Device
import tradly.social.data.model.CoroutinesManager
import tradly.social.data.model.dataSource.ParseFeedbackDataSource
import tradly.social.common.network.feature.common.datasource.ParseMediaDataSource
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Category
import tradly.social.domain.entities.FileInfo
import tradly.social.domain.entities.Result
import tradly.social.domain.repository.FeedbackRepository
import tradly.social.domain.repository.MediaRepository
import tradly.social.domain.usecases.SendFeedback
import tradly.social.domain.usecases.UploadMedia
import java.io.File
import kotlin.coroutines.CoroutineContext

class FeedbackPresenter(var view:View?): CoroutineScope {

    private var job: Job
    private var uploadMedia: UploadMedia
    private var sendFeedback:SendFeedback
    interface View {
        fun onFailure(appError: AppError)
        fun onSuccess()
        fun showProgressDialog(msg:Int , title: Int = 0)
        fun changeProgressMessage(msg:Int , title: Int = 0)
        fun hideProgressDialog()
        fun onMediaUploadFailed(msg:Int)
        fun showFieldError(id:Int,msg:Any)
        fun showCategories(list: List<Category>)
    }

    init {
        val mediaDataSource = ParseMediaDataSource()
        val mediaRepository = MediaRepository(mediaDataSource)
        uploadMedia = UploadMedia(mediaRepository)
        val feedbackDataSource = ParseFeedbackDataSource()
        val feedbackRepository = FeedbackRepository(feedbackDataSource)
        sendFeedback = SendFeedback(feedbackRepository)
        job = SupervisorJob()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    fun loadCategories(){
        if(NetworkUtil.isConnectingToInternet(true)){
            view?.showProgressDialog(R.string.please_wait)
            CoroutinesManager.ioThenMain(job,{sendFeedback.getCategories()},{
                when(it){
                    is Result.Success->view?.showCategories(it.data)
                    is Result.Error->view?.onFailure(it.exception)
                }
                view?.hideProgressDialog()
            })
        }
    }

    fun sendFeedback(categoryName:String,mood:Int,email:String,title:String,addInfo:String,attachmentList:List<FileInfo>){
        if(isValid(title)){
            if(NetworkUtil.isConnectingToInternet(true)){
                if(attachmentList.isNotEmpty()){
                    view?.showProgressDialog(R.string.feedback_upload_attachment, R.string.please_wait)
                    CoroutinesManager.ioThenMain(job,{
                        uploadMedia.invoke(getFileInfoList(attachmentList),true)
                    },{result->
                        when(result){
                            is Result.Success->{
                                view?.changeProgressMessage(R.string.feedback_sending_feedback)
                                Utils.getDeviceDetail {
                                    sendFeedback(categoryName, mood, email, title, addInfo, result.data,it)
                                }
                            }
                            is Result.Error->{
                                view?.hideProgressDialog()
                                view?.onMediaUploadFailed(R.string.media_failed_msg)
                            }
                        }

                    })
                }
                else{
                    view?.showProgressDialog(R.string.feedback_sending_feedback)
                    Utils.getDeviceDetail {
                        sendFeedback(categoryName, mood, email, title, addInfo, attachmentList,it)
                    }
                }
            }
        }
    }

    private fun sendFeedback(categoryName:String,mood:Int,email:String,title:String,addInfo:String,attachmentList:List<FileInfo>,device: Device){
        CoroutinesManager.ioThenMain(job,{sendFeedback.sendFeedback(categoryName,mood,email,title,addInfo,attachmentList,getDeviceDetailMap(device))},{
            view?.hideProgressDialog()
            when(it){
                is Result.Success->view?.onSuccess()
                is Result.Error->view?.onFailure(it.exception)
            }
        })
    }

    private fun getFileInfoList(list:List<FileInfo>):List<FileInfo>{
        list.map {
            val file = File(it.fileUri)
            it.name = file.name
            it.type = FileHelper.getMimeType(it.fileUri)
        }
        return list
    }

    private fun getDeviceDetailMap(device: Device):HashMap<String,Any?>{
        val deviceInfo = hashMapOf<String,Any?>()
        deviceInfo["device_name"] = device.deviceName
        deviceInfo["device_manufacturer"] = device.deviceManufacturer
        deviceInfo["device_model"] = device.deviceModel
        deviceInfo["app_version"] = device.appVersion
        deviceInfo["os_version"] = device.osVersion
        deviceInfo["language"] = device.locale
        deviceInfo["client_type"] = device.clientType
        return deviceInfo
    }

    private fun isValid(title:String):Boolean{
        if(title.isEmpty()){
            view?.showFieldError(R.id.feedback_title,R.string.login_required)
            return false
        }
        return true
    }
}