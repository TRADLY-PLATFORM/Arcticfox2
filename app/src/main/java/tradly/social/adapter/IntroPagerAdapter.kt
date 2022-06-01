package tradly.social.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class IntroPagerAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragments = ArrayList<Fragment>()
    private val titles = ArrayList<String>()

    override fun getItem(position: Int): Fragment = fragments[position]

    override fun getCount(): Int = fragments.size

    override fun getPageTitle(position: Int): CharSequence? = titles[position]

    fun addFragment(fragment: Fragment) {
        fragments.add(fragment)
    }
}
