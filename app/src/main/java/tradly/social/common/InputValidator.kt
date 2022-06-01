package tradly.social.common

import androidx.annotation.IntDef
import tradly.social.R
import tradly.social.common.base.Utils
import tradly.social.domain.entities.Country
import tradly.social.ui.base.FieldView

object InputValidator {

    const val EMAIL_PASSWORD = 1
    const val MOBILE_PASSWORD = 2
    const val MOBILE = 3
    const val EMAIL_RECOVER = 4
    const val MOBILE_RECOVER = 5

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(
        EMAIL_PASSWORD,
        MOBILE_PASSWORD,
        MOBILE,
        EMAIL_RECOVER,
        MOBILE_RECOVER
    )
    annotation class InputType



    fun validateEmail(email: String) = when {
        email.isEmpty() -> InputResult.Invalid(R.string.forgotpassword_required)
        !Utils.validateEmail(email) -> InputResult.Invalid(R.string.forgotpassword_invalid_email)
        else -> InputResult.Success
    }

    fun validateMobile(country: Country, mobile: String) = when{
        mobile.isEmpty()-> InputResult.Invalid(R.string.forgotpassword_required)
        country.mobileNumberLength != mobile.length ||
        !Utils.isValidNumberFormat(country.mobileNumberRegex,country.dialCode.plus(mobile))->InputResult.Invalid(R.string.invalid_no)
        else->InputResult.Success
    }

    fun validatePasswordReset(view: FieldView?, code:Pair<Int,String>, password:Pair<Int,String>, rePassword:Pair<Int,String>) = when{
        code.second.isEmpty()->{
            view?.inputError(code.first,R.string.forgotpassword_required)
            false
        }
        code.second.length !=6->{
            view?.inputError(code.first,R.string.otp_invalid_otp)
            false
        }
        password.second.isEmpty()->{
            view?.inputError(password.first,R.string.forgotpassword_required)
            false
        }
        rePassword.second.isEmpty()->{
            view?.inputError(rePassword.first,R.string.forgotpassword_required)
            false
        }
        password.second != rePassword.second -> {
            view?.inputError(rePassword.first,R.string.forgotpassword_password_mismatch)
            false
        }
        else -> true
    }
}