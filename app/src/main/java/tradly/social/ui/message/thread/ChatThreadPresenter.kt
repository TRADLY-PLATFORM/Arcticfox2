package tradly.social.ui.message.thread

import tradly.social.common.MessageHelper
import tradly.social.common.base.NetworkUtil
import tradly.social.common.network.feature.common.datasource.ParseMediaDataSource
import tradly.social.common.network.feature.common.datasource.StoreDataSourceImpl
import tradly.social.domain.entities.*
import tradly.social.domain.repository.MediaRepository
import tradly.social.domain.repository.StoreRepository
import tradly.social.domain.usecases.GetStores
import tradly.social.domain.usecases.UploadMedia
import kotlinx.coroutines.*
import tradly.social.common.base.AppController
import tradly.social.data.model.dataSource.ParseChatDataSource
import tradly.social.domain.repository.ChatRepository
import tradly.social.domain.usecases.GetNegotiation
import kotlin.coroutines.CoroutineContext


class ChatThreadPresenter(var view: View?) : CoroutineScope {
    private var job: Job
    private var uploadMedia: UploadMedia? = null
    var getStores:GetStores?=null
    val getNegotiation:GetNegotiation
    var userId:String
    var userPic:String
    var userName:String
    var user:User?
    interface View {
        fun onSuccess(list: List<Chat>)
        fun onFailure(appError: AppError)
        fun onUpdateNegotiation(isAccepted:Boolean,messageId:String,makeOffer: MakeOffer)
        fun showProgressLoader()
        fun hideProgressLoader()
        fun showProgressDialog()
        fun hideProgressDialog()
        fun noInternet()
    }

    init {
        user = AppController.appController.getUser()
        userId = user?.id?:Constant.EMPTY
        userName = user?.name?:Constant.EMPTY
        userPic = user?.profilePic?:Constant.EMPTY
        val storeDataSource = StoreDataSourceImpl()
        val storeRepository=  StoreRepository(storeDataSource)
        val mediaDataSource = ParseMediaDataSource()
        val mediaRepository = MediaRepository(mediaDataSource)
        getNegotiation = GetNegotiation(ChatRepository(ParseChatDataSource()))
        uploadMedia = UploadMedia(mediaRepository)
        getStores = GetStores(storeRepository)
        job = Job()
    }


    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    fun uploadMedia(fileInfo: FileInfo,provider: User,chatRoomId: String){
        if(NetworkUtil.isConnectingToInternet()){
            launch(Dispatchers.Main) {
                val call = async (Dispatchers.IO){ uploadMedia?.invoke(listOf(fileInfo),false) }
                when(val result = call.await()){
                    is Result.Success->{
                        view?.hideProgressDialog()
                        if(result.data.count()>0){
                            sendMsg(provider,chatRoomId,result.data[0].uploadedUrl,fileInfo.type,fileInfo.name)
                        }
                    }
                    is Result.Error->{
                        view?.hideProgressDialog()
                        view?.onFailure(result.exception)}
                }
            }
        }
    }

    fun updateNegotiation(isAccepted:Boolean,messageId: String,productId: String,makeOffer: MakeOffer,status:Int){
        if (NetworkUtil.isConnectingToInternet(true)){
            launch(Dispatchers.Main){
                view?.showProgressDialog()
                val call = async(Dispatchers.IO){ getNegotiation.updateNegotiationId(productId, makeOffer.negotiation.id, status) }
                when(val result = call.await()){
                    is Result.Success->view?.onUpdateNegotiation(isAccepted,messageId,makeOffer)
                    is Result.Error->{
                        view?.onFailure(result.exception)
                    }
                }
                view?.hideProgressDialog()
            }
        }
    }

    fun sendMsg(provider: User,chatRoomId:String,txt:String,mimeType:String,fileName:String=Constant.EMPTY ){
        sendMsg(userName, userId, provider.id, provider.name?:Constant.EMPTY, userPic, provider.profilePic?:Constant.EMPTY, chatRoomId,txt,mimeType,fileName)
    }
    private fun sendMsg(senderName: String,senderId: String,providerId: String,providerName: String,profilePic: String ,providerProfilePic: String,chatRoomId:String,txt:String,mimeType:String,fileName:String=Constant.EMPTY){
        MessageHelper.sendMsg(senderName, senderId, providerId, providerName, profilePic, providerProfilePic, chatRoomId, txt, mimeType,fileName)
    }
}