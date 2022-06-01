package tradly.social.common

sealed class InputResult {
    object Success : InputResult()
    data class Invalid(var msg: Int) : InputResult()

}