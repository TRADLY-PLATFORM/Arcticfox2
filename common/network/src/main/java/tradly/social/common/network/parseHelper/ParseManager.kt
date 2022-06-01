package tradly.social.common.network.parseHelper

import android.content.Context
import tradly.social.domain.entities.*
import com.parse.*
import com.parse.ktx.putOrIgnore
import com.parse.livequery.ParseLiveQueryClient
import org.json.JSONArray
import tradly.social.common.network.CustomError


class ParseManager {

    private var parseLiveQueryClient: ParseLiveQueryClient? = null

    companion object {
        @Volatile
        private var INSTANCE: ParseManager? = null

        fun getInstance(): ParseManager {
            return INSTANCE ?: synchronized(this) {
                ParseManager().also {
                    INSTANCE = it
                }
            }
        }
    }


    fun initializeParse(context: Context, appConfig: AppConfig) {
        Parse.enableLocalDatastore(context)
       /* Parse.initialize(
            Parse.Configuration.Builder(context)
                .applicationId(appConfig.tenantKey)
                .clientKey(appConfig.tenantKey)
                .server(appConfig.tenantKey)
                .enableLocalDataStore()
                .build()
        )*/
        parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient()
    }

    fun cacheParseConfig(appConfig: AppConfig) {

    }


    fun getUser(id: String): Result<ParseUser> =
        try {
            val query = ParseUser.getQuery()
            val result = query.get(id)
            Result.Success(result)
        } catch (exception: ParseException) {
            Result.Error(exception = AppError(exception.message, exception.code))
        }

    fun getUserCurrency(): String? {
        val user = ParseUser.getCurrentUser()
        val parseCountryObject = user.getParseObject(ParseConstant.COUNTRY)
        parseCountryObject?.run {
            when (this.isDataAvailable) {
                true -> {
                    return this.getString(ParseConstant.CURRENCY)
                }
                false -> {
                    return Constant.EMPTY
                }
            }
        }
        return Constant.EMPTY
    }

    //endregion
    fun getHome(): Result<HashMap<String, Any?>> =
        try {
            val result =
                ParseCloud.callFunction<HashMap<String, Any?>>(ParseCloudFunction.APP_HOME, hashMapOf<String, Any?>())
            Result.Success(data = result)
        } catch (exception: ParseException) {
            Result.Error(exception = AppError(exception.message, exception.code))
        }

    fun getCountries(): Result<List<ParseObject>> {
        try {
            val parseQuery = ParseQuery.getQuery<ParseObject>(ParseClasses.COUNTRIES)
            parseQuery.whereEqualTo(ParseConstant.ACTIVE, true)
            /*val cacheQuery = parseQuery.fromPin(ParseConstant.PIN_COUNTRY)
            val cachedResult = cacheQuery.find()
            if (cachedResult.count() > 0) {

                return Result.Success(data = cachedResult)
            }
*/
            val fromNetwork = parseQuery.fromNetwork().find()
            //ParseObject.unpinAll(ParseConstant.PIN_COUNTRY, fromNetwork)
            //ParseObject.pinAll(ParseConstant.PIN_COUNTRY, fromNetwork)
            return Result.Success(data = fromNetwork)
        } catch (exception: ParseException) {
            return Result.Error(exception = AppError(exception.message, exception.code))
        }
    }

    fun getCountry(id: String): Result<ParseObject> =
        try {
            val query = ParseQuery.getQuery<ParseObject>(ParseClasses.COUNTRIES)
            val result = query.get(id)
            Result.Success(data = result)

        } catch (exception: ParseException) {
            Result.Error(exception = AppError(exception.message, exception.code))
        }

    fun updateUserDetail(hashMap: HashMap<String, Any>): Result<ParseUser> =
        try {
            val parseUser = ParseUser.getCurrentUser()
            for ((k, v) in hashMap) {
                parseUser.put(k, v)
            }
            parseUser.save()
            Result.Success(data = parseUser)
        } catch (parseException: ParseException) {
            Result.Error(exception = AppError(parseException.message, parseException.code))
        }

    fun addStore(store: ParseObject): Result<ParseObject> =
        try {
            when (val result = updateUserDetail(hashMapOf(ParseConstant.USER_TYPE to ParseConstant.UserType.SELLER))) {
                is Result.Success -> {
                    store.apply { put(ParseConstant.USER, result.data) }.also { it.save() }
                    Result.Success(data = store)
                }
                is Result.Error -> result

            }

        } catch (parseException: ParseException) {
            Result.Error(exception = AppError(parseException.message, parseException.code))
        }

