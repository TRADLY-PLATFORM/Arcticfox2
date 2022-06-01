package tradly.social.event.addevent.ui.variant.addVariant

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import tradly.social.common.base.getViewModel
import tradly.social.common.base.observeEvent
import tradly.social.common.base.getIntentExtra
import tradly.social.common.navigation.Activities
import tradly.social.common.resources.CommonResourceDrawable
import tradly.social.common.resources.CommonResourceString
import tradly.social.common.util.parser.extension.toList
import tradly.social.common.util.parser.extension.toObject
import tradly.social.domain.entities.Currency
import tradly.social.domain.entities.Variant
import tradly.social.domain.entities.VariantType
import tradly.social.event.addevent.R
import tradly.social.event.addevent.databinding.ActivityVariantHostBinding
import tradly.social.event.addevent.ui.variant.viewmodel.SharedVariantViewModel
import tradly.social.common.base.BaseActivity

class VariantHostActivity : BaseActivity() {

    lateinit var activityVariantHostBinding: ActivityVariantHostBinding
    lateinit var sharedVariantViewModel: SharedVariantViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityVariantHostBinding = DataBindingUtil.setContentView(this,R.layout.activity_variant_host)
        setToolbar(activityVariantHostBinding.toolbar.toolbar,title = CommonResourceString.add_variant_variant_details,backNavIcon = CommonResourceDrawable.ic_back)
        sharedVariantViewModel = getViewModel { SharedVariantViewModel() }
        setDataFromIntent()
        onFragmentNavigation(AddVariantDetailFragment.TAG)
        observeLiveData()
    }

    private fun setDataFromIntent(){
        sharedVariantViewModel.setSelectedCurrency(getIntentExtra<String>(Activities.VariantHostActivity.EXTRAS_CURRENCY).toObject<Currency>()!!)
        sharedVariantViewModel.setVariantList(intent.getStringExtra(Activities.VariantHostActivity.EXTRAS_VARIANT_LIST).toList<Variant>())
        val variant = intent.getStringExtra(Activities.VariantHostActivity.EXTRAS_VARIANT).toObject<Variant>()
        sharedVariantViewModel.setSelectedVariant(variant)
        if (variant!=null){
            sharedVariantViewModel.setSelectedVariantValueList(variant.values)
        }
    }

    private fun launchFragment(fragment:Fragment,addToBackStack:Boolean,tag:String){
        with(supportFragmentManager.beginTransaction()){
            add(activityVariantHostBinding.fragmentContainer.id,fragment,tag)
            if (addToBackStack){
                addToBackStack(tag)
            }
            commit()
        }
    }

    private fun observeLiveData(){
        observeEvent(sharedVariantViewModel.fragmentNavigationLiveData,this::onFragmentNavigation)
        observeEvent(sharedVariantViewModel.fragmentPopupLiveData,{supportFragmentManager.popBackStack()})
        observeEvent(sharedVariantViewModel.onFinishLiveData,this::onFinish)
    }

    private fun onFragmentNavigation(tag:String){
        when(tag){
            AddVariantDetailFragment.TAG ->{
                val variantTypeList = intent.getStringExtra(Activities.VariantHostActivity.EXTRAS_VARIANT_TYPE_LIST).toList<VariantType>()
                launchFragment(
                    AddVariantDetailFragment.newInstance(variantTypeList),false,
                    AddVariantDetailFragment.TAG
                )
            }
            AddVariantFragment.TAG -> launchFragment(
                AddVariantFragment(),true,
                AddVariantFragment.TAG
            )
        }
    }

    private fun onFinish(bundle: Bundle){
        val intent = Intent()
        intent.putExtras(bundle)
        setResult(Activity.RESULT_OK,intent)
        finish()
    }

}