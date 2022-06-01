package tradly.social.common.network.converters

import org.json.JSONArray
import org.json.JSONObject
import tradly.social.common.network.entity.*
import tradly.social.common.util.parser.extension.toList
import tradly.social.common.util.parser.extension.toObject
import tradly.social.domain.entities.*

open class ProductConverter {
    companion object{
        fun mapFromList(fromList: List<ProductEntity>?):ArrayList<Product>{
            val list = ArrayList<Product>()
            fromList?.let {
                for (obj in fromList){
                    list.add(mapFrom(obj))
                }
            }
            return list
        }

        fun mapFrom(productEntity: ProductEntity? , jsonString: String = Constant.EMPTY):Product {
            productEntity?.let {
                return Product(
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
                    tags = TagConverter.mapFromList(productEntity.tags),
                    isActive = productEntity.active,
                    location = AddressConverter.mapFrom(it.coordinates),
                    variants = VariantConverter.mapFromVariant(it.variantList),
                    address = AddressConverter.mapFrom(it.address),
                    maxQuantity = it.maxQuantity,
                    category = if(it.categories.isNotEmpty())CategoryConverter.mapFrom(it.categories[0])else null,
                    negotiation = mapFrom(it.negotiationEntity),
                    startAt =  it.startAt?:0L*1000L,
                    endAt =  it.endAt?:0L*1000L,
                    likes = it.likes

                ).also {
                    if (jsonString.isNotEmpty()) {
                        it.attributes = mapFrom(jsonString)
                    }
                    it.shipping = mapFrom(productEntity.shippingCharges)
                    if (productEntity.ratingData!=null){
                        it.rating = ReviewConverter.mapFrom(productEntity.ratingData)
                    }
                }
            }
            return Product()
        }

        fun mapFrom(product: Product) = Event(
            id = product.id,
            title = product.title,
            offerPercent = product.offerPercent,
            offerPrice = product.offerPrice,
            listPrice = product.listPrice,
            displayPrice = product.displayPrice,
            displayOffer = product.displayOffer,
            images = product.images,
            inStock = product.inStock,
            stock = product.stock,
            status = product.status,
            createdAt = product.createdAt,
            currencyId = product.currencyId,
            categoryIds = product.categoryIds,
            isLiked = product.isLiked,
            store = product.store,
            description = product.description,
            isProductInCart = product.isProductInCart,
            tags = product.tags,
            isActive = product.isActive,
            location = product.location,
            variants = product.variants,
            address = product.address,
            maxQuantity = product.maxQuantity,
            category = product.category,
            negotiation = product.negotiation,
            startAt = product.startAt,
            endAt = product.endAt
        )

        fun mapFrom(event: Event) = Product(
            id = event.id,
            title = event.title,
            offerPercent = event.offerPercent,
            offerPrice = event.offerPrice!!,
            listPrice = event.listPrice!!,
            displayPrice = event.displayPrice,
            displayOffer = event.displayOffer,
            images = event.images,
            inStock = event.inStock,
            stock = event.stock,
            status = event.status,
            createdAt = event.createdAt,
            currencyId = event.currencyId,
            categoryIds = event.categoryIds,
            isLiked = event.isLiked,
            store = event.store,
            description = event.description,
            isProductInCart = event.isProductInCart,
            tags = event.tags,
            isActive = event.isActive,
            location = event.location,
            variants = event.variants!!,
            address = event.address,
            maxQuantity = event.maxQuantity,
            category = event.category,
            negotiation = event.negotiation,
            startAt = event.startAt,
            endAt = event.endAt
        )


        fun mapFrom(negotiationEntity: NegotiationEntity?):Negotiation?{
            negotiationEntity?.let {
                return Negotiation().apply {
                    id = negotiationEntity.id.toString()
                    if (negotiationEntity.priceEntity!=null){
                        negotiatedPrice = mapFrom(negotiationEntity.priceEntity)
                    }
                    expiry = negotiationEntity.expiry
                    createdAt = negotiationEntity.createdAt
                }
            }
            return null
        }

        fun mapFrom(jsonString:String):ArrayList<Attribute>{
            val mList = ArrayList<Attribute>()
            return try{
                val json = JSONObject(jsonString)
                val data = json.getJSONObject("data")
                if(data.has("listing")){
                    mapAttribute(data.getJSONObject("listing").getJSONArray("attributes"))
                } else{
                    mapAttribute(data.getJSONArray("attributes"))
                }
            } catch (ex:Exception){
                mList
            }
        }


        /**
         *   Attribute mappings from API
         */

        fun mapAttribute(jsArray:JSONArray):ArrayList<Attribute>{
            val mList = ArrayList<Attribute>()
            try{
                for(i in 0 until jsArray.length()){
                    val attribute = jsArray.getJSONObject(i)
                    Attribute(
                        id = attribute.optInt("id"),
                        name = attribute.optString("name"),
                        type = attribute.optString("type"),
                        fieldType = attribute.optInt("field_type"),
                        categories = CategoryConverter.mapFromList(attribute.optString("categories").toList<CategoryEntity>()),
                        optional = attribute.optBoolean("optional",false)
                    ).also {
                        if(it.fieldType == 1 || it.fieldType == 2){
                            it.attributeValues = attribute.optJSONArray("values")?.toString()?.toList<Value>()?: listOf()
                        }
                        else{
                            it.attributeValueList = attribute.optJSONArray("values")?.toString()?.toList<String>()?: listOf()
                        }
                        mList.add(it)
                    }
                }
                return mList
            }
            catch (ex:Exception){
                return mList
            }
        }

        fun mapFrom(from: ValueEntity) = Value(from.id,from.name)

        fun mapFromString(productString:String?):Product = mapFrom(productString.toObject<ProductEntity>())

        fun mapFrom(priceEntity: PriceEntity) = Price(priceEntity.amount,priceEntity.currency,priceEntity.displayCurrency)

        fun mapFrom(price:Price) = PriceEntity(price.amount,price.currency,price.displayCurrency)

        fun mapFrom(priceEntity: PriceEntity?):Shipping{
            if(priceEntity!=null){
                return Shipping(true,priceEntity.amount,priceEntity.displayCurrency,priceEntity.currency)
            }
            return Shipping()
        }
    }
}