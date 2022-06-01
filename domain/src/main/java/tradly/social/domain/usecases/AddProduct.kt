package tradly.social.domain.usecases

import tradly.social.domain.entities.Constant
import tradly.social.domain.entities.GeoPoint
import tradly.social.domain.entities.Variant
import tradly.social.domain.repository.ProductRepository

class AddProduct(private val productRepository: ProductRepository) {

    suspend fun addProduct(
        storeId:String?,
        id:String?,
        title: String,
        currencyId: String?,
        price: String,
        offerPrice:String,
        cid: String,
        desc: String,
        maxQty:String,
        stock:String,
        shippingCharge:String,
        tagList: List<String>,
        productImages: List<String?>,
        attributeValue: List<HashMap<String, Any?>>,
        geoPoint: GeoPoint?,
        isForEdit:Boolean = false,
        variantList: List<Variant>,
        startTime:Long =0,
        endTime:Long = 0
    )= productRepository.addProduct(id,getRequestBody(storeId,title, currencyId, price, offerPrice, cid, desc,maxQty,stock,shippingCharge, tagList, productImages,geoPoint, attributeValue,startTime, endTime),isForEdit,variantList)

    fun updateProduct(productId:String,hashMap: HashMap<String,Any>) = productRepository.updateProduct(productId,hashMap)

    private fun getRequestBody(
        stroreId:String?,
        title: String,
        currencyId: String?,
        price: String,
        offerPrice:String,
        cid: String,
        desc: String,
        maxQty:String,
        stock: String,
        shippingCharge:String,
        tagList: List<String>,
        productImages: List<String?>,
        geoPoint: GeoPoint?,
        attributeValue: List<HashMap<String, Any?>>,
        startTime:Long=0,
        endTime:Long = 0
    ): HashMap<String, Any?> {
        val map = hashMapOf<String, Any?>()
        val listing = hashMapOf<String, Any?>()
        listing["account_id"] = stroreId
        listing["title"] = title
        listing["currency_id"] = currencyId
        listing["images"] = productImages
        listing["list_price"] = price
        if(geoPoint!=null){
            listing["coordinates"] = hashMapOf("latitude" to geoPoint.latitude , "longitude" to geoPoint.longitude)
        }
        if (offerPrice.isNotEmpty()){ listing["offer_percent"] = offerPrice }
        if(desc.isNotEmpty()){listing["description"] = desc}
        if(tagList.isNotEmpty()){ listing["tags"] = tagList}
        if(cid.isNotEmpty()){listing["category_id"] = listOf(cid)}
        if(attributeValue.isNotEmpty()){listing["attributes"] = attributeValue}
        if(maxQty.isNotEmpty()){ listing["max_quantity"] = maxQty}
        if(stock.isNotEmpty()){ listing["stock"] = stock}
        listing["shipping_charges"] = if(shippingCharge.isNotEmpty()) shippingCharge else "0"
        if(startTime!=0L){
            listing["start_at"] = startTime/1000
        }
        if (endTime!=0L){
            listing["end_at"] = endTime/1000
        }
        map["listing"] = listing
        return map
    }
}