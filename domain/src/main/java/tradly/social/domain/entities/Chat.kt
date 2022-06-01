package tradly.social.domain.entities

data class Chat(
    var userId: String,
    var userName: String,
    var message: String?=Constant.EMPTY,
    var mimeType: String,
    var timeStamp: Any,
    var fileName: String = Constant.EMPTY,
    var deliveryStatus:Int = 1,
    val makeOffer:MakeOffer?=null
) {
    constructor():this("","","","","","")
    fun toMap(): HashMap<String, Any?> {
        val map = hashMapOf<String, Any?>()
        map["userId"] = userId
        map["userName"] = userName
        map["message"] = message
        map["mimeType"] = mimeType
        map["timeStamp"] = timeStamp
        map["fileName"] = fileName
        map["deliveryStatus"] = deliveryStatus
        if(makeOffer!=null){
            map["makeOffer"] = makeOffer
        }
        return map
    }
}

data class MakeOffer(
    var content:String,
    var buyer:Person,
    var seller:Person,
    var negotiation: Negotiation
){
    constructor():this("", Person(), Person(),Negotiation())
}

data class Person(
    val userId:String = Constant.EMPTY,
    val offerStatus:Int = 0
)

data class Negotiation(
    var id:String = Constant.EMPTY,
    var price:String = Constant.EMPTY,
    var createdAt: Long = 0,
    var expiry:Long = 0,
    var negotiatedPrice:Price? = null
)