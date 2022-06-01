package tradly.social.domain.usecases

import tradly.social.domain.repository.ReviewRepository

class GetReviews(val reviewRepository: ReviewRepository){
    suspend fun getReviewList(id:String,type:String,page:Int) = reviewRepository.getReviewList(id,type, page)
    suspend fun addReview(type:String,id: String,title:String,content:String,rating:Int,images:List<String>) = reviewRepository.addReview(type,id, title, content, rating, images)
    suspend fun likeReview(reviewId:String,status:Int) = reviewRepository.likeReview(reviewId, status)
}