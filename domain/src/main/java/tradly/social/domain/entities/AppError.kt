package tradly.social.domain.entities


data class AppError(
    var message: String? = "",
    var code: Int = 0,
    var networkResponseCode:Int=0,
    var url:String = Constant.EMPTY,
    var payload:String? = Constant.EMPTY,
    val errorType:Int = 0,
    var apiId:Int = 0
)