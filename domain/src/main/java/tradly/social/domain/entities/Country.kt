package tradly.social.domain.entities

data class Country(
    var code2: String = Constant.EMPTY,
    var code3: String = Constant.EMPTY,
    var currencyLocale: String = Constant.EMPTY,
    var name: String = Constant.EMPTY,
    var flag: String = Constant.EMPTY,
    var mobileNumberRegex: String = Constant.EMPTY,
    var currency: String = Constant.EMPTY,
    var locale: String = Constant.EMPTY,
    var dialCode: String = Constant.EMPTY,
    var mobileNumberLength: Int = 0,
    var timeZone:String = Constant.EMPTY,
    var currencyCode:String = Constant.EMPTY,
    override var id: String = Constant.EMPTY,
    override val createdAt: Long = 0,
    override val updatedAt: Long = 0
) : BaseInfo