package tradly.social.data.model.dataSource

import tradly.social.common.network.converters.ReviewConverter
import tradly.social.data.model.network.retrofit.RetrofitManager
import tradly.social.common.network.CustomError
import tradly.social.domain.dataSource.ReviewDataStore
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Rating
import tradly.social.domain.entities.Result

class ParseReviewDataSource:ReviewDataStore {
    override fun getReviewList(id: String, type: String, page: Int): Result<Rating>  =
        when(val result = RetrofitManager.getInstance().getReviewList(getReviewParam(id,type,page))){
            is Result.Success-> Result.Success(ReviewConverter.mapFrom(result.data.data))
            is Result.Error->result
        }

    override fun addReview(
        type: String,
        id: String,
        title: String,
        content: String,
        rating: Int,
        images: List<String>
    ): Result<Boolean> {

        return when(val result = RetrofitManager.getInstance().addReview(getAddReviewParam(type,id,title, content, rating, images))){
            is Result.Success->{
                if(result.data.status){
                    Result.Success(result.data.status)
                }
                else{
                    Result.Error(exception = AppError(code = CustomError.REVIEW_SUBMIT_FAILED))
                }
            }
            is Result.Error->result
        }

    }

    override fun likeReview(reviewId: String, status: Int): Result<Boolean> =
        when(val result = RetrofitManager.getInstance().likeReview(reviewId,getReviewLikeParam(status))){
            is Result.Success->Result.Success(result.data.status)
            is Result.Error->result
        }


    private fun getReviewParam(id: String,type:String, pagination: Int) =
        hashMapOf(
            "id" to id,
            "type" to type,
            "page" to pagination
        )

    private fun getAddReviewParam(type:String, id:String, title:String, content:String, rating:Int, images:List<String>):HashMap<String,Any?>{
        val map = hashMapOf<String,Any?>()
        map["type"] = type
        map["id"] = id.toInt()
        if(title.isNotEmpty()){
            map["title"] = title
        }
        map["content"] = content
        if(images.isNotEmpty()){
            map["images"] = images
        }
        map["rating"] = rating
        return hashMapOf<String,Any?>("review" to map)
    }


    private fun getReviewLikeParam(status:Int):HashMap<String,Any?>{
        return hashMapOf("review" to hashMapOf<String,Any?>("status" to status))
    }
}