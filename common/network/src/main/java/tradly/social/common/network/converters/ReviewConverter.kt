package tradly.social.common.network.converters

import tradly.social.common.network.entity.RatingEntity
import tradly.social.common.network.entity.ReviewData
import tradly.social.common.network.entity.ReviewEntity
import tradly.social.domain.entities.Rating
import tradly.social.domain.entities.RatingCount
import tradly.social.domain.entities.Review

class ReviewConverter {

    companion object {
        fun mapFromList(list: List<ReviewEntity>): List<Review> {
            val reviewList = mutableListOf<Review>()
            list.forEach { reviewEntity ->
                reviewList.add(mapFrom(reviewEntity))
            }
            return reviewList
        }

        private fun mapFrom(reviewEntity: ReviewEntity):Review{
            return Review(
                reviewEntity.id,
                reviewEntity.title,
                reviewEntity.content,
                reviewEntity.rating,
                reviewEntity.createdAt * 1000L,
                UserModelConverter().mapFrom(reviewEntity.user),
                reviewEntity.likeStatus,
                reviewEntity.images
            )
        }

        fun mapFrom(ratingEntity: RatingEntity):Rating{
            return Rating(
                ratingAverage = ratingEntity.ratingAverage,
                ratingCount = ratingEntity.ratingCount,
                reviewCount = ratingEntity.reviewCount
            ).also {
                if (ratingEntity.ratingCountData!=null){
                    it.ratingDataCount = RatingCount(
                        ratingEntity.ratingCountData.rating1,
                        ratingEntity.ratingCountData.rating2,
                        ratingEntity.ratingCountData.rating3,
                        ratingEntity.ratingCountData.rating4,
                        ratingEntity.ratingCountData.rating5
                    )
                }
            }
        }

        fun mapFrom(reviewData: ReviewData): Rating {
            return Rating().apply {
                if (reviewData.ratingData != null) {
                    ratingAverage = reviewData.ratingData.ratingAverage
                    ratingCount = reviewData.ratingData.ratingCount
                    reviewCount = reviewData.ratingData.reviewCount
                    if (reviewData.ratingData.ratingCountData!=null){
                        ratingDataCount = RatingCount(
                            reviewData.ratingData.ratingCountData.rating1,
                            reviewData.ratingData.ratingCountData.rating2,
                            reviewData.ratingData.ratingCountData.rating3,
                            reviewData.ratingData.ratingCountData.rating4,
                            reviewData.ratingData.ratingCountData.rating5
                        )
                    }
                }
                if(reviewData.myReview!=null){
                   myReview = mapFrom(reviewData.myReview)
                }
                reviewList = mapFromList(reviewData.reviews)
            }
        }
    }
}