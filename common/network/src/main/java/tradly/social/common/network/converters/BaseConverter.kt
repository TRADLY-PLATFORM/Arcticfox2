package tradly.social.common.network.converters

import tradly.social.domain.entities.Constant

open class BaseConverter {
     inline fun <reified T> castOrDefault(anything: Any?): T? {
        return anything as? T
    }

    fun getStringOrEmpty(data:String?) = data?:Constant.EMPTY
}