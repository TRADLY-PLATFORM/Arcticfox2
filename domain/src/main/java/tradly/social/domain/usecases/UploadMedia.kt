package tradly.social.domain.usecases

import tradly.social.domain.entities.FileInfo
import tradly.social.domain.repository.MediaRepository

class UploadMedia(val mediaRepository: MediaRepository) {
    suspend operator fun invoke(fileList:List<FileInfo>, shouldCreateThumbnail:Boolean) = mediaRepository.uploadMedia(fileList, shouldCreateThumbnail)

}