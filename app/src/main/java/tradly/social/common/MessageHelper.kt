package tradly.social.common

import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.google.firebase.database.*
import com.google.firebase.database.Transaction
import tradly.social.BuildConfig.BASE_URL
import tradly.social.R
import tradly.social.common.base.AppConstant
import tradly.social.common.base.AppController
import tradly.social.common.base.ErrorHandler
import tradly.social.common.base.FileHelper
import tradly.social.common.network.CustomError
import tradly.social.common.resources.ResourceConfig
import tradly.social.domain.entities.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MessageHelper {
   companion object{
       private var chatListener:ChatListener?=null
       var chatRoomValueEventListener:ValueEventListener?=null
       var recentMessageListener:ValueEventListener?=null
       interface ChatListener{
           fun onSuccess(any: Any?)
           fun onFailure(message:String?=Constant.EMPTY,code:Int=0)
       }
       val mDatabase: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference.child(getChatPath()) }

       init {
           FirebaseDatabase.getInstance().setPersistenceEnabled(true)
       }

       object ChatEnv{
           const val PROD = "_prod"
           const val DEV = "_dev"
           const val SANDBOX = "_sandbox"
       }
       private fun getChatPath():String{
           return when(BuildConfig.FLAVOR){
               "tradlySocial" -> "tradly"+ getChatEnv()
               else-> ResourceConfig.TENANT_ID+ getChatEnv()
           }
       }

       private fun getChatEnv()= when(BuildConfig.BUILD_TYPE){
               "release"->if(BASE_URL.contains("sandbox"))ChatEnv.SANDBOX else ChatEnv.PROD
               "debug"-> ChatEnv.DEV
               "sandBox", "sandBoxRelease"->ChatEnv.SANDBOX
               else-> throw IllegalArgumentException("Unknown BUILD_TYPE")
           }

       object Type{
           const val MSG = "msg"
           const val IMAGE = "image"
           const val AUDIO = "audio"
           const val VIDEO = "video"
           const val PRODUCT = "product"
           const val APPLICATION = "application"
           const val TEXT = "text"
           const val MAKE_OFFER = "make_offer"

           object MakeOfferType{
               const val SENT_REQUEST = 1
               const val RECEIVED_REQUEST = 2
               const val ACCEPTED_REQUEST = 3
               const val DENIED_REQUEST = 4
               const val READY_TO_BUY = 5
               const val SKIP = 6
           }
       }

       object Keys {
           const val USERS = "users"
           const val CHATS = "chats"
           const val CHAT_ROOMS = "chatrooms"
           const val CREATED_TIME = "createdTime"
           const val MESSAGES = "messages"
           const val MAKE_OFFER = "makeOffer"
           const val BUYER = "buyer"
           const val OFFER_STATUS = "offerStatus"
           const val SELLER = "seller"
           const val TYPE = "type"
           const val COUNT = "count"
           const val RECEIVER_ID = "receiverId"
           const val DELIVERY_STATUS = "deliveryStatus"
           const val UPDATED_BY = "updatedBy"
       }
       object ErrorCode{
           const val UN_AUTHORIZED = 121
       }

       object ChatDeliveryStatus{
           const val SENT = 1
           const val DELIVERED = 2
           const val READ = 3
       }

       private fun getChatRoomReference(userId: String):DatabaseReference =  mDatabase.child(Keys.USERS).child(userId).child(Keys.CHAT_ROOMS)

       private fun getChatRoomReferenceById(userId: String,chatRoomId: String) = mDatabase.child(Keys.USERS).child(userId).child(Keys.CHAT_ROOMS).child(chatRoomId)

       fun checkChatRoomAvailable(provider:User,chatListener: ChatListener){
           AppController.appController.getUser()?.let { user->
               checkChatRoomAvailable(user.name,user.id,provider.id,provider.name,user.profilePic,provider.profilePic,chatListener)
           }?:run{
               chatListener.onFailure(code = ErrorCode.UN_AUTHORIZED)
           }
       }

       fun checkChatRoomAvailable(senderName:String,senderId:String,providerId:String,providerName:String,profilePic:String,providerProfilePic:String,chatListener: ChatListener){
           mDatabase.child(Keys.USERS).child(senderId).child(Keys.CHAT_ROOMS).addListenerForSingleValueEvent(object :ValueEventListener{
               override fun onDataChange(dataSnapshot: DataSnapshot) {
                   if(dataSnapshot.exists() && dataSnapshot.childrenCount>0){
                       var mChatRoom:ChatRoom
                       for(shot in dataSnapshot.children){
                           val chatRoom = shot.getValue(ChatRoom::class.java)
                           chatRoom?.let {
                               if(providerId == it.receiverId){
                                   mChatRoom = chatRoom
                                   mChatRoom.id = shot.key
                                   chatListener.onSuccess(mChatRoom)
                                   return
                               }
                           }
                       }
                       createChatRoom(senderName,senderId,providerId,providerName,profilePic,providerProfilePic,chatListener)
                   }
                   else{
                       createChatRoom(senderName,senderId,providerId,providerName,profilePic,providerProfilePic,chatListener)
                   }
               }

               override fun onCancelled(error: DatabaseError) {
                   ErrorHandler.handleError(exception = AppError(error.message, CustomError.CHAT_ROOM_FETCH_FAILED))
                   chatListener.onFailure(error.message, CustomError.CHAT_ROOM_FETCH_FAILED)
               }
           })
       }

       fun initiateNegotiationChat(sender:User,receiver:User,receiverName:String,productContent: String,negotiateAmount:String,negotiateId:String,chatListener: ChatListener){
           checkChatRoomAvailable(sender.name,sender.id,receiver.id,receiverName,sender.profilePic,receiver.profilePic,object :ChatListener{
               override fun onSuccess(any: Any?) {
                   val chatRoom = any as ChatRoom
                   val makeOffer = MakeOffer(productContent,buyer = Person(sender.id,Type.MakeOfferType.SENT_REQUEST),seller = Person(receiver.id,Type.MakeOfferType.RECEIVED_REQUEST),Negotiation(negotiateId,negotiateAmount))
                   sendMsg(sender.name,sender.id,receiver.id,receiverName,sender.profilePic,receiver.profilePic,chatRoom.id!!,
                       AppConstant.EMPTY,Type.MAKE_OFFER,makeOffer = makeOffer)
                   chatListener.onSuccess(any)
               }

               override fun onFailure(message: String?, code: Int) {
                   chatListener.onFailure(message, code)
               }
           })
       }


       fun getChatRoomById(userId: String,chatRoomId: String, chatListener: ChatListener){
           getChatRoomReferenceById(userId, chatRoomId).addListenerForSingleValueEvent(object : ValueEventListener {
               override fun onDataChange(snapshot: DataSnapshot) {
                   if(snapshot!=null){
                       val chatRoom = snapshot.getValue(ChatRoom::class.java)
                       chatRoom?.id = snapshot.key
                       chatListener.onSuccess(chatRoom)
                   }
                   else{
                       ErrorHandler.handleError(exception = AppError(code = CustomError.CHAT_ROOM_FETCH_FAILED))
                       chatListener.onFailure(null)
                   }
               }

               override fun onCancelled(error: DatabaseError) {
                   ErrorHandler.handleError(exception = AppError(error.message, CustomError.CHAT_ROOM_FETCH_FAILED))
                   chatListener.onFailure(error.message)
               }
           })
       }

       fun createChatRoom(senderName:String,senderId:String,providerId:String,providerName:String,profilePic:String, providerProfilePic:String,chatListener: ChatListener){
           val chatRoomId = Calendar.getInstance().timeInMillis
           val multiPath = hashMapOf<String,Any?>()
           val chats = hashMapOf<String,Any?>()
           chats[Keys.CREATED_TIME] = ServerValue.TIMESTAMP
           chats[Keys.TYPE] = "private"
           chats[Keys.USERS] = hashMapOf( senderId to true ,providerId to true)
           multiPath[Keys.CHATS.plus("/").plus(chatRoomId)] = chats
           val senderChatRoom = ChatRoom()
           senderChatRoom.apply {
               count = 0
               deleteStatus = false
               fileName = Constant.EMPTY
               lastMessage = Constant.EMPTY
               lastUpdated =  ServerValue.TIMESTAMP
               mimeType = Constant.EMPTY
               this.profilePic = providerProfilePic
               receiver = providerName
               receiverId = providerId
               sender = senderName
               updatedBy = senderId
               type = "private"
           }

           val receiverChatRoom = senderChatRoom.copy()
           receiverChatRoom.apply {
               this.profilePic = profilePic
               sender = providerName
               receiver = senderName
               receiverId = senderId
               updatedBy = senderId
           }

           multiPath[Keys.USERS+"/".plus(senderId)+"/".plus(Keys.CHAT_ROOMS)+"/".plus(chatRoomId)]= senderChatRoom.toMap()
           multiPath[Keys.USERS+"/".plus(providerId)+"/".plus(Keys.CHAT_ROOMS)+"/".plus(chatRoomId)] = receiverChatRoom.toMap()
           mDatabase.updateChildren(multiPath).addOnSuccessListener{
               chatListener.onSuccess(senderChatRoom.also { it.id = chatRoomId.toString() })
           }.addOnFailureListener { exception: Exception ->
               chatListener.onFailure(exception.message)
           }
       }

       fun sendMsg(senderName: String,senderId: String,providerId: String,providerName: String,profilePic: String ,providerProfilePic: String,chatRoomId:String,txt:String,mimeType:String,fileName:String=Constant.EMPTY,makeOffer: MakeOffer?=null){
           val mimeType = FileHelper().getFileType(mimeType)
           val chatRef = mDatabase.child(Keys.CHATS).child(chatRoomId).child(Keys.MESSAGES).push()
           val newKey = chatRef.key
           val chat = Chat(senderId,senderName,txt,mimeType,ServerValue.TIMESTAMP,fileName,makeOffer = makeOffer)
           val hashMap = hashMapOf<String,Any?>()
           val lastUpdated = ServerValue.TIMESTAMP
           //sender
           updateChatRoom(hashMap,senderId,chatRoomId,false,fileName,txt,providerName,senderName,mimeType,lastUpdated,providerProfilePic,"private",ChatDeliveryStatus.SENT,senderId,makeOffer)

           //receiver
           updateChatRoom(hashMap,providerId,chatRoomId,false,fileName,txt,senderName,providerName,mimeType,lastUpdated,profilePic,"private",0,senderId,makeOffer)

           hashMap[Keys.CHATS+"/"+chatRoomId+"/"+Keys.MESSAGES+"/"+newKey] = chat.toMap()
           incrementCount(mDatabase.child(Keys.USERS+"/"+providerId+"/"+Keys.CHAT_ROOMS+"/"+chatRoomId+"/"+Keys.COUNT))
           mDatabase.updateChildren(hashMap)
       }

       fun updateChatRoom(
           hashMap:HashMap<String,Any?>,
           userId: String,
           chatRoomId: String,
           deleteStatus: Boolean,
           fileName: String,
           lastMessage: String,
           receiver:String,
           sender:String,
           mimeType:String,
           lastUpdated:Map<String,String>,
           profilePic:String,
           type:String,
           deliveryStatus:Int,
           updatedBy:String,
           makeOffer: MakeOffer?
       ) {
           hashMap[Keys.USERS+"/"+userId+"/"+Keys.CHAT_ROOMS+"/"+chatRoomId+"/deleteStatus"]= deleteStatus
           hashMap[Keys.USERS+"/"+userId+"/"+Keys.CHAT_ROOMS+"/"+chatRoomId+"/fileName"] = fileName
           hashMap[Keys.USERS+"/"+userId+"/"+Keys.CHAT_ROOMS+"/"+chatRoomId+"/lastMessage"] = lastMessage
           hashMap[Keys.USERS+"/"+userId+"/"+Keys.CHAT_ROOMS+"/"+chatRoomId+"/receiver"] = receiver
           hashMap[Keys.USERS+"/"+userId+"/"+Keys.CHAT_ROOMS+"/"+chatRoomId+"/sender"] = sender
           hashMap[Keys.USERS+"/"+userId+"/"+Keys.CHAT_ROOMS+"/"+chatRoomId+"/mimeType"] = mimeType
           hashMap[Keys.USERS+"/"+userId+"/"+Keys.CHAT_ROOMS+"/"+chatRoomId+"/lastUpdated"] = lastUpdated
           hashMap[Keys.USERS+"/"+userId+"/"+Keys.CHAT_ROOMS+"/"+chatRoomId+"/profilePic"] = profilePic
           hashMap[Keys.USERS+"/"+userId+"/"+Keys.CHAT_ROOMS+"/"+chatRoomId+"/type"] = type
           hashMap[Keys.USERS+"/"+userId+"/"+Keys.CHAT_ROOMS+"/"+chatRoomId+"/"+Keys.DELIVERY_STATUS] = deliveryStatus
           hashMap[Keys.USERS+"/"+userId+"/"+Keys.CHAT_ROOMS+"/"+chatRoomId+"/"+Keys.UPDATED_BY] = updatedBy
           if (mimeType == Type.MAKE_OFFER && makeOffer!=null){
               if (makeOffer.seller.userId == userId){
                   hashMap[Keys.USERS+"/"+userId+"/"+Keys.CHAT_ROOMS+"/"+chatRoomId+"/"+Keys.OFFER_STATUS] = makeOffer.seller.offerStatus
               }
               if(makeOffer.buyer.userId == userId){
                   hashMap[Keys.USERS+"/"+userId+"/"+Keys.CHAT_ROOMS+"/"+chatRoomId+"/"+Keys.OFFER_STATUS] = makeOffer.buyer.offerStatus
               }
           }
       }

       fun updateNegotiation(isAccepted:Boolean,sender: User,receiver:User,receiverName:String,chatRoomId: String, messageId:String,makeOffer: MakeOffer){
           val hashMap = hashMapOf<String,Any?>()
           val chat = Chat(sender.id,sender.name,
               AppConstant.EMPTY,Type.MAKE_OFFER,ServerValue.TIMESTAMP,
               AppConstant.EMPTY,makeOffer = makeOffer)
           val chatRef = mDatabase.child(Keys.CHATS).child(chatRoomId).child(Keys.MESSAGES).push()
           val newKey = chatRef.key
           hashMap[Keys.CHATS+"/"+chatRoomId+"/"+Keys.MESSAGES+"/"+newKey] = chat.toMap()
           hashMap[Keys.CHATS+"/"+chatRoomId+"/"+Keys.MESSAGES+"/"+messageId+"/"+Keys.MAKE_OFFER+"/"+Keys.SELLER+"/"+Keys.OFFER_STATUS] = if (isAccepted) Type.MakeOfferType.ACCEPTED_REQUEST else Type.MakeOfferType.DENIED_REQUEST
           updateChatRoom(hashMap,receiver.id,chatRoomId,false,
               AppConstant.EMPTY,
               AppConstant.EMPTY,sender.name,receiverName,Type.MAKE_OFFER,ServerValue.TIMESTAMP,sender.profilePic,"private",ChatDeliveryStatus.SENT,sender.id,makeOffer)
           incrementCount(mDatabase.child(Keys.USERS+"/"+receiver.id+"/"+Keys.CHAT_ROOMS+"/"+chatRoomId+"/"+Keys.COUNT))
           mDatabase.updateChildren(hashMap)
       }

       fun updateReadStatus(chatRoomId:String,map:HashMap<String,Any?>) =  mDatabase.child(Keys.CHATS).child(chatRoomId).child(Keys.MESSAGES).updateChildren(map)

       private fun incrementCount(ref:DatabaseReference){
           ref.runTransaction(object : Transaction.Handler{
               override fun doTransaction(mutableData: MutableData): Transaction.Result {
                   val currentValue = mutableData.getValue(Int::class.java)
                   if (currentValue == null) {
                       mutableData.value = 1
                   } else {
                       mutableData.value = currentValue + 1
                   }

                   return Transaction.success(mutableData)
               }

               override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {
                   ErrorHandler.handleError(exception = AppError(message = p0?.message))
               }
           })
       }

       fun updateChatRoom(updatedBy:String?,providerId:String?,userId: String,chatRoomId: String?){
           val map = hashMapOf<String,Any>()
           map[Keys.USERS+"/"+userId+"/"+Keys.CHAT_ROOMS+"/"+chatRoomId+"/"+Keys.COUNT] = 0
           if(updatedBy!=null && updatedBy!=userId){
               map[Keys.USERS+"/"+providerId+"/"+Keys.CHAT_ROOMS+"/"+chatRoomId+"/"+Keys.DELIVERY_STATUS] = ChatDeliveryStatus.DELIVERED
           }
           mDatabase.updateChildren(map)
       }

       fun getChatRoomList(userId: String, chatListener: ChatListener) {
           this.chatListener= chatListener
           val chatRoomList = ArrayList<ChatRoom>()
           val chatRoomValueEventListener = object :ValueEventListener{
               override fun onDataChange(dataSnapShot: DataSnapshot) {
                   chatRoomList.clear()
                   if(dataSnapShot.exists() && dataSnapShot.childrenCount>0){
                       for(shot in dataSnapShot.children){
                           val chatRoom = shot.getValue(ChatRoom::class.java)
                           if(chatRoom != null && (!chatRoom.lastMessage.isNullOrEmpty() || chatRoom.mimeType== Type.MAKE_OFFER)){
                               chatRoomList.add(chatRoom.also { it.id = shot.key })
                           }
                       }
                   }
                   chatRoomList.reverse()
                   chatListener.onSuccess(chatRoomList)
               }

               override fun onCancelled(error: DatabaseError) {
                   chatListener.onFailure(error.message)
               }
           }
           this.chatRoomValueEventListener = chatRoomValueEventListener
           getChatRoomReference(userId).orderByChild("lastUpdated").addValueEventListener(chatRoomValueEventListener)
       }

       fun getChatRoomByUserId(receiverId: String,chatListener: ChatListener){
           AppController.appController.getUser()?.let {user->
               getChatRoomByUserId(user.id,receiverId).addListenerForSingleValueEvent(object :ValueEventListener{
                   override fun onDataChange(snapshot: DataSnapshot) {
                       var chatRoom:ChatRoom? = null
                       if(snapshot.exists()){
                           for(shot in snapshot.children){
                               chatRoom = shot.getValue(ChatRoom::class.java)
                               if(chatRoom!=null){
                                  chatRoom.id = shot.key
                                  return
                               }
                           }
                       }
                       chatListener.onSuccess(chatRoom)
                   }

                   override fun onCancelled(error: DatabaseError) {
                       chatListener.onFailure(error.message,error.code)
                   }
               })
           }?:run{
               chatListener.onFailure(code = ErrorCode.UN_AUTHORIZED)
           }
       }

       fun getChatThreadQuery(chatRoomId: String?): Query {
           return getChatThreadRefererence(chatRoomId).orderByKey().limitToLast(1000)
       }

       fun getChatThreadRefererence(chatRoomId: String?):DatabaseReference = mDatabase.child("chats/$chatRoomId/messages")

       private fun getChatRoomByUserId(userId:String,receiverId: String) = mDatabase.child(Keys.USERS).child(userId).child(Keys.CHAT_ROOMS).orderByChild(Keys.RECEIVER_ID).equalTo(receiverId)

       fun removeChatRoomReference(userId:String){
           this.chatRoomValueEventListener?.let {
               getChatRoomReference(userId).removeEventListener(it)
           }
       }

       fun removeRecentMessageListener(){
           this.recentMessageListener?.let { listener->
               val user = AppController.appController.getUser()
               if(user!=null){
                   getChatRoomReference(user.id).removeEventListener(listener)
               }
           }
       }

       fun checkRecentMessage(callback:(status:Boolean)->Unit){
           val user = AppController.appController.getUser()
           val query =  mDatabase.child(Keys.USERS).child(user!!.id).child(Keys.CHAT_ROOMS).orderByChild("lastUpdated")
           recentMessageListener = object :ValueEventListener{
               override fun onDataChange(snapshot: DataSnapshot) {
                   if(snapshot.exists()) {
                       for (shot in snapshot.children) {
                           val chatRoom = shot.getValue(ChatRoom::class.java)
                           chatRoom?.count?.let { count->
                               if(count>0){
                                   callback(true)
                                   return
                               }
                           }
                       }
                   }
                   callback(false)
               }

               override fun onCancelled(error: DatabaseError) {
                   ErrorHandler.handleError(exception = AppError(message = error.message,code = error.code))
                   callback(false)
               }
           }
           query.addValueEventListener(recentMessageListener!!)
       }

       fun setReadStatus(imageView: ImageView?, status: Int){
           imageView?.setImageResource(getReadStatusIcon(status))
           if(status == ChatDeliveryStatus.READ || status == ChatDeliveryStatus.DELIVERED){
               imageView?.setColorFilter(ContextCompat.getColor(AppController.appContext, R.color.colorChatStatusDelivered), android.graphics.PorterDuff.Mode.SRC_IN)
           }
           else{
               imageView?.setColorFilter(ContextCompat.getColor(AppController.appContext, R.color.colorDarkGrey), android.graphics.PorterDuff.Mode.SRC_IN)
           }
       }

       fun getReadStatusIcon(status:Int):Int{
           return when(status){
              ChatDeliveryStatus.SENT -> R.drawable.ic_icon_sent
              ChatDeliveryStatus.READ,
              ChatDeliveryStatus.DELIVERED-> R.drawable.ic_icon_delivered_status
               else -> R.drawable.ic_icon_delivered_status
           }
       }
   }
}