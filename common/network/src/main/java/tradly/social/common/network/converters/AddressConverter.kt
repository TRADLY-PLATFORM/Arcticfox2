package tradly.social.common.network.converters

import tradly.social.common.network.entity.*
import tradly.social.common.util.parser.extension.getValue
import tradly.social.domain.entities.*

class AddressConverter {

companion object:BaseConverter(){
    fun mapFrom(shippingAddressList:List<ShippingAddressEntity>):List<ShippingAddress>{
        val list =  mutableListOf<ShippingAddress>()
        shippingAddressList.forEach {
            list.add(mapFrom(it))
        }
        return list
    }

    fun mapFrom(shippingAddressEntity: ShippingAddressEntity) =
        ShippingAddress(
            id = shippingAddressEntity.id.toString(),
            name = getStringOrEmpty(shippingAddressEntity.name),
            phoneNumber = getStringOrEmpty(shippingAddressEntity.phoneNumber),
            lane1 = getStringOrEmpty(shippingAddressEntity.lane1),
            lane2 = getStringOrEmpty(shippingAddressEntity.lane2),
            landmark = getStringOrEmpty(shippingAddressEntity.landMark),
            country = getStringOrEmpty(shippingAddressEntity.country),
            formattedAddress = getStringOrEmpty(shippingAddressEntity.formattedAddress),
            defaultAddress = shippingAddressEntity.isCurrentAddress,
            province = getStringOrEmpty(shippingAddressEntity.state),
            zipcode = getStringOrEmpty(shippingAddressEntity.postCode),
            type = getStringOrEmpty(shippingAddressEntity.type),
            city = Constant.EMPTY
        )

    fun mapFrom(locationEntity: LocationEntity):Address =
        Address().apply {
            locality = getStringOrEmpty(locationEntity.locality)
            formattedAddress = getStringOrEmpty(locationEntity.formattedAddress)
            city = getStringOrEmpty(locationEntity.city)
            country = getStringOrEmpty(locationEntity.country)
            postalCode = getStringOrEmpty(locationEntity.postalCode)
            countryCode = getStringOrEmpty(locationEntity.countryCode)
            state = getStringOrEmpty(locationEntity.state)
            geoPoint = GeoPoint(locationEntity.latitude,locationEntity.longitude)
            type = locationEntity.type
        }

    fun mapFrom(addressResponse: AddressResponse):Address {
        val reverseGeoCodeEntity = addressResponse.addressData.addressEntity.locationEntity
        return Address(
            city = reverseGeoCodeEntity.city.getValue(),
            locality = reverseGeoCodeEntity.locality.getValue(),
            countryCode = reverseGeoCodeEntity.countryCode.getValue(),
            country = reverseGeoCodeEntity.country.getValue(),
            postalCode = reverseGeoCodeEntity.postalCode.getValue(),
            formattedAddress = reverseGeoCodeEntity.formattedAddress.getValue(),
            geoPoint = GeoPoint(reverseGeoCodeEntity.latitude,reverseGeoCodeEntity.longitude),
            type = reverseGeoCodeEntity.type
        )
    }

    fun mapFromList(list: List<LocationEntity>) = mutableListOf<Address>().apply {
        list.forEach { this.add(mapFrom(it)) }
    }

    fun mapFrom(geoPointEntity: GeoPointEntity?) = GeoPoint(geoPointEntity?.latitude?:0.0,geoPointEntity?.longitude?:0.0)

    fun mapFrom(shippingMethodData: ShippingMethodData):List<ShippingMethod>{
        val list = mutableListOf<ShippingMethod>()
        shippingMethodData.shippingMethodList.forEach {
            list.add(
                ShippingMethod(
                id = it.id,
                name = it.name,
                logo = it.logoPath,
                type = it.type,
                default = it.default
            ))
        }
        return list
    }

    fun mapFrom(shippingMethod:ShippingMethodEntity):ShippingMethod{
        return  ShippingMethod(
            id = shippingMethod.id,
            name = shippingMethod.name,
            logo = shippingMethod.logoPath,
            type = shippingMethod.type,
            default = shippingMethod.default
        )
    }
}

}