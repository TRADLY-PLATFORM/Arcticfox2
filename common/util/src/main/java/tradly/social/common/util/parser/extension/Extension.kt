package tradly.social.common.util.parser.extension

import com.squareup.moshi.Types
import org.json.JSONObject
import tradly.social.common.util.common.AppConstant

inline fun <reified T> String?.toObject(): T? {
    val adapter = tradly.social.common.util.parser.TradlyParser.getAdapter<T>()
    return if (!this.isNullOrEmpty()) adapter.fromJson(this) else null
}

inline fun <reified T> Any?.toJson(): String {
    val adapter = tradly.social.common.util.parser.TradlyParser.getAdapter<T>()
    return if (this != null) adapter.toJson(this as? T) else AppConstant.EMPTY
}

inline fun <reified T> String?.toList(): List<T> {
    val type = Types.newParameterizedType(List::class.java, T::class.java)
    val adapter = tradly.social.common.util.parser.TradlyParser.moshi.adapter<List<T>>(type)
    return if (!this.isNullOrEmpty()) adapter.fromJson(this)?: listOf() else listOf()
}

fun String?.getValue() = this ?: AppConstant.EMPTY

fun String?.toJsonObject(): JSONObject {
    try{
        if(!this.isNullOrEmpty()){
            return JSONObject(this)
        }
    }catch (ex:Exception){}
    return JSONObject()
}