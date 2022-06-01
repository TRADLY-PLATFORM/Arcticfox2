package tradly.social.domain.entities

data class User(
    var active: Boolean = false,
    var emailVerified: Boolean = false,
    var name: String = Constant.EMPTY,
    var firstName: String = Constant.EMPTY,
    var password: String? = Constant.EMPTY,
    var email: String? = Constant.EMPTY,
    var userType: Int = 0,
    var mobile: String = Constant.EMPTY,
    var dialCode:String = Constant.EMPTY,
    var profilePic: String = Constant.EMPTY,
    var counrty: Country? = null,
    var authKeys: AuthKeys = AuthKeys(),
    var lastName:String=Constant.EMPTY,
    var isStripeConnected:Boolean = false,
    var isStripeOnboardConnected:Boolean = false,
    override var id: String = "",
    override val createdAt: Long = 0,
    override val updatedAt: Long = 0
) : BaseInfo

data class AuthKeys(val authKey: String = Constant.EMPTY, val refreshKey: String = Constant.EMPTY, var firebaseToken:String = Constant.EMPTY)