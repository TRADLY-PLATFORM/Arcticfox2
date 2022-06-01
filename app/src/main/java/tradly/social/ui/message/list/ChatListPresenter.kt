package tradly.social.ui.message.list

import tradly.social.common.MessageHelper
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.ChatRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import tradly.social.common.base.AppController
import kotlin.coroutines.CoroutineContext

class ChatListPresenter(var view: View?) : CoroutineScope {
    private var job: Job

    interface View {
        fun onLoadList(list: ArrayList<ChatRoom>)
        fun onFailure(appError: AppError)
        fun noInternet()
        fun showProgressLoader()
        fun hideProgressLoader()
    }

    init {
        job = Job()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    fun getChatRoomList(userId:String) {
        view?.showProgressLoader()
        MessageHelper.getChatRoomList(userId, object : MessageHelper.Companion.ChatListener {
            override fun onSuccess(any: Any?) {
                val list = any as? ArrayList<ChatRoom>
                view?.onLoadList(list?: arrayListOf())
                view?.hideProgressLoader()
            }

            override fun onFailure(error: String?,code:Int) {
                view?.onFailure(AppError()) //TODO Pass custom error
                view?.hideProgressLoader()
            }
        })
    }

    fun removeChatListListener(){
        AppController.appController.getUser()?.let {
            MessageHelper.removeChatRoomReference(it.id)
        }
    }

    fun onDestroy() {
        removeChatListListener()
        job.cancel()
        view = null
    }
}