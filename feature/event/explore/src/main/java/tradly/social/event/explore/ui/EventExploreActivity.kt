package tradly.social.event.explore.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import tradly.social.common.base.*
import tradly.social.common.filter.FilterListFragment
import tradly.social.common.filter.FilterMultiSelectFragment
import tradly.social.common.resources.CommonResourceAnim
import tradly.social.event.explore.R
import tradly.social.event.explore.databinding.EventExploreActivityBinding
import tradly.social.event.explore.ui.filter.*
import tradly.social.event.explore.ui.filter.viewmodel.EventExploreViewModel
import tradly.social.common.filter.SharedFilterViewModel

class EventExploreActivity : BaseActivity(),CustomOnClickListener.OnCustomClickListener {

    private lateinit var eventExploreActivityBinding: EventExploreActivityBinding
    private lateinit var eventExploreViewModel: EventExploreViewModel
    private lateinit var sharedFilterViewModel: SharedFilterViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventExploreActivityBinding = DataBindingUtil.setContentView(this,R.layout.activity_event_explore)
        eventExploreActivityBinding.lifecycleOwner = this
        eventExploreActivityBinding.onClickListener = CustomOnClickListener(this)
        eventExploreViewModel = getViewModel { EventExploreViewModel() }
        sharedFilterViewModel = getViewModel { SharedFilterViewModel() }
        initFragment()
        setListener()
        observerLiveData()
    }


    private fun observerLiveData(){
        observeEvent(eventExploreViewModel.fragmentNavigation,this::onFragmentNavigation)
        observeEvent(sharedFilterViewModel.fragmentNavigation,this::onFragmentNavigation)
        observeEvent(sharedFilterViewModel.fragmentPopup,{ supportFragmentManager.popBackStack()})
        observeEvent(eventExploreViewModel.fragmentPopup,{supportFragmentManager.popBackStack()})
    }

    private fun initFragment(){
        supportFragmentManager.beginTransaction().let {
            val fragment = EventExploreFragment()
            it.add(eventExploreActivityBinding.fragmentContainer.id,fragment,EventExploreFragment.TAG)
            it.setPrimaryNavigationFragment(fragment)
            it.commitNow()
        }
        setToolbarView(EventExploreFragment.TAG)
    }

    private fun onFragmentNavigation(tag:String){
        when(tag){
            EventListFragment.TAG-> launchFragment(EventListFragment(),EventListFragment.TAG,true)
            FilterListFragment.TAG -> launchFragment(FilterListFragment.newInstance(AppConstant.FilterType.EVENTS), FilterListFragment.TAG,true)
            FilterMultiSelectFragment.TAG -> launchFragment(FilterMultiSelectFragment.newInstance(AppConstant.FilterType.EVENTS), FilterMultiSelectFragment.TAG,true)
        }
    }

    private fun launchFragment(fragment: Fragment, tag:String, addToBackStack:Boolean){
        Log.d("TAG::",tag)
        with(supportFragmentManager.beginTransaction()){
            if (addToBackStack){
                setCustomAnimations(CommonResourceAnim.slide_in, CommonResourceAnim.fade_out, CommonResourceAnim.fade_in,CommonResourceAnim.slide_out)
                add(eventExploreActivityBinding.fragmentContainer.id,fragment,tag)
                addToBackStack(tag)
            }
            else{
                add(eventExploreActivityBinding.fragmentContainer.id,fragment,tag)
            }
            this.setPrimaryNavigationFragment(fragment)
            commit()
        }
    }

    private fun setListener(){
        supportFragmentManager.addOnBackStackChangedListener { onFragmentBackStack() }
    }

    private fun onFragmentBackStack(){
        val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (fragment != null && fragment.isVisible && !fragment.tag.isNullOrEmpty()) {
            setToolbarView(fragment.tag!!)
        }
    }

    private fun setToolbarView(tag:String){
        when(tag){
            EventExploreFragment.TAG->{
            }
            EventListFragment.TAG->{
                     }
            else->{
                     }
        }
    }

    override fun onCustomClick(view: View) {

    }
}