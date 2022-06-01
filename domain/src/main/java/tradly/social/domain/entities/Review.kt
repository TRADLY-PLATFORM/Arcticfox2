package tradly.social.domain.entities

data class Review(
    val id:Int,
    val title:String,
    val content:String,
    val rating:Int,
    val createdAt:Long,
    val user:User,
    var likeStatus:Int,
    val images:List<String>
)

data class Rating(
    var ratingAverage:Double=0.0,
    var ratingCount:Int=0,
    var reviewCount:Int=0,
    var ratingDataCount:RatingCount = RatingCount(),
    var reviewList:List<Review> = mutableListOf(),
    var myReview:Review?=null
)

data class RatingCount(
    var rating1: Int=0,
    var rating2: Int=0,
    var rating3: Int=0,
    var rating4: Int=0,
    var rating5: Int=0
)