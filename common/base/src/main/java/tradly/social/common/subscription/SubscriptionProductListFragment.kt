package tradly.social.common.subscription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import tradly.social.common.adapter.SubscriptionAdapter
import tradly.social.common.base.*
import tradly.social.common.base.databinding.FragmentSubscriptionProductListBinding
import tradly.social.common.network.RequestID
import tradly.social.common.base.setVisible
import tradly.social.common.util.parser.extension.toJson
import tradly.social.domain.entities.SubscriptionProduct


class SubscriptionProductListFragment : BaseFragment() {

    lateinit var binding:FragmentSubscriptionProductListBinding
    lateinit var subscriptionViewModel: SubscriptionViewModel
    lateinit var adapter:SubscriptionAdapter
    private val navigationController by lazy { requireActivity().findNavController(R.id.fragmentContainer) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscriptionViewModel = getViewModel { SubscriptionViewModel() }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!::binding.isInitialized) {
            binding = FragmentSubscriptionProductListBinding.inflate(inflater, container, false)
            initAdapter()
            subscriptionViewModel.connectToBillingClient(requireContext())
            subscriptionViewModel.getPurchaseList()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeLiveData()
    }

    private fun initAdapter(){
        adapter = SubscriptionAdapter()
        adapter.onItemClickItemListener = onItemClickListener
        binding.rvProductList.addItemDecoration(SpaceGrid(2,Utils.getPixel(requireContext(),8),true))
        binding.rvProductList.layoutManager = GridLayoutManager(requireContext(),2,GridLayoutManager.VERTICAL,false)
        binding.rvProductList.adapter = adapter
    }

    private fun observeLiveData(){
        subscriptionViewModel.productList.observe(viewLifecycleOwner,{ productList->
            subscriptionViewModel.getSubscriptionProductList(productList)
        })
        subscriptionViewModel.skuDetailList.observe(viewLifecycleOwner,{
            onSubscribeProducts(subscriptionViewModel.productList.value!!)
        })
        observeEvent(subscriptionViewModel.onBillingClientConnectLiveData,{
            subscriptionViewModel.getProductList()
        })
        observeEvent(subscriptionViewModel.uiState,this::handleUIState)
    }

    private fun onSubscribeProducts(list: List<SubscriptionProduct>){
        adapter.items = list.toMutableList()
        binding.rvProductList.setVisible()
    }


    private val onItemClickListener = object :GenericAdapter.OnClickItemListListener<SubscriptionProduct>{
        override fun onClick(value: SubscriptionProduct, view: View) {
            when(view.id){
                R.id.tvSeeMore->{
                    val arg = Bundle().apply { putString(SubscriptionMoreBottomSheet.ARG_SUBSCRIPTION_PRODUCT,value.toJson<SubscriptionProduct>()) }
                    navigationController.navigate(R.id.subscription_product_list_info_sheet,arg)
                }
                R.id.clParentItem->{
                    if (subscriptionViewModel.productList.value!!.filter { it.subscriptionStatus }.isEmpty()){
                        subscriptionViewModel.launchBillingFlow(requireActivity(),value)
                    }
                    else{
                        Utils.showAlertDialog(requireContext(),title = null,message = AppConfigHelper.getConfigKey<String>(
                            AppConfigHelper.Keys.SUBSCRIPTION_ALREADY_PURCHASED_MESSAGE)
                            ,isCancellable = false,withCancel = false,positiveButtonName = R.string.okay,dialogInterface = object :
                                Utils.DialogInterface{
                                override fun onAccept() {
                                    Utils.launchPlayStore(requireContext(),requireActivity().packageName)
                                }

                                override fun onReject() {

                                }

                            })
                    }
                }
            }
        }
    }

    private fun handleUIState(uiState: UIState<Unit>){
        when(uiState){
           is UIState.Loading->{
               when(uiState.apiId){
                   RequestID.CONNECT_TO_BILLING_CLIENT,
                   RequestID.GET_SUBSCRIPTION_PRODUCTS->{
                       binding.progressBar.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE
                   }
                   RequestID.CONFIRM_SUBSCRIPTION->{
                       if(uiState.isLoading){
                           showLoader(R.string.please_wait)
                       }
                       else{
                           hideLoader()
                       }
                   }
               }
            }
            is UIState.Failure->{
                when(uiState.errorData.apiId){
                    RequestID.CONNECT_TO_BILLING_CLIENT->{
                        showBillingClientError()
                    }
                    else-> ErrorHandler.handleError(uiState.errorData)
                }
            }
        }
    }

    private fun showSuccessDialog(){
        Utils.showAlertDialog(requireContext(), AppConstant.EMPTY,getString(R.string.subscription_upgraded_successfully), false, false,R.string.subscription_ok,object :Utils.DialogInterface{
            override fun onAccept() {
                adapter.items.clear()
                adapter.notifyDataSetChanged()
                subscriptionViewModel.getProductList()
            }

            override fun onReject() {

            }
        })
    }


    private fun showBillingClientError(){
        Utils.showAlertDialog(requireContext(),getString(R.string.subscription_connection_error_title),getString(R.string.subscription_connection_error_desc),false,true,R.string.subscription_connection_try_again,object :Utils.DialogInterface{
            override fun onAccept() {
                subscriptionViewModel.connectToBillingClient(requireContext())
            }

            override fun onReject() {
                requireActivity().finish()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        subscriptionViewModel.billingClient.endConnection()
    }
}