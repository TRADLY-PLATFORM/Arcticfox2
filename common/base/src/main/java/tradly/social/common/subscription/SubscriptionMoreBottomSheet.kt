package tradly.social.common.subscription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import tradly.social.common.base.BR
import tradly.social.common.base.BaseBottomSheetDialogFragment
import tradly.social.common.base.databinding.SubscriptionMoreBinding
import tradly.social.common.cache.CurrencyCache
import tradly.social.common.base.getPrimitiveArgumentData
import tradly.social.common.util.parser.extension.toJson
import tradly.social.common.util.parser.extension.toObject
import tradly.social.domain.entities.SubscriptionProduct

class SubscriptionMoreBottomSheet:BaseBottomSheetDialogFragment() {

    private lateinit var binding:SubscriptionMoreBinding

    companion object{
        const val ARG_SUBSCRIPTION_PRODUCT = "subscription_product"

        fun newInstance(subscriptionProduct:SubscriptionProduct) = SubscriptionMoreBottomSheet().apply {
            arguments = Bundle().apply {
                putString(ARG_SUBSCRIPTION_PRODUCT,subscriptionProduct.toJson<SubscriptionProduct>())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SubscriptionMoreBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setVariable(BR.subscriptionProduct,getPrimitiveArgumentData<String>(ARG_SUBSCRIPTION_PRODUCT).toObject<SubscriptionProduct>())
        binding.setVariable(BR.currency,CurrencyCache.getDefaultCurrency())
    }
}