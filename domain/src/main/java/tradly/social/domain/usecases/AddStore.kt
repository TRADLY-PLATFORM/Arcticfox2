package tradly.social.domain.usecases

import tradly.social.domain.entities.*
import tradly.social.domain.repository.StoreRepository

class AddStore(private val storeRepository: StoreRepository) {
    suspend operator fun invoke(isFor:Boolean,accountId:String,categoryId:String,storeName:String?,webAddress: String?, geoPoint: GeoPoint?,desc:String?, storeLinkName:String,storePic:String? , attributeValue: List<HashMap<String, Any?>>,selectedShipments:List<Int>) =
        if(isFor){
            storeRepository.editStore(accountId, categoryId, storeName, webAddress, geoPoint, desc,storeLinkName, storePic, attributeValue,selectedShipments)
        }
        else{
            storeRepository.addStore(categoryId,storeName, webAddress,geoPoint, desc, storeLinkName,storePic ,attributeValue,selectedShipments)
        }

    suspend fun getAttribute(categoryId:String) = storeRepository.getAttribute(categoryId)
}