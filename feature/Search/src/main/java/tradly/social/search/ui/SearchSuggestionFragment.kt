package tradly.social.search.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import tradly.social.common.base.AppConstant
import tradly.social.common.base.getViewModel
import tradly.social.common.resources.CommonResourceString
import tradly.social.search.R
import tradly.social.search.databinding.FragmentSearchSuggestionBinding

class SearchSuggestionFragment : Fragment() {

    private lateinit var sharedSearchViewModel: SharedSearchViewModel
    private lateinit var binding:FragmentSearchSuggestionBinding
    companion object{
        const val TAG =  "SearchSuggestionFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedSearchViewModel = requireActivity().getViewModel { SharedSearchViewModel() }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchSuggestionBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeLiveData()
    }

    private fun observeLiveData(){
        sharedSearchViewModel.searchKeyword.observe(viewLifecycleOwner,{
            sharedSearchViewModel.setFragmentNavigation(SearchFragment.TAG)
        })
        sharedSearchViewModel.searchByType.observe(viewLifecycleOwner,{
            when(it){
                AppConstant.FilterType.ACCOUNTS-> binding.tvSearchHint.text = getString(CommonResourceString.search_search_a_store)
                AppConstant.FilterType.PRODUCTS-> binding.tvSearchHint.text = getString(CommonResourceString.search_search_a_product)
            }
        })
    }

}