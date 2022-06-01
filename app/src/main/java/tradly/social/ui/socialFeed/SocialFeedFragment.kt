package tradly.social.ui.socialFeed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import tradly.social.R
import tradly.social.adapter.TabAdapter
import tradly.social.common.base.BaseFragment
import tradly.social.ui.socialFeed.latestItems.LatestItemFragment
import tradly.social.ui.socialFeed.mostFollowers.MostFollowers
import tradly.social.ui.socialFeed.trending.TrendingFragment
import kotlinx.android.synthetic.main.fragment_social_feed.*


class SocialFeedFragment : BaseFragment() {
    var tabAdapter:TabAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_social_feed, container, false)
        tabAdapter = TabAdapter(requireContext(),childFragmentManager)
        tabAdapter?.addFragment(TrendingFragment(),R.string.socialfeed_trending)
        tabAdapter?.addFragment(LatestItemFragment(),R.string.socialfeed_latest_item)
        tabAdapter?.addFragment(MostFollowers(),R.string.socialfeed_most_followers)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager?.adapter = tabAdapter
        tabLayout?.setupWithViewPager(viewPager)
    }
}
