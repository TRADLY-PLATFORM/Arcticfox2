package tradly.social.domain.entities

data class FileInfo(var fileUri:String?=Constant.EMPTY, var uploadedUrl:String=Constant.EMPTY, var name:String=Constant.EMPTY, var type:String=Constant.EMPTY)