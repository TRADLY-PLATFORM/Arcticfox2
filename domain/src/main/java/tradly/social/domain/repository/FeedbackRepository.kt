package tradly.social.domain.repository

import tradly.social.domain.dataSource.FeedbackDataSource
import tradly.social.domain.entities.FileInfo

class FeedbackRepository(val feedbackDataSource: FeedbackDataSource){
    fun sendFeedback(categoryName: String, mood:Int, emailOrPhone:String, title:String, addInfo:String, attachmentList:List<FileInfo>, deviceMap:HashMap<String,Any?>) = feedbackDataSource.sendFeedback(categoryName, mood, emailOrPhone, title, addInfo, attachmentList, deviceMap)
    fun getFeedbackCategories() = feedbackDataSource.getFeedbackCategories()
}