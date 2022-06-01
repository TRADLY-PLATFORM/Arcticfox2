package tradly.social.domain.entities

data class ChatRoom(
    var id: String? = Constant.EMPTY,
    var count: Int? =null,
    var deleteStatus: Boolean?=null,
    var lastMessage: String? = null,
    var lastUpdated: Any? = null,
    var profilePic: String? = null,
    var receiver: String? = null,
    var sender: String? = null,
    var receiverId: String? = null,
    var mimeType: String? = null,
    var fileName: String? = null,
    var type: String? = null,
    var deliveryStatus:Int = 0,
    var updatedBy:String?=null,
    var offerStatus:Int?=0
){
    fun toMap():HashMap<String,Any?>{
        val map = hashMapOf<String,Any?>()
        if(!id.isNullOrEmpty()){ map["id"] = id }
        if(count != null){map["count"] = count}
        if(deleteStatus != null){map["deleteStatus"] = deleteStatus}
        if(lastMessage != null){map["lastMessage"] = lastMessage}
        if(lastUpdated != null){map["lastUpdated"] = lastUpdated}
        if(profilePic != null){ map["profilePic"] = profilePic}
        if(receiver != null){map["receiver"] = receiver}
        if(sender != null){map["sender"] = sender}
        if(receiverId != null){map["receiverId"] = receiverId}
        if(mimeType != null){map["mimeType"] =  mimeType}
        if(fileName != null){map["fileName"] = fileName}
        if(type != null){map["type"] = type}
        if(updatedBy!=null){map["updatedBy"] = updatedBy}
        map["deliveryStatus"] = deliveryStatus
        return map
    }
}