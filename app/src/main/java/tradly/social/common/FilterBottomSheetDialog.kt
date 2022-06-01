package tradly.social.common

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import tradly.social.R
import tradly.social.domain.entities.Constant
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.layout_filter_bottom_sheet.*
import kotlinx.android.synthetic.main.layout_filter_bottom_sheet.view.*
import tradly.social.common.base.AppConstant
import tradly.social.common.base.safeClickListener
import java.text.DecimalFormat
import kotlin.math.round

class FilterBottomSheetDialog : BottomSheetDialogFragment(), View.OnClickListener {

    private var listener: DialogListener? = null
    private var mView:View?=null
    private var dialogType:Int = DialogType.FILTER_BY_PRICE
    private var decimalFormat:DecimalFormat = DecimalFormat("#")

    object DialogType{
        const val FILTER_BY_PRICE = 1
        const val FILTER_BY_DISTANCE = 2
        const val SEARCH_BY = 3
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.layout_filter_bottom_sheet, container, false)
        mView?.filterOneView?.setOnClickListener(this)
        mView?.filterTwoView?.setOnClickListener(this)
        mView?.filterThreeView?.setOnClickListener(this)
        arguments?.let {
            if(dialogType==DialogType.FILTER_BY_DISTANCE){
                filterByDistance?.visibility = View.VISIBLE
                mView?.title?.setText(R.string.search_filter_by_distace)
                mView?.filterByDistance?.visibility = View.VISIBLE
                initSlider(it.getString(
                    AppConstant.BundleProperty.FILTER_BY_DISTANCE_KEY,
                    AppConstant.EMPTY))
                mView?.btnDone?.safeClickListener {
                    listener?.onClick(decimalFormat.format(round(slider.value)).toString())
                    dismiss()
                }
            }
            else if (dialogType == DialogType.SEARCH_BY){
                mView?.title?.setText(R.string.search_search_by)
                mView?.sortByFilter?.visibility = View.VISIBLE
            }
            else{
                mView?.title?.setText(R.string.search_sort_by)
                mView?.sortByFilter?.visibility = View.VISIBLE
                switchRadioButton(arguments?.getString("sort",Constant.EMPTY))
            }
        }
        return mView
    }

    @SuppressLint("SetTextI18n")
    private fun initSlider(sliderValue:String = Constant.EMPTY){
        if(sliderValue.isNotEmpty()){
            mView?.slider?.value =sliderValue.toFloat()
            mView?.maxDistanceTxt?.text = "${sliderValue} KMS"
        }
        mView?.slider?.addOnChangeListener { slider, value, fromUser ->
            maxDistanceTxt?.text = "${decimalFormat.format(round(value))} KMS"
        }
    }

    override fun onClick(p0: View?) {
        p0?.let {
            when (p0.id) {
                R.id.filterOneView -> {
                    switchRadioButton(R.id.radioFilterOne)
                    if (dialogType == DialogType.FILTER_BY_PRICE){
                        listener?.onClick(AppConstant.SORT.PRICE_LOW_HIGH)
                    }
                    else{
                        listener?.onClick(AppConstant.ModuleType.LISTINGS)
                    }
                }
                R.id.filterTwoView -> {
                    switchRadioButton(R.id.radioFilterTwo)
                    if (dialogType == DialogType.FILTER_BY_PRICE){
                        listener?.onClick(AppConstant.SORT.PRICE_HIGH_LOW)
                    }
                    else{
                        listener?.onClick(AppConstant.ModuleType.ACCOUNTS)
                    }
                }
                R.id.filterThreeView -> {
                    switchRadioButton(R.id.radioFilterThree)
                    listener?.onClick(AppConstant.SORT.RECENT)
                }
            }
            return@let
        }
        dialog?.dismiss()
    }

    private fun switchRadioButton(id: Int) {
        radioFilterOne.isChecked = false
        radioFilterTwo.isChecked = false
        radioFilterThree.isChecked = false
        when (id) {
            R.id.radioFilterOne->{
                radioFilterOne.isChecked = true
            }
            R.id.radioFilterTwo->{
                radioFilterTwo.isChecked = true
            }
            R.id.radioFilterThree->{
                radioFilterThree.isChecked = true
            }
        }
    }



    private fun switchRadioButton(sort: String?) {
        mView?.radioFilterOne?.isChecked = false
        mView?.radioFilterTwo?.isChecked = false
        mView?.radioFilterThree?.isChecked = false
        when (sort) {
            AppConstant.SORT.PRICE_LOW_HIGH->{
                mView?.radioFilterOne?.isChecked = true
            }
            AppConstant.SORT.PRICE_HIGH_LOW->{
                mView?.radioFilterTwo?.isChecked = true
            }
            AppConstant.SORT.RECENT->{
                mView?.radioFilterThree?.isChecked = true
            }
        }
    }

    fun setDialogType(type:Int){
        dialogType = type
    }

   fun setListener(listener: DialogListener){
       this.listener = listener
   }
}