package tradly.social.domain.repository

import tradly.social.domain.dataSource.ReviewDataStore

class ReviewRepository(val reviewDataStore: ReviewDataStore) {

    fun getReviewList(id:String,type:String,page:Int) = reviewDataStore.getReviewList(id,type, page)

    fun addReview(type:String,id: String,title:String,content:String,rating:Int,images:List<String>) = reviewDataStore.addReview(type,id, title, content, rating, images)

    fun likeReview(reviewId:String,status:Int) = reviewDataStore.likeReview(reviewId, status)
}