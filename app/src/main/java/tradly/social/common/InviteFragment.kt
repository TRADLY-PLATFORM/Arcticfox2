package tradly.social.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.layout_invite_dialog.*
import kotlinx.android.synthetic.main.layout_invite_dialog.view.*
import tradly.social.R
import tradly.social.common.base.*


class InviteFragment : BaseDialogFragment(),View.OnClickListener {

    var mView:View?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, ThemeUtil.getSelectedTheme())
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.layout_invite_dialog, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.inviteLayout?.background = Utils.getDrawable(requireContext(), 0, R.color.white, R.color.white, 16f)
        view.inviteTradlyTxt?.text = String.format(requireContext().getString(R.string.invite_tradly),requireContext().getString(R.string.app_name))
        view.inviteTradlyDesc?.text = String.format(requireContext().getString(R.string.invite_your_friends_products),requireContext().getString(R.string.app_name))
        view.iconWhatApp?.setOnClickListener(this)
        view.iconFaceBook?.setOnClickListener(this)
        view.iconInstagram?.setOnClickListener(this)
        view.iconShare?.setOnClickListener(this)
        val imageUrl = AppConfigHelper.getConfigKey<String>(AppConfigHelper.Keys.INVITE_FRIENDS_DETAIL_IMAGE)
        view.invite_logo?.setImageByUrl(requireContext(),imageUrl,R.drawable.ic_home_invite,R.drawable.ic_home_invite)
        val colorPrimary = ThemeUtil.getResourceDrawable(requireContext(), R.attr.colorPrimary)
        parentLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), colorPrimary))
    }
    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
        }
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.iconFaceBook,
            R.id.iconInstagram,
            R.id.iconShare,
            R.id.iconWhatApp->{
                val appLink = BranchHelper.getAppSharePersistLink()
                if(view.id==R.id.iconShare && appLink.isNotEmpty()){
                    ShareUtil.showIntentChooser(requireContext(),appLink)
                }
                else{
                    showLoader(R.string.please_wait)
                    BranchHelper.getAppShareBranchLink(requireContext(),getChannel(view.id)){ url->
                        hideLoader()
                        when(view.id){
                            R.id.iconShare-> ShareUtil.showIntentChooser(requireContext(),url)
                            R.id.iconWhatApp-> ShareUtil.openThirdPartyApp(requireContext(),
                                ShareUtil.WHATSAPP_PCKG,url)
                            R.id.iconInstagram-> ShareUtil.openThirdPartyApp(requireContext(),
                                ShareUtil.INSTAGRAM_PCKG,url)
                            R.id.iconFaceBook->{
                               ShareUtil.openFacebookShareDialog(requireActivity(),url)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getChannel(id:Int) = when(id){
        R.id.iconFaceBook-> BranchHelper.Channel.FACEBOOK
        R.id.iconInstagram-> BranchHelper.Channel.INSTAGRAM
        R.id.iconShare-> BranchHelper.Channel.GENERAL_SHARE
        R.id.iconWhatApp-> BranchHelper.Channel.WHATS_APP_SHARE
        else -> BranchHelper.Channel.GENERAL_SHARE
    }
}