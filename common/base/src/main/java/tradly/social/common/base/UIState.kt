package tradly.social.common.base

import tradly.social.domain.entities.AppError

sealed class UIState<T> {

    data class Success<T>(val data: T): UIState<T>()

    data class Failure<T>(val errorData: AppError): UIState<T>()

    data class ValidationFailure<T>(val error:Pair<Int,Int>):UIState<T>()

    data class Loading<T>(val isLoading: Boolean = false , val apiId:Int = 0): UIState<T>()
}