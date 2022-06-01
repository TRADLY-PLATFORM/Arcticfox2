package tradly.social.ui.socialFeed.latestItems


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import tradly.social.R
import tradly.social.common.base.BaseFragment

/**
 * A simple [Fragment] subclass.
 */
class LatestItemFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_latest_item, container, false)
    }


}
