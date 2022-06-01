package tradly.social.domain.usecases

import tradly.social.domain.entities.Result

abstract class BaseUseCase<out R : Any> {
    abstract suspend fun execute():Result<R>
}