package tradly.social.common.subscription

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import tradly.social.common.base.BaseActivity
import tradly.social.common.base.R
import tradly.social.common.base.databinding.ActivityInAppSubscriptionBinding

class InAppSubscriptionActivity : BaseActivity() {

    private lateinit var binding:ActivityInAppSubscriptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInAppSubscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setToolbar(binding.toolbar.toolbar,R.string.subscription_title,R.drawable.ic_back)
    }
}