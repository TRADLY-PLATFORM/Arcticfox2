package tradly.social.common.network.converters

import tradly.social.domain.entities.AuthKeys
import tradly.social.domain.entities.Constant
import tradly.social.domain.entities.User
import com.parse.ParseUser
import tradly.social.common.network.entity.AuthKeyEntity
import tradly.social.common.network.entity.UserDetailResponse
import tradly.social.common.network.entity.UserEntity
import tradly.social.common.network.parseHelper.ParseConstant

class UserModelConverter {
    fun mapFrom(from: ParseUser?): User {
        from?.run {
            if(this.isDataAvailable){
                return User(
                    from.getBoolean(ParseConstant.ACTIVE),
                    from.getBoolean(ParseConstant.EMAIL_VERIFIED),
                    "",
                    from.getString(ParseConstant.USERNAME)?:"",
                    from.getString(ParseConstant.PASSWORD),
                    from.getString(ParseConstant.EMAIL),
                    from.getInt(ParseConstant.USER_TYPE),
                    Constant.EMPTY,
                    Constant.EMPTY,
                    "",
                    id = from.objectId,
                    updatedAt = from.updatedAt.time,
                    createdAt = from.createdAt.time
                )
            }
        }
        return User()
    }

    fun mapFromList(fromList:List<ParseUser?>):List<User>{
        val list = mutableListOf<User>()
        for (obj in fromList){
            list.add(mapFrom(obj))
        }
        return list
    }

    fun mapFrom(userEntity: UserEntity) = User().apply {
        id = userEntity.id
        firstName = userEntity.firstName?:Constant.EMPTY
        lastName = userEntity.lastName?:Constant.EMPTY
        name = firstName.plus(" ").plus(lastName)
        email = userEntity.email?:Constant.EMPTY
        emailVerified = userEntity.emailVerified?:false
        profilePic = userEntity.profilePic?:Constant.EMPTY
        mobile = userEntity.mobile
        dialCode = userEntity.dialCode
        isStripeConnected = userEntity.metadata.isStripeConnected
        isStripeOnboardConnected = userEntity.metadata.isStripeOnboardConnected
    }

    fun mapFrom(list: List<UserEntity>) = list.map { mapFrom(it) }

    fun mapFrom(userDetailResponse: UserDetailResponse):User{
        val userEntity = userDetailResponse.response.user
        val user = mapFrom(userEntity)
        user.authKeys = mapFrom(userDetailResponse.response.user.authKeyEntity)
        return user
    }

    fun mapFrom(authKeyEntity: AuthKeyEntity) = AuthKeys(authKeyEntity.authKey,authKeyEntity.refreshKey , authKeyEntity.customToken)
}