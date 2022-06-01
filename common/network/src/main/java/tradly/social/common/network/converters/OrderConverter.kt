package tradly.social.common.network.converters

import tradly.social.common.*
import tradly.social.common.network.entity.OrderEntity
import tradly.social.common.util.parser.extension.getValue
import tradly.social.domain.entities.*

object OrderConverter:BaseConverter() {

    fun mapFrom(orders: List<OrderEntity>): List<Order> {
        return orders.map {
            Order(
                it.id.toString(),
                if (it.orderListing[0].listing.images.isNotEmpty()) it.orderListing[0].listing.images[0] else "",
                it.account.storeName,
                it.orderListing[0].listing.title,
                it.referenceNumber.toString(),
                it.createdAt*1000,
                it.offerTotal.displayCurrency
            )
        }
    }

    fun mapFrom(order: OrderEntity): OrderDetail {

        val orderedProductEntities = order.orderListing.map {
            OrderedProductEntity(
                it.listing.id,
                it.listing.images[0],
                it.listing.title,
                it.listing.description,
                it.quantity.toString(),
                it.offerPrice.displayCurrency,
                it.listing.reviewStatus
            )
        }

        val orderStatusEntities = order.statusHistory.map {
            OrderStatusEntity(
                it.remarks,
                it.createdAt*1000,
                it.createdAt*1000,
                order.orderStatus==it.status,
                it.status
            )
        }
        val orderAddressEntity = OrderAddressEntity(
            order.shippingAddress.id,
            getStringOrEmpty(order.shippingAddress.name),
            getStringOrEmpty(order.shippingAddress.formattedAddress),
            getStringOrEmpty(order.shippingAddress.phoneNumber),
            order.shippingAddress.formattedAddress.getValue()
        )
        var pickupAddressEntity:OrderAddressEntity?=null

        if(order.pickupAddress!= null && !order.pickupAddress.name.isNullOrEmpty()){
            pickupAddressEntity = OrderAddressEntity(
                order.pickupAddress.id,
                getStringOrEmpty(order.pickupAddress.name),
                getStringOrEmpty(order.pickupAddress.formattedAddress),
                getStringOrEmpty(order.pickupAddress.phoneNumber),
                order.pickupAddress.formattedAddress.getValue()
            )
        }

        return OrderDetail(
            orderedProductEntities,
            orderStatusEntities,
            orderAddressEntity,
            pickupAddressEntity,
            order.shippingMethod?.type.getValue(),
            order.referenceNumber.toString(),
            ProductConverter.mapFrom(order.grandTotal),
            order.nextStatus,
            user = UserModelConverter().mapFrom(order.user),
            store = StoreModelConverter.mapFrom(order.account)
        )
    }
}