    fun findStoreByUser(userId: String): Result<ParseObject> =
        try {
            when (val parseUserResult = getUser(userId)) {
                is Result.Success -> findStoreByUser(parseUserResult.data)
                is Result.Error -> parseUserResult
            }
        } catch (parseException: ParseException) {
            Result.Error(exception = AppError(parseException.message, parseException.code))
        }

    fun findStoreByUser(user:ParseUser):Result<ParseObject> =
        try{
            val query = ParseQuery.getQuery<ParseObject>(ParseClasses.STORE_DETAILS)
            query.whereEqualTo(ParseConstant.USER, user)
            query.whereEqualTo(ParseConstant.BLOCKED, false)
            val result = query.first
            Result.Success(result)
        }catch (parseException: ParseException) {
            Result.Error(exception = AppError(parseException.message, parseException.code))
        }

    fun followStore(storeId: String?): Result<String> =
        try {
            val parseObject = ParseObject(ParseClasses.STORE_FOLLOWERS)
            val storeObject = ParseObject.createWithoutData(ParseClasses.STORE_DETAILS, storeId)
            parseObject.putOrIgnore(ParseConstant.USER, ParseUser.getCurrentUser())
            parseObject.putOrIgnore(ParseConstant.STORE, storeObject)
            parseObject.save()
            storeObject.increment(ParseConstant.FOLLOWERS_COUNT)
            Result.Success(data = parseObject.objectId)
        } catch (parseException: ParseException) {
            Result.Error(exception = AppError(parseException.message, parseException.code))
        }

    fun getStores(scope: String, pagination: Int = 0, key: String = Constant.EMPTY): Result<HashMap<String, Any?>> =
        try {
            val map = hashMapOf(
                ParseCloudFunction.Params.SCOPE to scope,
                ParseCloudFunction.Params.PAGE to pagination,
                ParseCloudFunction.Params.KEY to key
            )
            val result = ParseCloud.callFunction<HashMap<String, Any?>>(ParseCloudFunction.STORES, map)
            Result.Success(data = result)
        } catch (parseException: ParseException) {
            Result.Error(exception = AppError(parseException.message, parseException.code))
        }

    fun getStoreType(): Result<List<ParseObject>> =
        try {
            val query = ParseQuery.getQuery<ParseObject>(ParseClasses.STORE_TYPE)
            query.whereEqualTo(ParseConstant.ACTIVE, true)
            val result = query.find()
            Result.Success(result)
        } catch (parseException: ParseException) {
            Result.Error(exception = AppError(parseException.message, parseException.code))
        }


    fun getPromoBanner(type: Int = 0): Result<List<ParseObject>> =
        try {
            val parseQuery = ParseQuery.getQuery<ParseObject>(ParseClasses.PROMO_BANNER)
            parseQuery.whereEqualTo(ParseConstant.PROMO_TYPE, 1)
            val result = parseQuery.find()
            Result.Success(data = result)
        } catch (parseException: ParseException) {
            Result.Error(exception = AppError(parseException.message, parseException.code))
        }

    fun getCategories(map: HashMap<String, Any?>): Result<String> =
        try {
            Result.Success(data = JSONArray().toString())
        } catch (ex: ParseException) {
            Result.Error(exception = AppError(ex.message, ex.code))
        }


    fun getCategories(limit: Int = 0, pagination: Int = 0): Result<List<ParseObject>> =
        try {
            val parseQuery = ParseQuery.getQuery<ParseObject>(ParseClasses.CATEGORIES)
            parseQuery.whereEqualTo(ParseConstant.ACTIVE, true)
            parseQuery.include(ParseConstant.TRANSLATION)
            parseQuery.orderByAscending(ParseConstant.ORDER_BY)
            if (limit != 0) {
                parseQuery.limit = limit
            }
            parseQuery.skip = pagination
            val result = parseQuery.find()
            Result.Success(data = result)

        } catch (parseException: ParseException) {
            Result.Error(exception = AppError(parseException.message, parseException.code))
        }

