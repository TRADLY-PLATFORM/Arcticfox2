package tradly.social.ui.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import tradly.social.R
import tradly.social.common.base.AppConstant
import tradly.social.common.FragmentListener
import tradly.social.common.base.BaseActivity
import tradly.social.ui.forgotPassword.ForgotPasswordFragmentOne

class AuthenticationActivity : BaseActivity(), FragmentListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        navigateFragment(LoginFragment(), AppConstant.FragmentTag.LOGIN_FRAGMENT,false)
    }

    override fun callNextFragment(any: Any?, tag: String?) {
        when(tag){
            AppConstant.FragmentTag.LOGIN_FRAGMENT->navigateFragment(LoginFragment(),tag)
            AppConstant.FragmentTag.SIGNUP_FRAGMENT-> navigateFragment(SignUpFragment(),tag)
            AppConstant.FragmentTag.VERIFICATION_FRAGMENT->{
                val fragment = VerificationFragment()
                fragment.arguments = any as? Bundle
                navigateFragment(fragment,tag)
            }
            AppConstant.FragmentTag.FORGOT_PASSWORD_FRAGMENT_ONE,
            AppConstant.FragmentTag.FORGOT_PASSWORD_FRAGMENT_TWO->{
                val fragment = ForgotPasswordFragmentOne()
                fragment.arguments = any as? Bundle
                navigateFragment(fragment,tag)
            }
        }
    }

    override fun popFragment(tag: String) {
        supportFragmentManager.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    private fun navigateFragment(fragment: Fragment,tag:String? , shouldAddToStack:Boolean = true){
        with(supportFragmentManager.beginTransaction()){
            this.replace(R.id.fragContainer,fragment,tag)
            if(shouldAddToStack){
                this.addToBackStack(tag)
            }
            commitTransaction(this)
        }
    }

}
