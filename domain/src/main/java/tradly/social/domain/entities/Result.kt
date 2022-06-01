package tradly.social.domain.entities

sealed class Result<out T : Any> {
    data class Success<out T : Any>(val data: T) : Result<T>()
    data class Error(var exception: AppError) : Result<Nothing>()
}