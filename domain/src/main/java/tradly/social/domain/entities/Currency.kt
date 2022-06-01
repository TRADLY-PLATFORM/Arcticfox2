package tradly.social.domain.entities

data class Currency(
    var id:Int = 0,
    var name:String = Constant.EMPTY,
    var code:String = Constant.EMPTY,
    var precision:String = Constant.EMPTY,
    var format:String = Constant.EMPTY,
    var default:Boolean = false
){

    var symbol:String = format
    get() = format.substring(0,format.indexOfFirst { it == '{' })
}
