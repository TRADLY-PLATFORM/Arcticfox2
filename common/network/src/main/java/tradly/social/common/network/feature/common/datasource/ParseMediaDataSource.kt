package tradly.social.common.network.feature.common.datasource

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import tradly.social.common.network.Header
import tradly.social.common.network.base.BaseService
import tradly.social.common.network.parseHelper.ParseCloudFunction
import tradly.social.common.network.retrofit.MediaAPI
import tradly.social.domain.dataSource.MediaDataSource
import tradly.social.domain.entities.*
import java.io.File

class ParseMediaDataSource:MediaDataSource,BaseService() {

    val apiService = getRetrofitService(MediaAPI::class.java)

    override fun uploadMedia(fileList:List<FileInfo>,shouldCreateThumbnail:Boolean): Result<List<FileInfo>> {
        when(val result = apiCall(apiService.getSignedUrl(getPostData(fileList)))){
            is Result.Success->{
                val signedUrlList = result.data.response.signedUrlList
                for(i in 0 until(fileList.count())){
                    val mSignedUrl = signedUrlList[i]
                    val mFileList = fileList[i].also {
                        it.uploadedUrl = mSignedUrl.fileUri
                    }
                   /* val file = File(mFileList.fileUri)
                    val requestBody = file.readBytes().toRequestBody(mFileList.type.toMediaType())
                    val headerMap = mapOf(
                        //Headers.THUMBNAIL_IMAGE to shouldCreateThumbnail.toString(),
                        Header.CONTENT_TYPE to mFileList.type
                    )
                    when(val uploadResult = apiCall(apiService.uploadMedia(mSignedUrl.signedUrl, headerMap, requestBody))){
                        is Result.Error->{
                            return Result.Error(exception = uploadResult.exception)
                        }
                    }*/
                    when(val uploadResult = uploadMediaToSignedUrl(mSignedUrl.signedUrl,mFileList.fileUri!!,mFileList.type,shouldCreateThumbnail)){
                        is Result.Error->{
                            return Result.Error(exception = uploadResult.exception)
                        }
                    }
                }
                return Result.Success(data = fileList)
            }
            is Result.Error->{return result}
        }
    }

    override fun uploadMediaToSignedUrl(signedUrl:String,fileUri:String,fileType:String,shouldCreateThumbnail: Boolean):Result<FileInfo> {
        val file = File(fileUri)
        val requestBody = file.readBytes().toRequestBody(fileType.toMediaType())
        val headerMap = mapOf(
            //Headers.THUMBNAIL_IMAGE to shouldCreateThumbnail.toString(),
            Header.CONTENT_TYPE to fileType
        )
        return when(val uploadResult = getImageUploadResponse(apiService.uploadMedia(signedUrl, headerMap, requestBody))){
            is Result.Success-> Result.Success(FileInfo(fileUri,signedUrl,file.name,fileType))
            is Result.Error-> Result.Error(exception = uploadResult.exception)
        }
    }

    private fun getPostData(fileMetaList:List<FileInfo>):HashMap<String,Any?>{
        val metaList = mutableListOf<HashMap<String,Any?>>()
        for(meta in fileMetaList){
            metaList.add(hashMapOf(ParseCloudFunction.Params.NAME to meta.name , ParseCloudFunction.Params.TYPE to meta.type))
        }
        return hashMapOf(ParseCloudFunction.Params.FILES to metaList)
    }
}