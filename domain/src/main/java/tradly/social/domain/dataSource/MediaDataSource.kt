package tradly.social.domain.dataSource

import tradly.social.domain.entities.FileInfo
import tradly.social.domain.entities.Result

interface MediaDataSource {
    fun uploadMedia(fileList:List<FileInfo>,shouldCreateThumbnail:Boolean):Result<List<FileInfo>>
    fun uploadMediaToSignedUrl(signedUrl:String,fileUri:String,fileType:String,shouldCreateThumbnail: Boolean):Result<FileInfo>
}