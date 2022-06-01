package tradly.social.ui.store.storeDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_store_about.*
import kotlinx.android.synthetic.main.layout_reviews.*
import kotlinx.android.synthetic.main.layout_reviews.view.*
import tradly.social.R
import tradly.social.adapter.ListingAdapter
import tradly.social.common.*
import tradly.social.common.base.*
import tradly.social.domain.entities.Attribute
import tradly.social.domain.entities.Rating
import tradly.social.domain.entities.Review
import tradly.social.domain.entities.Store
import tradly.social.common.base.BaseFragment

class StoreAboutFragment : BaseFragment(), CustomOnClickListener.OnCustomClickListener {

    lateinit var store:Store
    var mView:View?=null
    var storeLink:String = AppConstant.EMPTY
    var colorPrimary:Int = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView =  inflater.inflate(R.layout.fragment_store_about, container, false)
        colorPrimary = ThemeUtil.getResourceValue(requireContext(),R.attr.colorPrimary)
        mView?.findViewById<RecyclerView>(R.id.reviewList)?.isNestedScrollingEnabled = false
        mView?.readAllReviewsBtn?.setOnClickListener(CustomOnClickListener(this))
        mView?.txtWriteReview?.setOnClickListener(CustomOnClickListener(this))
        return mView
    }

    companion object {
        @JvmStatic
        fun newInstance(isFrom: Int) =
            StoreAboutFragment().apply {
                arguments = Bundle().apply {
                    putInt(AppConstant.BundleProperty.IS_FROM, isFrom)
                }
            }
    }

    override fun onCustomClick(view: View) {
        if(view.id == R.id.iconCopy){
            if(::store.isInitialized){
                ShareUtil.copyToClipBoard(requireContext(),storeLink)
            }
        }
        else if(view.id == R.id.readAllReviewsBtn){
            ViewUtil.showReviewList(requireContext(),store.id, AppConstant.ModuleType.ACCOUNTS)
        }
        else if(view.id == R.id.txtWriteReview){
            (activity as? StoreDetailActivity)?.showReviewSheet()
        }
    }

    fun setStoreDetail(store: Store){
        if(isAdded){
            val formattedAddress = store.address.formattedAddress
            val geoPoint = store.geoPoint
            val storeDesc = store.storeDescription
            if(showShowDetailView(store)){
                storeInfoCard?.visibility = View.VISIBLE
                txtEmptyStateMsg?.visibility = View.GONE
                if(formattedAddress.isNotEmpty()){
                    iconLocation?.visibility = View.VISIBLE
                    txtLocationAddress?.visibility = View.VISIBLE
                    txtLocationHint?.visibility = View.VISIBLE
                    txtLocationAddress?.setTextWithUnderline(formattedAddress)
                    if(geoPoint.latitude!=0.0){
                        txtLocationAddress?.setOnClickListener { Utils.showGoogleMap(geoPoint.latitude,geoPoint.longitude) }
                    }
                }
                if(storeDesc.isNotEmpty()){
                    txtHintDesc?.visibility = View.VISIBLE
                    txtDesc?.visibility = View.VISIBLE
                    txtDesc?.text = storeDesc
                }
                store.attributes.let {
                    if(it.isNotEmpty()){
                        storeAttribute(it)
                    }
                }

                if(shouldShowStoreLink()){
                    txtStoreLinkHint?.visibility = View.VISIBLE
                    iconLink?.visibility = View.VISIBLE
                    iconCopy?.visibility = View.VISIBLE
                    iconCopy?.setOnClickListener(CustomOnClickListener(this))
                    txtLink?.visibility = View.VISIBLE
                    txtLink?.text = storeLink
                }
            }
            else{
                storeInfoCard?.visibility = View.GONE
            }
            //setUserRatingDetails()
        }
    }

    private fun initStoreData(){
        storeLink =  AppConfigHelper.getTenantConfigKey<String>(AppConfigHelper.Keys.BRANCH_LINK_BASE_URL).plus(store.uniqueName).trim()
    }

    private fun showShowDetailView(store: Store):Boolean{
        this.store = store
        initStoreData()
        val formattedAddress = store.address.formattedAddress
        val storeDesc = store.storeDescription
        if(formattedAddress.isNotEmpty()
            || storeDesc.isNotEmpty()
            || store.attributes.isNotEmpty()
            || shouldShowStoreLink()){
            return true
        }
        return false
    }
    private fun storeAttribute(attributes:List<Attribute>){
        specRecyclerView?.visibility = View.VISIBLE
        txtSpecificationHint?.visibility = View.VISIBLE
        specRecyclerView?.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.VERTICAL,false)
        specRecyclerView?.isNestedScrollingEnabled = false
        val adapter = ListingAdapter(requireContext(),attributes,
            AppConstant.ListingType.SPECIFICATION,specRecyclerView){ position, obj ->  }
        specRecyclerView?.adapter = adapter
    }

    private fun shouldShowStoreLink() = AppConfigHelper.getTenantConfigKey<String>(AppConfigHelper.Keys.BRANCH_LINK_BASE_URL).isNotEmpty() && store.uniqueName.isNotEmpty()

    fun showStoreReviews(rating: Rating){
        if(isAdded){
            ViewUtil.showReviewInfo(true,reviewLayout,rating){ any->
                val review = any as Review
                (activity as? StoreDetailActivity)?.likeStoreReview(review.id.toString(),review.likeStatus)
            }
            setUserRatingDetails(rating)
        }
    }

    private fun setUserRatingDetails(rating: Rating){
        AppController.appController.getUser()?.let { user->
            if(user.id == store.user?.id){
                txtWriteReview?.visibility = View.GONE
                storeRatingLayout?.setGone()
                return@let
            }
        }
        if(rating.myReview!=null){
            txtWriteReview?.visibility = View.GONE
            showUserRating(rating.myReview!!.rating)
        }
    }

    private fun showUserRating(rating:Int){
        when(rating){
            0-> txtWriteReview?.visibility = View.VISIBLE
            1-> setFilledTint(iconStarOne)
            2->{
                setFilledTint(iconStarOne)
                setFilledTint(iconStarTwo)
            }
            3->{
                setFilledTint(iconStarOne)
                setFilledTint(iconStarTwo)
                setFilledTint(iconStarThree)
            }
            4->{
                setFilledTint(iconStarOne)
                setFilledTint(iconStarTwo)
                setFilledTint(iconStarThree)
                setFilledTint(iconStarFour)
            }
            5->{
                setFilledTint(iconStarOne)
                setFilledTint(iconStarTwo)
                setFilledTint(iconStarThree)
                setFilledTint(iconStarFour)
                setFilledTint(iconStarFive)
            }
        }
    }
    private fun setFilledTint(imageView: ImageView){
        imageView.setColorFilter(ContextCompat.getColor(requireContext(), colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN)
        imageView.setImageResource(R.drawable.ic_star_filled)
    }
}