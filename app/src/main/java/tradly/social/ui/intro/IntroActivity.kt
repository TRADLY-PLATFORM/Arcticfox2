package tradly.social.ui.intro

import android.content.Intent
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import tradly.social.R
import tradly.social.adapter.IntroPagerAdapter
import tradly.social.common.base.AppConstant
import tradly.social.common.base.BaseActivity
import tradly.social.ui.main.MainActivity
import kotlinx.android.synthetic.main.activity_intro.*
import tradly.social.common.base.AppConfigHelper
import tradly.social.domain.entities.Intro

class IntroActivity : BaseActivity() {
    private lateinit var introList:List<Intro>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        introList = AppConfigHelper.getConfigKey(AppConfigHelper.Keys.INTRO_SCREENS)
        if(introList.isEmpty()){
            launchHome()      // safety check. Will remove it later. once properly Tested.
        }
        setupViewPager(pager)
        dots.attachViewPager(pager)
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }
            override fun onPageSelected(position: Int) {
                if(position == introList.size-1){
                    txtAction.text = getString(R.string.intro_get_started)
                }else{
                    txtAction.text = getString(R.string.intro_next)
                }
            }
        })
        btnAction.setOnClickListener {
            if(pager.currentItem == introList.size-1){
                launchHome()
            }else{
                pager.currentItem = pager.currentItem + 1;
            }
        }
    }

    private fun launchHome(){
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
        tradly.social.common.persistance.shared.PreferenceSecurity.putBoolean(AppConstant.PREF_KEY_INTRO,true)
        finish()
    }

    private fun setupViewPager(pager: ViewPager?) {
        val adapter = IntroPagerAdapter(supportFragmentManager)
        for (item in introList){
            adapter.addFragment(IntroFragment.newInstance(item.text,item.image))
        }
        pager?.adapter = adapter
    }
}