    fun getBrands(limit: Int = 0, pagination: Int = 0): Result<List<ParseObject>> =
        try {
            val innerQuery = ParseQuery.getQuery<ParseObject>(ParseClasses.BRAND_FOLLOWERS)
            innerQuery.whereEqualTo(ParseConstant.USER, ParseUser.getCurrentUser())
            val query = ParseQuery.getQuery<ParseObject>(ParseClasses.BRANDS)
            query.whereDoesNotMatchKeyInQuery("objectId", ParseConstant.BRAND + ".objectId", innerQuery)
            query.whereEqualTo(ParseConstant.ACTIVE, true)
            query.limit = limit
            query.skip = pagination
            val result = query.find()
            Result.Success(data = result)
        } catch (parseException: ParseException) {
            Result.Error(exception = AppError(parseException.message, parseException.code))
        }

    fun getGroups(scope: String, pagination: Int = 0, key: String = Constant.EMPTY): Result<HashMap<String, Any?>> =
        try {
            val result = ParseCloud.callFunction<HashMap<String, Any?>>(
                ParseCloudFunction.GROUPS, hashMapOf(
                    ParseCloudFunction.Params.IS_MY_GROUP to false,
                    ParseCloudFunction.Params.SCOPE to scope,
                    ParseCloudFunction.Params.KEY to key,
                    ParseCloudFunction.Params.PAGE to pagination
                )
            )
            Result.Success(data = result)

        } catch (parseException: ParseException) {
            Result.Error(exception = AppError(parseException.message, parseException.code))
        }

    fun getGroupTypes(): Result<List<ParseObject>> =
        try {
            val query = ParseQuery.getQuery<ParseObject>(ParseClasses.GROUP_TYPE)
            query.whereEqualTo(ParseConstant.ACTIVE, true)
            val result = query.find()
            Result.Success(result)
        } catch (parseException: ParseException) {
            Result.Error(exception = AppError(parseException.message, parseException.code))
        }

    fun getGroupRelationQueryFromPin(id: String?): Result<ParseObject> =
        try {
            val objects = ParseObject.createWithoutData(ParseClasses.GROUPS, id)
            objects.fetchFromLocalDatastore()
            if (objects.isDataAvailable) {
                Result.Success(data = objects)
            } else {
                Result.Error(exception = AppError(code = CustomError.NO_OBJECT_FOUND_IN_LOCAL))
            }
        } catch (exception: ParseException) {
            Result.Error(exception = AppError(exception.message, exception.code))
        }

    fun getGroupMembers(groupId: String?): Result<List<ParseObject>> =
        try {
            val query = ParseQuery.getQuery<ParseObject>(ParseClasses.GROUP_MEMBERS)
            query.whereEqualTo(ParseConstant.GROUP, ParseObject.createWithoutData(ParseClasses.GROUPS, groupId))
            query.include(ParseConstant.USER)
            query.limit = 10
            val result = query.find()
            Result.Success(data = result)
        } catch (exception: ParseException) {
            Result.Error(exception = AppError(exception.message, exception.code))
        }

    fun addGroup(parseObject: ParseObject): Result<String> =
        try {
            parseObject.save()
            Result.Success(parseObject.objectId)
        } catch (exception: ParseException) {
            Result.Error(exception = AppError(exception.message, exception.code))
        }

    fun editGroup(parseObject: ParseObject): Result<Boolean> =
        try {
            parseObject.save()
            Result.Success(true)
        } catch (exception: ParseException) {
            Result.Error(exception = AppError(exception.message, exception.code))
        }


    fun getRelation(query: ParseQuery<ParseObject>, pagination: Int): Result<List<ParseObject>> =
        try {
            query.limit = 20
            query.skip = pagination * 10
            query.whereEqualTo(ParseConstant.ARCHEIVED, false)
            query.whereEqualTo(ParseConstant.ACTIVE, true)
            val result = query.find()
            Result.Success(data = result)
        } catch (exception: ParseException) {
            Result.Error(exception = AppError(exception.message, exception.code))
        }

    fun getMyGroups(limit: Int = 0, pagination: Int = 0): Result<List<ParseObject>> =
        try {
            val query = ParseQuery.getQuery<ParseObject>(ParseClasses.GROUP_MEMBERS)
            query.whereEqualTo(ParseConstant.USER, ParseUser.getCurrentUser())
            query.include(ParseConstant.GROUP)
            if (limit != 0) {
                query.limit = limit
            }
            query.skip = pagination
            val result = query.find()
            Result.Success(data = result)
        } catch (ex: ParseException) {
            Result.Error(exception = AppError(ex.message, ex.code))
        }

