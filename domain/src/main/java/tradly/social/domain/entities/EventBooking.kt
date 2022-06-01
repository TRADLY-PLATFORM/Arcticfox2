package tradly.social.domain.entities

class EventBooking(
    val id:Int,
    val referenceNo:Int,
    val offerPrice:Price,
    val price:Price,
    val store:Store,
    val tickets:Int,
    val event:Event
)