package tradly.social.event.eventbooking.mybooking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import tradly.social.event.eventbooking.R
import tradly.social.common.base.BaseFragment

class MyBookingFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_booking, container, false)
    }
}