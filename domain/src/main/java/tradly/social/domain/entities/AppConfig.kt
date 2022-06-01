package tradly.social.domain.entities

data class AppConfig(
    var key: Key = Key(),
    var localeSupported: List<Locale> = mutableListOf(),
    var authType: Int = 0,
    var config: Config = Config(),
    var flavour:Int = 2,
    var module:Int = 0
)

data class Locale(
    var name: String = Constant.EMPTY,
    var locale: String = Constant.EMPTY,
    var default:Boolean = false
)

data class Key(
    var appKey:String = Constant.EMPTY
)

data class Config(
    var authType: Int = 0,
    var branchLinkDescription:String = Constant.EMPTY,
    var faqUrl:String = Constant.EMPTY,
    var listingMapLocationSelectorEnabled:Boolean = false,
    var accountMapLocationSelectorEnabled:Boolean = false,
    var homeLocationEnabled:Boolean = false,
    var enableFeedback:Boolean = false,
    var branchLinkBaseUrl:String = Constant.EMPTY,
    var subscription:Boolean = false
)