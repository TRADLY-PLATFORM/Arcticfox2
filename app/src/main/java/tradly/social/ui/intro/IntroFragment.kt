package tradly.social.ui.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.layout_onboarding.*
import tradly.social.R
import tradly.social.common.base.AppConstant
import tradly.social.common.base.ImageHelper

class IntroFragment : Fragment() {

    companion object{
        fun newInstance(title:String,image:String):IntroFragment{
            val fragment = IntroFragment()
            val args = Bundle()
            args.putString(AppConstant.BundleProperty.TITLE,title)
            args.putString(AppConstant.BundleProperty.IMAGE,image)
            return fragment.also { it.arguments = args }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        inflater.inflate(R.layout.layout_onboarding, container, false)!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtIntro?.text = arguments!!.getString(AppConstant.BundleProperty.TITLE)
        ImageHelper.getInstance().showImage(requireContext(),arguments!!.getString(AppConstant.BundleProperty.IMAGE),imgOnboard,R.drawable.placeholder_image,R.drawable.placeholder_image)
    }
}