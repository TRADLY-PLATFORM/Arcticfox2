package tradly.social.ui.message.list

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.chat_list_fragment.*
import tradly.social.R
import tradly.social.adapter.ListingAdapter
import tradly.social.common.*
import tradly.social.common.base.*
import tradly.social.common.util.parser.extension.toJson
import tradly.social.domain.entities.*
import tradly.social.ui.login.AuthenticationActivity
import tradly.social.ui.message.thread.ChatThreadActivity


class ChatListFragment : BaseFragment(),ChatListPresenter.View {

    var mView:View?=null
    private var presenter: ChatListPresenter? = null
    private var listingAdapter: ListingAdapter? = null
    private var chatRoomList = mutableListOf<ChatRoom>()
    var user:User?= AppController.appController.getUser()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(mView != null){
            return mView
        }
        mView = inflater.inflate(R.layout.chat_list_fragment, container, false)
        presenter = ChatListPresenter(this)
        listingAdapter = ListingAdapter(requireContext(),chatRoomList, AppConstant.ListingType.CHAT_ROOM,recycler_view){ position, obj ->
            val chatRoom = obj as? ChatRoom
            chatRoom?.let { chatRoom->
                val intent = Intent(requireContext(), ChatThreadActivity::class.java)
                intent.putExtra("chatRoom", chatRoom.toJson<ChatRoom>())
                startActivity(intent)
                user?.let { user->
                    MessageHelper.updateChatRoom(chatRoom.updatedBy,chatRoom.receiverId,user.id,chatRoom.id)
                }
            }
        }
        return mView
    }

    private fun initList(){
        chatRoomList.clear()
        listingAdapter?.notifyDataSetChanged()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_view.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
        recycler_view.adapter = listingAdapter
        getChatList()
    }
    private fun getChatList(){
        initList()
        AppController.appController.getUser()?.let {
            llEmptyStateChats?.visibility = View.GONE
            presenter?.getChatRoomList(it.id)
        }?:run{
            llEmptyStateChats?.visibility = View.VISIBLE
            txtEmptyStateMsg?.visibility = View.GONE
            btnLogin?.visibility = View.VISIBLE
            btnLogin?.setOnClickListener {
                val intent = Intent(requireContext(),AuthenticationActivity::class.java)
                intent.putExtra("isFor", AppConstant.LoginFor.CHAT)
                startActivityForResult(intent, AppConstant.ActivityResultCode.LOGIN_FROM_CHAT_LIST)
            }
        }
    }
    override fun noInternet() {
        requireContext().showToast(getString(R.string.no_internet))
    }

    override fun onFailure(appError: AppError) {
        ErrorHandler.handleError(requireContext(), appError)
    }

    override fun onLoadList(list: ArrayList<ChatRoom>) {
        chatRoomList.clear()
        chatRoomList.addAll(list)
        listingAdapter?.notifyDataSetChanged()
        if(chatRoomList.isEmpty()){
            llEmptyStateChats?.visibility = View.VISIBLE
            txtEmptyStateMsg?.visibility = View.VISIBLE
            txtEmptyStateMsg?.text = getString(R.string.chat_no_new_message)
            btnLogin?.visibility = View.GONE
        }
        else{
            llEmptyStateChats?.visibility = View.GONE
        }
    }

    override fun hideProgressLoader() {
        progressBar?.visibility = View.GONE
    }

    override fun showProgressLoader() {
        progressBar?.visibility = View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == AppConstant.ActivityResultCode.LOGIN_FROM_CHAT_LIST && data != null){
            user = AppController.appController.getUser()
            getChatList()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter?.removeChatListListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.onDestroy()
    }
}
