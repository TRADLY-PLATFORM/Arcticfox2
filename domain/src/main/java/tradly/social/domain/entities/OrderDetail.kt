package tradly.social.domain.entities

data class OrderDetail(
    val orderedProductEntities: List<OrderedProductEntity>,
    val orderStatusEntities: List<OrderStatusEntity>,
    val orderAddressEntity: OrderAddressEntity,
    val pickupAddressEntity: OrderAddressEntity?=null,
    val shipmentMethod:String = Constant.EMPTY,
    val orderId: String,
    val grandTotal: Price,
    val nextStatus:List<Int>,
    val user: User,
    val store:Store
)