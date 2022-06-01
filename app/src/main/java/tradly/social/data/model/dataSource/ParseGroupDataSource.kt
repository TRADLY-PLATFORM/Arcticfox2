package tradly.social.data.model.dataSource

import tradly.social.common.base.AppConstant
import tradly.social.common.network.converters.*
import tradly.social.data.model.network.retrofit.RetrofitManager
import tradly.social.common.network.parseHelper.ParseCloudFunction
import tradly.social.common.network.parseHelper.ParseConstant
import tradly.social.common.network.parseHelper.ParseManager
import tradly.social.domain.dataSource.GroupDataSource
import tradly.social.domain.entities.*
import com.google.gson.Gson
import com.parse.ParseObject

class ParseGroupDataSource : GroupDataSource {

    override fun getGroups(scope: String, pagination: Int, key: String) =
        when (val result = ParseManager.getInstance().getGroups(scope, pagination, key)) {
            is Result.Success -> {
                if (result.data["success"] == true) {
                    val list = Gson().fromJson(Gson().toJson(result.data), BaseResult::class.java)
                    Result.Success(data = list.groups)
                } else {
                    Result.Error(exception = AppError(code = result.data["error"] as Int))
                }
            }
            is Result.Error -> result
        }

    override fun addUserToGroup(groupId: String?): Result<String> = ParseManager.getInstance().addUserToGroup(groupId)

    override fun getMyGroups(limit: Int, pagination: Int): Result<List<Group>> =
        when (val result = ParseManager.getInstance().getMyGroups(limit, pagination)) {
            is Result.Success -> Result.Success(
                data = GroupConverter().mapFromList(
                    GroupMemberConverter().getGroupFromList(
                        result.data
                    )
                )
            )
            is Result.Error -> result
        }

    override fun getGroup(groupId: String?,sort: String?,pagination: Int,isMyProduct: Boolean): Result<BaseResult> {
        val map = HashMap<String, Any?>()
        if (!groupId.isNullOrEmpty()) {
            map.put(ParseCloudFunction.Params.GROUP_ID, groupId)
        }
        map.put(ParseCloudFunction.Params.MY_PRODUCTS, isMyProduct)
        map.put(ParseCloudFunction.Params.SORT, sort)
        map.put(ParseCloudFunction.Params.PAGE, pagination)
        map.put(ParseCloudFunction.Params.MAX_RESULTS, AppConstant.ListPerPage.PRODUCT_LIST)

        when (val result = ParseManager.getInstance().getProducts(map)) {
            is Result.Success -> {
                if (result.data["success"] == true) {
                    val baseResult = Gson().fromJson(Gson().toJson(result.data),BaseResult::class.java)
                    return Result.Success(data = baseResult)
                } else {
                    return Result.Error(exception = AppError(code = result.data["error"] as Int))
                }
            }
            is Result.Error -> {
                return result
            }
        }
    }

    override fun removeUserFromGroup(groupId: String?): Result<Boolean> =
        ParseManager.getInstance().removeUserFromGroup(groupId)

    override fun getGroupRelatedProduct(groupId: String?, pagination: Int): Result<List<Product>> =
        when (val result = ParseManager.getInstance().getGroupRelationQueryFromPin(groupId)) {
            is Result.Success -> {
                when (val list = ParseManager.getInstance()
                    .getRelation(result.data.getRelation<ParseObject>(ParseConstant.LISTINGS).query, pagination)) {
                    is Result.Success -> {
                        Result.Success(data = listOf())
                    }
                    is Result.Error -> list
                }
            }
            is Result.Error -> result
        }

    override fun getGroupMembers(groupId: String?): Result<List<User>> =
        when(val result = ParseManager.getInstance().getGroupMembers(groupId)){
            is Result.Success->{Result.Success(data = UserModelConverter().mapFromList(GroupMemberConverter().getMemberFromList(result.data)))}
            is Result.Error->{result}
        }

    override fun addGroup(group: Group): Result<Boolean> =
        when(val result = RetrofitManager.getInstance().addGroup(getAddGroupRequestBody(group))){
            is Result.Success->{Result.Success(result.data.status)}
            is Result.Error->{result}
        }

    override fun getGroupType(): Result<List<GroupType>> =
        when(val result = RetrofitManager.getInstance().getGroupTypes()){
            is Result.Success->{ Result.Success(GroupTypeConverter().mapFromList(result.data.responseData.groupTypes)) }
            is Result.Error->{result}
        }

    //Request Body
    private fun getAddGroupRequestBody(group: Group):HashMap<String,Any?>{
        val map = hashMapOf<String,Any?>()
        val data = hashMapOf<String,Any?>()
        data["type_id"] = group.groupType
        data["name"] = group.groupName
        data["description"] = group.groupDescription
        data["image_path"] = group.groupPic
        data["private"] = false
        data["address"] = group.groupAddress
        data["coordinates"] = hashMapOf("latitude" to group.location?.latitude, "longitude" to group.location?.longitude)
        map["group"] = data
        return map
    }
}