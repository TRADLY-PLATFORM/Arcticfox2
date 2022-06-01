package tradly.social.ui.store.followers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import tradly.social.adapter.AccountFollowersAdapter
import tradly.social.common.base.*
import tradly.social.common.base.getPrimitiveArgumentData
import tradly.social.common.network.RequestID
import tradly.social.common.base.setLoadMoreListener
import tradly.social.common.base.setVisible
import tradly.social.databinding.FragmentAccountFollowersBinding
import tradly.social.domain.entities.User
import tradly.social.ui.store.AccountViewModel

class AccountFollowersFragment : BaseFragment() {

    private lateinit var accountViewmodel:AccountViewModel
    private lateinit var adapter:AccountFollowersAdapter
    private var _binding: FragmentAccountFollowersBinding? = null
    private val binding get() = _binding!!
    private lateinit var accountId: String

    companion object{
       private const val ARG_ACCOUNT_ID = "accountId"

        fun newInstance(accountId:String) = AccountFollowersFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ACCOUNT_ID,accountId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountViewmodel = getViewModel { AccountViewModel() }
        accountId = getPrimitiveArgumentData(ARG_ACCOUNT_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountFollowersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        observeLiveData()
        accountViewmodel.getFollowers(accountId,false)
    }

    private fun initAdapter(){
        adapter = AccountFollowersAdapter()
        binding.rvFollowerList.apply {
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            this.adapter = this@AccountFollowersFragment.adapter
        }
        binding.rvFollowerList.setLoadMoreListener(object :OnScrollMoreListener(){
            override fun onLoadMore() {
                if (NetworkUtil.isConnectingToInternet() && !accountViewmodel.isLoading && !accountViewmodel.isPaginationEnd){
                    accountViewmodel.getFollowers(accountId,true)
                }
            }
        })
    }

    private fun observeLiveData(){
        viewLifecycleOwner.apply {
            accountViewmodel.accountFollowersLiveData.observe(this,this@AccountFollowersFragment::showFollowers)
            observeEvent(accountViewmodel.uiState,this@AccountFollowersFragment::handleUIState)
        }
    }

    private fun showFollowers(list: List<User>){
        adapter.updateList(list)
        binding.tvEmptyState.setVisible(adapter.items.isEmpty())
        accountViewmodel.isLoading = false
    }

    private fun handleUIState(uiState: UIState<Unit>){
        when(uiState){
            is UIState.Loading->{
                when(uiState.apiId){
                    RequestID.GET_ACCOUNT_FOLLOWERS-> binding.progressBar.setVisible(uiState.isLoading)
                }
            }
            is UIState.Failure->ErrorHandler.handleError(uiState.errorData)
        }
    }
}