package tradly.social.domain.usecases

import tradly.social.domain.entities.FileInfo
import tradly.social.domain.repository.FeedbackRepository

class SendFeedback (val feedbackRepository: FeedbackRepository){
    fun sendFeedback(categoryName: String, mood:Int, emailOrPhone:String, title:String, addInfo:String, attachmentList:List<FileInfo>, deviceMap:HashMap<String,Any?>) = feedbackRepository.sendFeedback(categoryName, mood, emailOrPhone, title, addInfo, attachmentList, deviceMap)
    fun getCategories() = feedbackRepository.getFeedbackCategories()
}