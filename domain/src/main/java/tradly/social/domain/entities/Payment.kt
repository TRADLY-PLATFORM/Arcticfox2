package tradly.social.domain.entities

data class Payment(
    val id:Int,
    val name:String,
    val logo:String,
    val type:String,
    val channel:String,
    var default:Boolean = false,
    val minAmount:Double
):BaseSelection()