    fun addUserToGroup(groupId: String?): Result<String> =
        try {
            val parseObject = ParseObject(ParseClasses.GROUP_MEMBERS)
            val groupObj = ParseObject.createWithoutData(ParseClasses.GROUPS, groupId)
            parseObject.putOrIgnore(ParseConstant.GROUP, groupObj)
            parseObject.putOrIgnore(ParseConstant.USER, ParseUser.getCurrentUser())
            parseObject.save()
            groupObj.increment(ParseConstant.MEMBERS_COUNT)
            groupObj.save()
            Result.Success(data = parseObject.objectId)
        } catch (ex: ParseException) {
            Result.Error(exception = AppError(ex.message, ex.code))
        }

    fun removeUserFromGroup(groupId: String?): Result<Boolean> =
        try {
            val query = ParseQuery.getQuery<ParseObject>(ParseClasses.GROUP_MEMBERS)
            val grpObj = ParseObject.createWithoutData(ParseClasses.GROUPS, groupId)
            query.whereEqualTo(ParseConstant.GROUP, grpObj)
            query.whereEqualTo(ParseConstant.USER, ParseUser.getCurrentUser())
            val result = query.first
            result?.let {
                result.delete()
                grpObj.increment(ParseConstant.MEMBERS_COUNT, -1)
                Result.Success(data = true)
            } ?: run {
                Result.Success(data = false)
            }
        } catch (exception: ParseException) {
            Result.Error(exception = AppError(exception.message, exception.code))
        }

    fun unfollowStore(storeId: String?): Result<Boolean> =
        try {
            val query = ParseQuery.getQuery<ParseObject>(ParseClasses.STORE_FOLLOWERS)
            query.whereEqualTo(ParseConstant.USER, ParseUser.getCurrentUser())
            query.whereEqualTo(ParseConstant.STORE, ParseObject.createWithoutData(ParseClasses.STORE_DETAILS, storeId))
            val result = query.first
            result?.let {
                result.delete()
                Result.Success(data = true)
            } ?: run {
                Result.Success(data = false)
            }
        } catch (exception: ParseException) {
            Result.Error(exception = AppError(exception.message, exception.code))
        }

    fun searchTags(keyWord: String): Result<List<ParseObject>> =
        try {
            val query = ParseQuery.getQuery<ParseObject>(ParseClasses.TAGS)
            query.whereStartsWith(ParseConstant.NAME, keyWord)
            val result = query.find()
            Result.Success(data = result)
        } catch (ex: ParseException) {
            Result.Error(exception = AppError(ex.message, ex.code))
        }


    //region Product
    fun addProduct(map: HashMap<String, Any?>): Result<String> =
        try {
            val result = ParseCloud.callFunction<HashMap<String, Any?>>(ParseCloudFunction.ADD_PRODUCT, map)
            //val jsonString = Gson().toJson(result)
            Result.Success(data = "")

        } catch (ex: ParseException) {
            Result.Error(exception = AppError(ex.message, ex.code))
        }


    fun getProducts(map: HashMap<String, Any?>): Result<HashMap<String, Any?>> =
        try {
            val result = ParseCloud.callFunction<HashMap<String, Any?>>(ParseCloudFunction.PRODUCTS, map)
            Result.Success(data = result)

        } catch (ex: ParseException) {
            Result.Error(exception = AppError(ex.message, ex.code))
        }

    fun getProduct(map: HashMap<String, Any?>): Result<HashMap<String, Any?>> =
        try {
            val result = ParseCloud.callFunction<HashMap<String, Any?>>(ParseCloudFunction.PRODUCT, map)
            Result.Success(data = result)
        } catch (ex: ParseException) {
            Result.Error(exception = AppError(ex.message, ex.code))
        }

    fun getUploadFileSignedUrl(map: HashMap<String, Any?>): Result<HashMap<String, Any?>> =
        try {
            val result = ParseCloud.callFunction<HashMap<String, Any?>>(ParseCloudFunction.UPLOAD_URL, map)
            Result.Success(data = result)
        } catch (ex: ParseException) {
            Result.Error(exception = AppError(ex.message, ex.code))
        }

    //endregion

    //region Cart

