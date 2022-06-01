package tradly.social.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class TabViewAdapter(
    fragmentManager: FragmentManager,
    private val title: List<String>,
    private val fragments: List<Fragment>
) :
    FragmentPagerAdapter(
        fragmentManager,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {
    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        super.getPageTitle(position)
        return title[position]
    }

}