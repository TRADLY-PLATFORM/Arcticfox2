package tradly.social.common.network.entity

import tradly.social.domain.entities.Constant
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CategoryResponse(
    @field:Json(name = "data") val categoryData: CategoryData = CategoryData()
):ResponseStatus()

@JsonClass(generateAdapter = true)
data class CategoryData(
    @field:Json(name = "categories") val categoryList: List<CategoryEntity> = listOf()
)
@JsonClass(generateAdapter = true)
data class CategoryEntity(
    @field:Json(name = "id") val id: String = Constant.EMPTY,
    @field:Json(name = "name") val name: String = Constant.EMPTY,
    @field:Json(name = "image_path") val imagePath: String = Constant.EMPTY,
    @field:Json(name = "has_sub_category") val hasSubCategory: Boolean = false,
    @field:Json(name = "parent") val parent:Int = 0,
    @field:Json(name = "sub_category") val subCategory:List<CategoryEntity> = listOf(),
    @field:Json(name = "hierarchy") val hierarchy:List<CategoryHierarchyResponse> = listOf()
)

@JsonClass(generateAdapter = true)
data class FeedbackCategoryResponse(
    @field:Json(name = "data") val categoryData: FeedbackCategoryData = FeedbackCategoryData()
):ResponseStatus()

@JsonClass(generateAdapter = true)
data class FeedbackCategoryData(
    @field:Json(name = "categories") val feedBackCategoryList: List<String> = listOf()
)

@JsonClass(generateAdapter = true)
data class CategoryHierarchyResponse(
    @field:Json(name = "id") val id:Int=0,
    @field:Json(name = "name") val name:String?=null,
    @field:Json(name = "level") val level:Int = 0
)