    fun addCartItem(map: HashMap<String, Any?>): Result<HashMap<String, Any?>> =
        try {
            val result = ParseCloud.callFunction<HashMap<String, Any?>>(ParseCloudFunction.ADD_CART, map)
            Result.Success(result)
        } catch (exception: ParseException) {
            Result.Error(exception = AppError(exception.message, exception.code))
        }

    fun removeCartItem(id: String?): Result<Boolean> =
        try {
            val parseObject = ParseObject.createWithoutData(ParseClasses.CART_DETAILS, id)
            parseObject.delete()
            Result.Success(true)
        } catch (exception: ParseException) {
            Result.Error(exception = AppError(exception.message, exception.code))
        }

    fun getCartItems(): Result<HashMap<String, Any?>> =
        try {
            val result = ParseCloud.callFunction<HashMap<String, Any?>>(ParseCloudFunction.CART, mapOf<String, Any>())
            Result.Success(result)
        } catch (exception: ParseException) {
            Result.Error(exception = AppError(exception.message, exception.code))
        }

    fun findCartItem(id:String?,type: Int):Result<ParseObject> =
        try{
            val query = ParseQuery.getQuery<ParseObject>(ParseClasses.CART_DETAILS)
           /* query.apply {
                when(type){
                    AppConstant.CartType.CART_PRODUCT->
                        whereEqualTo(
                            ParseConstant.LISTING,ParseObject.createWithoutData(
                                ParseClasses.LISTINGS,id))
                    AppConstant.CartType.CART_VARIANT->
                        whereEqualTo(
                            ParseConstant.VARIANT,ParseObject.createWithoutData(
                                ParseClasses.LISTING_VARIANTS,id))
                    AppConstant.CartType.CART_SET->
                        whereEqualTo(ParseConstant.SET,ParseObject.createWithoutData(ParseClasses.SETS,id))
                }
            }*/
            val result = query.first
            Result.Success(result)
        }catch (exception: ParseException) {
            Result.Error(exception = AppError(exception.message, exception.code))
        }

    //endregion

    //region Shipping Address

    fun getCurrentAddress():Result<ParseObject> =
        try{
            val query = ParseQuery.getQuery<ParseObject>(ParseClasses.SHIPPING_ADDRESS)
            query.whereEqualTo(ParseConstant.DEFAULT_ADDRESS,1)
            query.whereEqualTo(ParseConstant.USER, ParseUser.getCurrentUser())
            val result = query.first
            Result.Success(data = result)
        }
        catch (ex: ParseException) {
            Result.Error(exception = AppError(ex.message, ex.code))
        }

    fun getShippingAddress(): Result<List<ParseObject>> =
        try {
            val query = ParseQuery.getQuery<ParseObject>(ParseClasses.SHIPPING_ADDRESS)
            query.whereEqualTo(ParseConstant.USER, ParseUser.getCurrentUser())
            query.orderByAscending(ParseConstant.CREATED_AT)
            val result = query.find()
            Result.Success(data = result)
        } catch (ex: ParseException) {
            Result.Error(exception = AppError(ex.message, ex.code))
        }

    fun addShippingAddress(parseObject: ParseObject):Result<String> =
        try {
            val result = parseObject.save()
            Result.Success(parseObject.objectId)
        }catch (ex: ParseException) {
            Result.Error(exception = AppError(ex.message, ex.code))
        }

    fun updateShippingAddress(parseObject: ParseObject):Result<String> =
        try {
            val result = parseObject.save()
            Result.Success(parseObject.objectId)
        }catch (ex: ParseException) {
            Result.Error(exception = AppError(ex.message, ex.code))
        }

    fun changePresentAddress(currentAddress: ParseObject ,parseObject: ParseObject):Result<String> =
        try {
            currentAddress.save()
            parseObject.save()
            Result.Success(parseObject.objectId)
        }catch (ex: ParseException) {
            Result.Error(exception = AppError(ex.message, ex.code))
        }

    fun getCurrentAddressLocation(map:HashMap<String,Any?>) :Result<HashMap<String,Any?>> =
        try{
            val result = ParseCloud.callFunction<HashMap<String,Any?>>(ParseCloudFunction.GET_ADDRESS,map)
            Result.Success(result)
        }catch (ex: ParseException) {
            Result.Error(exception = AppError(ex.message, ex.code))
        }

    //endregion
}