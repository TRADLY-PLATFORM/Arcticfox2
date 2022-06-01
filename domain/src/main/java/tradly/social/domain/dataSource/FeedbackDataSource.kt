package tradly.social.domain.dataSource

import tradly.social.domain.entities.Category
import tradly.social.domain.entities.FileInfo
import tradly.social.domain.entities.Result

interface FeedbackDataSource {
    fun sendFeedback(categoryName: String,mood:Int,emailOrPhone:String,title:String,addInfo:String,attachmentList:List<FileInfo>,deviceMap:HashMap<String,Any?>):Result<Boolean>
    fun getFeedbackCategories():Result<List<Category>>
}