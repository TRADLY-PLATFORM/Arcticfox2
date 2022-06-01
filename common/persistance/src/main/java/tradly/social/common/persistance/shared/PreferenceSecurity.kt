package tradly.social.common.persistance.shared

import android.content.Context
import android.content.SharedPreferences
import com.yakivmospan.scytale.Crypto
import com.yakivmospan.scytale.Options
import com.yakivmospan.scytale.Store
import tradly.social.common.common.AppConstant
import javax.crypto.SecretKey

object PreferenceSecurity {

      private lateinit var  sharedPreferences:SharedPreferences
      private lateinit var languagePreference:SharedPreferences
      private lateinit var secretKey: SecretKey


       fun init(context: Context){
            sharedPreferences = context.getSharedPreferences(AppConstant.PREF_APP,Context.MODE_PRIVATE)
            languagePreference = context.getSharedPreferences(AppConstant.PREF_LANGUAGE,Context.MODE_PRIVATE)
            secretKey = getKey(context)
        }

        private fun getEncryptedToken(token: String): String? {
            val crypto = Crypto(Options.TRANSFORMATION_SYMMETRIC)
            crypto.setErrorListener{
                it?.let {

                }
            }
            return crypto.encrypt(token, secretKey)
        }

        private fun getDecryptedToken(encryptedData: String?): String? {
            val crypto = Crypto(Options.TRANSFORMATION_SYMMETRIC)
            if(!encryptedData.isNullOrEmpty()){
                return crypto.decrypt(encryptedData, secretKey)
            }
            return AppConstant.EMPTY
        }

        private fun getKey(context: Context): SecretKey {
            val store = Store(context)
            val key: SecretKey
            key = when (!store.hasKey(AppConstant.DEFAULT_KEY)) {
                true -> store.generateSymmetricKey(AppConstant.DEFAULT_KEY, null)
                false -> store.getSymmetricKey(AppConstant.DEFAULT_KEY, null)
            }

            return key
        }

        fun getEncryptedString(value:String):String = getEncryptedToken(value)
            ?:AppConstant.EMPTY

        fun getDecryptedString(value:String):String = getDecryptedToken(value)
            ?:AppConstant.EMPTY

        fun getBoolean(key:String,defValue:Boolean):Boolean =  sharedPreferences.getBoolean(key,defValue)

        fun getInt(key:String ,defValue:Int) = sharedPreferences.getInt(key,defValue)

        fun getString(key: String):String = sharedPreferences.getString(key,AppConstant.EMPTY)?:AppConstant.EMPTY

        fun getString(key: String,defValue:String,prefName:String) = when(prefName){
            AppConstant.PREF_APP-> getString(key)
            AppConstant.PREF_LANGUAGE-> languagePreference.getString(key,defValue)?:AppConstant.EMPTY
            else -> AppConstant.EMPTY
        }

        fun putBoolean(key:String,value:Boolean){
            with(sharedPreferences.edit()){
                putBoolean(key, value)
                apply()
            }
        }

        fun putInt(key:String,value:Int){
            with(sharedPreferences.edit()){
                putInt(key, value)
                apply()
            }
        }

       fun putString(key: String,value:String){
           with(sharedPreferences.edit()){
               putString(key,value)
               apply()
           }
       }

       fun putString(key: String,value:String,prefName:String){
           when(prefName){
               AppConstant.PREF_APP-> putString(key, value)
               AppConstant.PREF_LANGUAGE->{
                   with(languagePreference.edit()){
                       putString(key,value)
                       apply()
                   }
               }
           }
       }

       fun putStringWithEncrypt(key: String,value:String){
           val encData = getEncryptedString(value)
           with(sharedPreferences.edit()){
               putString(key,encData)
               apply()
           }
       }

       fun getStringWithDecryption(key: String):String{
           val decData = getString(key)
           if (decData.isNotEmpty()){
               return getDecryptedString(decData)
           }
           return AppConstant.EMPTY
       }

       fun clearValue(key: String){
           with(sharedPreferences.edit()){
               remove(key)
               apply()
           }
       }
}