package tradly.social.common.network.entity

import tradly.social.domain.entities.Constant
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


/**
 * Data layer model. use only for parsing
 */
@JsonClass(generateAdapter = true)
data class AppConfigResponse(
    @field:Json(name = "data") val data: AppConfigEntity
) : ResponseStatus()

@JsonClass(generateAdapter = true)
data class AppConfigEntity(
    @field:Json(name = "configs") val appConfig: ConfigEntity,
    @field:Json(name = "key") val key: Key = Key(),
    @field:Json(name = "languages") var localeSupported: List<LocaleEntity>? = listOf(),
    @field:Json(name = "logo_path") var logoPath:String = Constant.EMPTY,
    @field:Json(name = "module") val module:Int = 0
)

@JsonClass(generateAdapter = true)
data class LocaleEntity(
    @field:Json(name = "name") val name: String = Constant.EMPTY,
    @field:Json(name = "code") val locale: String = Constant.EMPTY,
    @field:Json(name = "default") val default:Boolean = false
)

@JsonClass(generateAdapter = true)
data class ConfigEntity(
    @field:Json(name = "auth_type") val authType:Int = 0,
    @field:Json(name = "branch_link_description") val branchLinkDescription:String = Constant.EMPTY,
    @field:Json(name = "faq_url") val faqUrl:String = Constant.EMPTY,
    @field:Json(name = "listing_map_location_selector_enabled") val listingMapLocationSelectorEnabled:Boolean = false,
    @field:Json(name = "account_map_location_selector_enabled") val accountMapLocationSelectorEnabled:Boolean = false,
    @field:Json(name = "home_location_enabled") val homeLocationEnabled:Boolean = false,
    @field:Json(name = "enable_feedback") val enableFeedback:Boolean = false,
    @field:Json(name = "branch_link_base_url") val branchLinkBaseUrl:String = Constant.EMPTY
)
@JsonClass(generateAdapter = true)
data class Key(
    @field:Json(name = "app_key")val tenantKey: String = Constant.EMPTY
)