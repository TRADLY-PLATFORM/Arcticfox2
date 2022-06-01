package tradly.social.common.network.converters

import tradly.social.domain.entities.Constant
import tradly.social.domain.entities.Country
import com.parse.ParseObject
import tradly.social.common.network.entity.CountryEntity

class CountryModelConverter {
    fun mapFrom(from: ParseObject?): Country {
        from?.run {
            return Country(
                /*from.getString(ParseConstant.CODE2),
                from.getString(ParseConstant.CODE3),
                from.getBoolean(ParseConstant.ACTIVE),
                from.getString(ParseConstant.CURRENCY_LOCALE),
                from.getString(ParseConstant.NAME),
                from.getString(ParseConstant.FLAG),
                from.getString(ParseConstant.MOBILE_NUMBER_REGEX),
                from.getString(ParseConstant.CURRENCY),
                from.getString(ParseConstant.LOCALE),
                from.getInt(ParseConstant.DIAL_CODE),
                from.getInt(ParseConstant.MOBILE_NUMBER_LENGTH),
                from.objectId,
                from.createdAt.time,
                from.updatedAt.time*/
            )
        }
        return Country()
    }

  /*  fun mapFromList(from: List<ParseObject>?): List<Country> {
        val countryList = mutableListOf<Country>()
        from?.run {
            for (parseObj in from) {
                countryList.add(
                    Country(
                        parseObj.getString(ParseConstant.CODE2),
                        parseObj.getString(ParseConstant.CODE3),
                        parseObj.getBoolean(ParseConstant.ACTIVE),
                        parseObj.getString(ParseConstant.CURRENCY_LOCALE),
                        parseObj.getString(ParseConstant.NAME),
                        parseObj.getString(ParseConstant.FLAG),
                        parseObj.getString(ParseConstant.MOBILE_NUMBER_REGEX),
                        parseObj.getString(ParseConstant.CURRENCY),
                        parseObj.getString(ParseConstant.LOCALE),
                        parseObj.getInt(ParseConstant.DIAL_CODE),
                        parseObj.getInt(ParseConstant.MOBILE_NUMBER_LENGTH),
                        parseObj.objectId,
                        parseObj.createdAt.time,
                        parseObj.updatedAt.time
                    )
                )
            }
        }
        return countryList
    }
*/
    fun mapFromList(list:List<CountryEntity>):List<Country> {
        val countryList = mutableListOf<Country>()
        for(item in list){
            countryList.add(mapFrom(item))
        }
        return countryList
    }

    fun mapFrom(countryEntity: CountryEntity) =
        Country(
            id = countryEntity.id.toString(),
            name = countryEntity.name,
            code2 = countryEntity.code2?:Constant.EMPTY,
            code3 = countryEntity.code3?:Constant.EMPTY,
            dialCode = countryEntity.dialCode?:Constant.EMPTY,
            timeZone = countryEntity.timeZone?:Constant.EMPTY,
            currencyCode = countryEntity.currencyCode?:Constant.EMPTY,
            currency = countryEntity.currencyEn?:Constant.EMPTY,
            currencyLocale = countryEntity.currencyLocale?:Constant.EMPTY,
            mobileNumberLength = countryEntity.mobileNumberLength.toInt(),
            mobileNumberRegex = countryEntity.mobileNumberRegex,
            flag = countryEntity.flagUrl
        )

}