package tradly.social.common.network.entity

import tradly.social.domain.entities.AppError
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class ResponseStatus(
    var status:Boolean = false,
    var error:AppError?= null,
    var timestamp:Int = 0
)