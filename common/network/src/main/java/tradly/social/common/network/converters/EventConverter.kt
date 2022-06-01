package tradly.social.common.network.converters

import tradly.social.common.network.feature.exploreEvent.model.EventEntity
import tradly.social.domain.entities.Constant
import tradly.social.domain.entities.Event

class EventConverter:ProductConverter() {
    companion object{
        fun mapFrom(eventList:List<EventEntity>):List<Event>{
            val list = mutableListOf<Event>()
            eventList.forEach { eventEntity->
                list.add(mapFrom(eventEntity))
            }
            return list
        }

        fun mapFrom(eventEntity: EventEntity, jsonString: String = Constant.EMPTY): Event {
            eventEntity.let {
                return Event(
                    id = it.id.toString(),
                    title = it.title,
                    offerPercent = it.offerPercent,
                    offerPrice = mapFrom(it.offerPrice),
                    listPrice = mapFrom(it.listPrice),
                    displayPrice = it.listPrice.displayCurrency,
                    displayOffer = it.offerPrice.displayCurrency,
                    images = it.images ?: listOf(),
                    inStock = it.stock!=0,
                    stock = it.stock,
                    status = it.status,
                    createdAt = it.createdAt,
                    currencyId = it.currencyId,
                    categoryIds = it.categoryId,
                    isLiked = it.liked,
                    store = it.store?.let { StoreModelConverter.mapFrom(it) } ?: run { null },
                    description = it.description,
                    isProductInCart = it.isInCart,
                    tags = TagConverter.mapFromList(eventEntity.tags),
                    isActive = eventEntity.active,
                    location = AddressConverter.mapFrom(it.coordinates),
                    variants = VariantConverter.mapFromVariant(it.variantList),
                    address = AddressConverter.mapFrom(it.address),
                    maxQuantity = it.maxQuantity,
                    category = if(it.categories.isNotEmpty())CategoryConverter.mapFrom(it.categories[0])else null,
                    negotiation = mapFrom(it.negotiationEntity),
                    startAt =  it.startAt*1000L,
                    endAt =  it.endAt*1000L


                ).also {
                    if (jsonString.isNotEmpty()) {
                        it.attributes = mapFrom(jsonString)
                    }
                    it.shipping = mapFrom(eventEntity.shippingCharges)
                    if (eventEntity.ratingData!=null){
                        it.rating = ReviewConverter.mapFrom(eventEntity.ratingData)
                    }
                }
            }
        }
    }
}