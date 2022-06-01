package tradly.social.domain.repository

import tradly.social.domain.dataSource.MediaDataSource
import tradly.social.domain.entities.FileInfo
import tradly.social.domain.entities.Result

class MediaRepository(val mediaDataSource: MediaDataSource) {
    suspend fun uploadMedia(fileList:List<FileInfo>, shouldCreateThumbnail:Boolean):Result<List<FileInfo>> = mediaDataSource.uploadMedia( fileList, shouldCreateThumbnail)
}