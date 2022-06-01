package tradly.social.common.network.converters

import org.json.JSONArray
import tradly.social.common.network.entity.AttributeEntity
import tradly.social.common.network.entity.StoreEntity
import tradly.social.common.network.entity.SubscriptionEntity
import tradly.social.common.util.parser.extension.toJson
import tradly.social.domain.entities.Constant
import tradly.social.domain.entities.Store
import tradly.social.domain.entities.Subscription

object StoreModelConverter : BaseConverter() {

    fun mapFrom(from:StoreEntity):Store =
        Store().apply {
            id = from.id
            storeName = getStringOrEmpty(from.storeName)
            storeDescription = getStringOrEmpty(from.description)
            storePic = from.images.let { if(it.isNotEmpty()) it[0] else Constant.EMPTY }
            webAddress = getStringOrEmpty(from.webAddress)
            listingsCount = from.totalListing
            followersCount = from.totalFollowers
            followed = from.following
            active = from.active
            uniqueName = from.uniqueName
            geoPoint = AddressConverter.mapFrom(from.coordinates)
            address = AddressConverter.mapFrom(from.location)
            user = if(from.user != null) UserModelConverter().mapFrom(from.user) else null
            categoryId = if(from.categoryIds.isNotEmpty()) from.categoryIds[0].toString() else Constant.EMPTY //TODO Need to discuss with Thahaseen
            category = if(from.categories.isNotEmpty())CategoryConverter.mapFrom(from.categories[0]) else null
            attributes = ProductConverter.mapAttribute(JSONArray(from.attributes.toJson<List<AttributeEntity>>()))
            shippingMethods = from.shippingMethods.map { AddressConverter.mapFrom(it) }
            status = from.status
            subscription = mapFrom(from.subscription)
        }

    fun mapFrom(list:List<StoreEntity>?):ArrayList<Store>{
        val mList = arrayListOf<Store>()
        list?.let {
            it.forEach { s->
                mList.add(mapFrom(s))
            }
        }
        return mList
    }

    fun mapFrom(from: SubscriptionEntity?):Subscription{
        from?.let {
         return Subscription(from.allowListing?:false,from.subscriptionEnd*1000L,from.createdAt*1000L)
        }
        return Subscription()
    }
}