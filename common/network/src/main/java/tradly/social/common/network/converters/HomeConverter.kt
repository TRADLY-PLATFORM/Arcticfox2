package tradly.social.common.network.converters

import tradly.social.common.network.entity.CollectionEntity
import tradly.social.common.network.entity.HomeResponse
import tradly.social.common.network.parseHelper.ParseConstant
import tradly.social.domain.entities.*
import tradly.social.domain.entities.Collection

class HomeConverter {
    companion object{
        fun mapFrom(homeResponse: HomeResponse): Home =
            Home(
                promoBanners = PromoBannerConverter.mapFromList(homeResponse.homeEntity.promoBanners),
                categories = CategoryConverter.mapFromList(homeResponse.homeEntity.categories),
                collections = mapFromList(homeResponse.homeEntity.collections),
                clientVersion = homeResponse.homeEntity.clientVersion.version?:0
            )

        fun mapFromList(collectionEntityList: List<CollectionEntity>?): ArrayList<Collection> {
            val list = ArrayList<Collection>()
            collectionEntityList?.forEach { collectionItem ->
                Collection().apply {
                    id = collectionItem.id
                    scopeType = collectionItem.scopeType
                    viewType = collectionItem.viewType
                    backgroundColor = collectionItem.backgroundColor ?: Constant.EMPTY
                    backgroundUrl = collectionItem.backgroundUrl ?: Constant.EMPTY
                    title = collectionItem.title ?: Constant.EMPTY
                    description = collectionItem.description ?: Constant.EMPTY
                    imagePath = collectionItem.imagePath?:Constant.EMPTY
                }.also {
                    when(collectionItem.scopeType){
                        ParseConstant.HOME_SCOPE.INVITE_FRIENDS-> list.add(it)
                        ParseConstant.HOME_SCOPE.FEATURED_PRODUCTS->{
                            it.products =  ProductConverter.mapFromList(collectionItem.products)
                            list.add(it)
                        }
                        ParseConstant.HOME_SCOPE.STORE->{
                            it.stores = StoreModelConverter.mapFrom(collectionItem.stores)
                            list.add(it)
                        }
                    }

                }

            }
            return list
        }
    }
}