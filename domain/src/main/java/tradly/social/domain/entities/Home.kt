package tradly.social.domain.entities

class Home(
    var categories: List<Category> = mutableListOf(),
    var promoBanners: List<PromoBanner> = mutableListOf(),
    var collections:ArrayList<Collection>,
    val clientVersion:Int
)