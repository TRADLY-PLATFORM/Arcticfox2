package tradly.social.domain.dataSource

import tradly.social.domain.entities.Rating
import tradly.social.domain.entities.Result

interface ReviewDataStore {

    fun getReviewList(id:String,type:String,page:Int): Result<Rating>

    fun addReview(type:String,id: String,title:String,content:String,rating:Int,images:List<String>): Result<Boolean>

    fun likeReview(reviewId:String,status:Int): Result<Boolean>
}