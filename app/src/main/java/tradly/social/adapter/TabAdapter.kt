package tradly.social.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class TabAdapter(internal var context:Context,supportFragmentManager: FragmentManager):FragmentStatePagerAdapter(supportFragmentManager) {
    private val mFragmentList = ArrayList<Fragment>()
    private val mFragmentTitleList = ArrayList<Int>()

    override fun getItem(position: Int): Fragment {
        return mFragmentList.get(position)
    }

    override fun getCount(): Int {
        return mFragmentList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(mFragmentTitleList[position])
    }

    fun addFragment(fragment: Fragment, title: Int) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
    }
}