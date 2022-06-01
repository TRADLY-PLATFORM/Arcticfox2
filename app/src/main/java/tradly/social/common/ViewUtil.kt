package tradly.social.common
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.layout_reviews.view.*
import tradly.social.R
import tradly.social.adapter.ListingAdapter
import tradly.social.common.base.AppConstant
import tradly.social.common.base.AppController
import tradly.social.common.util.parser.extension.toJson
import tradly.social.domain.entities.OrderedProductEntity
import tradly.social.domain.entities.Rating
import tradly.social.domain.entities.Store
import tradly.social.ui.groupListing.ListingActivity
import tradly.social.common.base.Utils
import tradly.social.common.base.startActivity


class ViewUtil {
    companion object{
        fun showReviewInfo(isForStoreReview:Boolean,view:View,rating: Rating , onClick:(any:Any)->Unit){
            with(view){
                var mList = rating.reviewList
                if(mList.isNotEmpty()){
                    if(mList.size>5){
                        mList = mList.subList(0,3)
                        this.readAllReviewsBtn?.visibility = View.VISIBLE
                    }
                    else{
                        readAllReviewsBtn?.visibility = View.GONE
                    }
                    this.findViewById<ConstraintLayout>(R.id.reviewLayout)?.visibility = View.VISIBLE
                    reviewList?.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL,false)
                    val reviewListAdapter = ListingAdapter(this.context,mList,
                        AppConstant.ListingType.REVIEW_LIST,null){ position, obj ->
                        onClick(obj)
                    }
                    reviewList?.adapter = reviewListAdapter
                    showRatingProgressView(this,rating)
                }
                else{
                    if(isForStoreReview){
                        this.findViewById<ConstraintLayout>(R.id.reviewLayout)?.visibility = View.VISIBLE
                        if(rating.myReview!=null){
                            ratingCountLayout?.visibility = View.VISIBLE
                            reviewList?.visibility = View.GONE
                            reviewsTxtCount?.visibility = View.GONE
                            showRatingProgressView(this,rating)
                        }
                        else{
                            ratingCountLayout?.visibility = View.GONE
                        }
                    }
                    else{
                        ratingCountLayout?.visibility = View.GONE
                    }
                }
                storeRatingLayout?.visibility = if(isForStoreReview && AppController.appController.getUser()!=null) View.VISIBLE else View.GONE
            }
        }

        private fun showRatingProgressView(view: View,rating: Rating){
            with(view){
                ratingTxt?.text = rating.ratingAverage.toString()
                ratingCountTxt?.text = String.format(this.context.getString(R.string.review_rating_count),rating.ratingCount)
                reviewsTxtCount?.text =String.format(this.context.getString(R.string.review_reviews_count),rating.reviewCount)
                progressValue1?.progress = Utils.getRatingPercent(rating.ratingDataCount.rating1,rating.ratingCount)
                progressValue2?.progress = Utils.getRatingPercent(rating.ratingDataCount.rating2,rating.ratingCount)
                progressValue3?.progress = Utils.getRatingPercent(rating.ratingDataCount.rating3,rating.ratingCount)
                progressValue4?.progress = Utils.getRatingPercent(rating.ratingDataCount.rating4,rating.ratingCount)
                progressValue5?.progress = Utils.getRatingPercent(rating.ratingDataCount.rating5,rating.ratingCount)
                ratingCountTxt1?.text = rating.ratingDataCount.rating1.toString()
                ratingCountTxt2?.text = rating.ratingDataCount.rating2.toString()
                ratingCountTxt3?.text = rating.ratingDataCount.rating3.toString()
                ratingCountTxt4?.text = rating.ratingDataCount.rating4.toString()
                ratingCountTxt5?.text = rating.ratingDataCount.rating5.toString()
            }
        }

        fun showReviewList(ctx:Context,id:String,type:String){
            val bundle = Bundle().apply {
                putString(AppConstant.BundleProperty.TYPE,type)
                putString(AppConstant.BundleProperty.ID,id)
                putInt(AppConstant.BundleProperty.ISFOR, AppConstant.ListingType.REVIEW_LIST)
                putInt(AppConstant.BundleProperty.TOOLBAR_TITLE,R.string.product_all_reviews)
            }
            ctx.startActivity(ListingActivity::class.java,bundle)
        }

        fun showReviewSheet(type:String,any:Any,callback:(bundle:Bundle)->Unit ):BottomChooserDialog{
            val bottomChooserDialog = BottomChooserDialog()
            val arg = Bundle()
            arg.putString(AppConstant.BundleProperty.TYPE,type)
            if(type == AppConstant.ModuleType.LISTINGS){
                arg.putString(AppConstant.BundleProperty.PRODUCT_DETAIL,(any as OrderedProductEntity).toJson<OrderedProductEntity>())
            }
            else{
                arg.putString(AppConstant.BundleProperty.STORE,(any as Store).toJson<Store>())
            }
            bottomChooserDialog.arguments = arg
            bottomChooserDialog.setDialogType(BottomChooserDialog.DialogType.ADD_REVIEW)
            bottomChooserDialog.setListener(object :DialogListener{
                override fun onClick(any: Any) {
                    callback(any as Bundle)

                }
            })
            return bottomChooserDialog
        }
    }
}