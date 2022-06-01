package tradly.social.common.util.parser

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object TradlyParser {
    var moshi: Moshi

    init {
        moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    }

    inline fun <reified T> getAdapter(): JsonAdapter<T> {
        val adapter = moshi.adapter<T>(T::class.java)
        return adapter
    }
}

