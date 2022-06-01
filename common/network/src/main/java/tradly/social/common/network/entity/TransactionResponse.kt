package tradly.social.common.network.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TransactionResponse(
    @field:Json(name = "data") val data:TransactionData
):ResponseStatus()

@JsonClass(generateAdapter = true)
data class TransactionData(
    @field:Json(name = "total_records") val totalRecords:Int,
    @field:Json(name = "page") val page:Int,
    @field:Json(name = "transactions") val transactions:List<TransactionEntity>
)

@JsonClass(generateAdapter = true)
data class TransactionEntity(
    @field:Json(name = "order") val order:TransactionOrder,
    @field:Json(name = "transaction_number") val transactionNumber:String,
    @field:Json(name = "reference_type") val referenceType:Int,
    @field:Json(name = "reference_id") val referenceId:Int,
    @field:Json(name = "amount") val price:PriceEntity,
    @field:Json(name = "super_type") val superType :Int,
    @field:Json(name = "type") val type:Int,
    @field:Json(name = "created_at") val createdAt:Long
)

@JsonClass(generateAdapter = true)
data class TransactionOrder (
    @field:Json(name ="id") val id : Int,
    @field:Json(name ="reference_number") val referenceNumber : Int
)
