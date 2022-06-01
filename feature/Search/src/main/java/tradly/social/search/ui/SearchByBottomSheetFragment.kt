package tradly.social.search.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import tradly.social.common.base.AppConstant
import tradly.social.common.base.CustomOnClickListener
import tradly.social.common.base.getViewModel
import tradly.social.common.filter.SharedFilterViewModel
import tradly.social.common.network.NetworkConstant
import tradly.social.search.BR
import tradly.social.search.R
import tradly.social.search.databinding.SearchSheetBottomSheetBinding
import tradly.social.common.base.BaseBottomSheetDialogFragment
import tradly.social.common.resources.CommonResourceString
import tradly.social.event.explore.common.CommonBaseResourceId

class SearchByBottomSheetFragment: BaseBottomSheetDialogFragment(),CustomOnClickListener.OnCustomClickListener {

    private lateinit var binding: SearchSheetBottomSheetBinding
    private lateinit var sharedFilterViewModel:SharedFilterViewModel
    private lateinit var sharedSearchViewModel: SharedSearchViewModel

    companion object{
        const val TAG = "SearchByBottomSheetFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedFilterViewModel = requireActivity().getViewModel { SharedFilterViewModel() }
        sharedSearchViewModel = requireActivity().getViewModel { SharedSearchViewModel() }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_search_by,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setVariable(BR.onClickListener,CustomOnClickListener(this))
        binding.setVariable(BR.title,getString(CommonResourceString.search_search_by))
        observeLiveData()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            (view?.parent as ViewGroup).background = ColorDrawable(Color.TRANSPARENT)
        }
        return dialog
    }

    private fun observeLiveData(){
        sharedSearchViewModel.searchByType.observe(viewLifecycleOwner,{
            when(it){
                AppConstant.FilterType.PRODUCTS-> binding.radioBtnProducts.isChecked = true
                AppConstant.FilterType.ACCOUNTS -> binding.radioBtnStore.isChecked = true
            }
        })
    }

    override fun onCustomClick(view: View) {
        when(view.id){
            CommonBaseResourceId.ivBack ->{
                dismiss()
            }
            R.id.clProducts->{
                binding.radioBtnStore.isChecked = false
                binding.radioBtnProducts.isChecked = true
            }
            R.id.clStore->{
                binding.radioBtnStore.isChecked = true
                binding.radioBtnProducts.isChecked = false
            }
            R.id.btnDone->{
                if (binding.radioBtnProducts.isChecked){
                    sharedFilterViewModel.setFinalQuery(NetworkConstant.QueryParam.TYPE,NetworkConstant.Param.LISTINGS)
                    sharedSearchViewModel.setSearchByType(AppConstant.FilterType.PRODUCTS)
                    sharedFilterViewModel.setFilterApply()
                    dismiss()
                }
                else if(binding.radioBtnStore.isChecked ){
                    sharedFilterViewModel.setFinalQuery(NetworkConstant.QueryParam.TYPE,NetworkConstant.Param.ACCOUNT)
                    sharedSearchViewModel.setSearchByType(AppConstant.FilterType.ACCOUNTS)
                    sharedFilterViewModel.setFilterApply()
                    dismiss()
                }
            }
        }
    }
}