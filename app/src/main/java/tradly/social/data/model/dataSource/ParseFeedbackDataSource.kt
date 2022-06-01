package tradly.social.data.model.dataSource

import tradly.social.common.network.converters.CategoryConverter
import tradly.social.data.model.network.retrofit.RetrofitManager
import tradly.social.common.base.ErrorHandler
import tradly.social.common.network.CustomError
import tradly.social.domain.dataSource.FeedbackDataSource
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Category
import tradly.social.domain.entities.FileInfo
import tradly.social.domain.entities.Result

class ParseFeedbackDataSource:FeedbackDataSource{
    override fun sendFeedback(
        categoryName: String,
        mood: Int,
        emailOrPhone: String,
        title: String,
        addInfo: String,
        attachmentList: List<FileInfo>,
        deviceMap: HashMap<String, Any?>
    ): Result<Boolean> {
        return when(val result = RetrofitManager.getInstance().sendFeedback(getPayload(categoryName, mood, emailOrPhone, title, addInfo, attachmentList, deviceMap))) {
            is Result.Success->{
                if(result.data.status){
                    Result.Success(true)
                }
                else{
                    Result.Error(exception = AppError(code = CustomError.FEEDBACK_FAILED))
                }
            }
            is Result.Error->result
        }
    }

    override fun getFeedbackCategories(): Result<List<Category>> {
        return when(val result = RetrofitManager.getInstance().getFeedbackCategories()){
            is Result.Success->Result.Success(CategoryConverter.mapFromStringList(result.data.categoryData.feedBackCategoryList))
            is Result.Error->result
        }
    }

    private fun getPayload(
    categoryName: String,
    mood: Int,
    emailOrPhone: String,
    title: String,
    addInfo: String,
    attachmentList: List<FileInfo>,
    deviceMap: HashMap<String, Any?>
    ):HashMap<String,Any?>{
        val map = hashMapOf<String,Any?>()
        map["category"] = categoryName
        if(mood!=0){
            map["mood"] = mood
        }
        if(emailOrPhone.isNotEmpty()){
            map["email"] = emailOrPhone
        }
        map["title"] = title
        if(addInfo.isNotEmpty()){
            map["description"] = addInfo
        }
        if(attachmentList.isNotEmpty()){
            map["attachments"] = attachmentList.map { it.uploadedUrl }
        }
        map["device_info"] = deviceMap
        return map
    }
}