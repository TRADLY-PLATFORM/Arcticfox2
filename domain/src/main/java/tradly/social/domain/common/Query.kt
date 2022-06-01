package tradly.social.domain.common

import kotlinx.serialization.json.json


class Query private constructor(val className:String,val queryConstraint:String){
     object Comparator{
        const val PREFIX = "$"
        internal const val LESS_THAN = "${PREFIX}le"
    }
    class Builder(val className:String){
        var map = mapOf<String,Any>()
        var json = json {  }
        fun whereEqualTo(key:String,value:Any){
            constraint(key,value)
        }
        fun whereLessThan(key:String,value:Any){
            constraint(key,json{Comparator.LESS_THAN to value})
        }
        private fun constraint(key:String,value:Any){
            json.apply { key to value }

        }

        fun build() = Query(this.className,"where=${json.toString()}")
    }
}






