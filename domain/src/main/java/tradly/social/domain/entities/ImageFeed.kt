package tradly.social.domain.entities

import tradly.social.domain.entities.Constant

data class ImageFeed(var filePath:String = Constant.EMPTY, var isAddItem:Boolean = false)