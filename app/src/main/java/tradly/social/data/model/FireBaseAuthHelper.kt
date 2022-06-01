package tradly.social.data.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import tradly.social.common.base.ErrorHandler
import tradly.social.common.network.CustomError
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Result
import java.lang.Exception

object FireBaseAuthHelper {

    private val firebaseAuthInstance: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun signInWithCustomToken(customToken: String): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuthInstance.signInWithCustomToken(customToken).await()
            authResult.user?.let {
                Result.Success(it)
            } ?: run {
                Result.Error(AppError(code = CustomError.FIREBASE_AUTH_FAILED))
            }
        } catch (ex: Exception) {
            Result.Error(AppError(ex.message,CustomError.FIREBASE_AUTH_FAILED))
        }
    }

    fun signOut() = firebaseAuthInstance.signOut()
}