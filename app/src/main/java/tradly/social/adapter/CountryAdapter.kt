package tradly.social.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import tradly.social.R
import tradly.social.common.base.AppConstant
import tradly.social.common.base.ImageHelper
import tradly.social.domain.entities.Country
import tradly.social.domain.entities.Locale
import tradly.social.common.uiEntity.CustomPair

class CountryAdapter(internal var cList: List<Any>, internal var context: Context , var isFor:Int) :
        ArrayAdapter<Any>(context, 0, cList) {

    class ViewHolder {
        internal var imgFlag: ImageView? = null
        internal var countryName: TextView? = null
    }

    class SingleLineViewHolder{
        var checkBox:CheckBox? = null
        var txtName:TextView? = null
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup) =
        when(isFor){
            AppConstant.ListingType.COUNTRY_LIST->getCustomView(position, convertView, parent)
            AppConstant.ListingType.LOCALE_LIST->getLocaleView(position, convertView, parent)
            AppConstant.ListingType.STATUS_BY_FILTER->getFilterView(position, convertView, parent)
            AppConstant.ListingType.SELLER_STATUS_LIST-> getSellerStatusView(position, convertView, parent)
            else -> getCustomView(position, convertView, parent)
        }


    private fun getLocaleView(position: Int, convertView: View?, parent: ViewGroup):View{
        var convertView = convertView
        var locale = cList[position] as Locale
        val viewHolder: SingleLineViewHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_single_select, parent, false)
            viewHolder = SingleLineViewHolder()
            viewHolder.checkBox = convertView!!.findViewById(R.id.checkbox)
            viewHolder.txtName = convertView.findViewById(R.id.txtViewName)
            convertView.tag = viewHolder

        }else {
            viewHolder = convertView.tag as SingleLineViewHolder
        }
        viewHolder.checkBox?.visibility = View.VISIBLE
        viewHolder.txtName?.text = locale.name
        viewHolder.checkBox?.isChecked = locale.default
        viewHolder.checkBox?.isClickable = false
        convertView.setOnClickListener {
            viewHolder.checkBox?.isChecked = true
            cList.forEach { (it as Locale).default = false}
            (cList[position] as Locale).default = true
            notifyDataSetChanged()
        }
        return convertView
    }

    @SuppressWarnings("unchecked")
    private fun getSellerStatusView(position: Int, convertView: View?, parent: ViewGroup):View{
        var convertView = convertView
        var item = cList[position] as CustomPair<Boolean, CustomPair<Int, Int>>
        val viewHolder: SingleLineViewHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_single_select, parent, false)
            viewHolder = SingleLineViewHolder()
            viewHolder.checkBox = convertView!!.findViewById(R.id.checkbox)
            viewHolder.txtName = convertView.findViewById(R.id.txtViewName)
            convertView.tag = viewHolder

        }else {
            viewHolder = convertView.tag as SingleLineViewHolder
        }
        viewHolder.checkBox?.visibility = View.VISIBLE
        viewHolder.txtName?.text = context.getString(item.second.second)
        viewHolder.checkBox?.isChecked = item.first
        viewHolder.checkBox?.isClickable = false
        convertView.setOnClickListener {
            viewHolder.checkBox?.isChecked = true
            cList.forEach { (it as CustomPair<Boolean, CustomPair<Int, Int>>).first = false}
            (cList[position] as CustomPair<Boolean, CustomPair<Int, Int>>).first = true
            notifyDataSetChanged()
        }
        return convertView
    }

    private fun getFilterView(position: Int, convertView: View?, parent: ViewGroup):View{
        var convertView = convertView
        val pair = cList[position] as Pair<Int,Int>
        val viewHolder: SingleLineViewHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_single_select, parent, false)
            viewHolder = SingleLineViewHolder()
            viewHolder.txtName = convertView!!.findViewById(R.id.txtViewName)
            convertView.tag = viewHolder

        }else {
            viewHolder = convertView.tag as SingleLineViewHolder
        }

        viewHolder.txtName?.text = context.getString(pair.first)
        return convertView
    }

    private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val country = cList[position] as Country
        val viewHolder: ViewHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_country, parent, false)
            viewHolder = ViewHolder()
            viewHolder.imgFlag = convertView!!.findViewById(R.id.countryFlag)
            viewHolder.countryName = convertView.findViewById(R.id.countryName)
            convertView.tag = viewHolder

        } else {
            viewHolder = convertView.tag as ViewHolder

        }
        viewHolder.countryName!!.text =
            String.format(context.getString(R.string.dial_code_country), country.dialCode, country.name)
        ImageHelper.getInstance().showImage(context,country.flag,viewHolder.imgFlag,R.drawable.placeholder_image,R.drawable.placeholder_image)
        return convertView
    }


    override fun getItem(position: Int): Any? {
        return cList[position]
    }

    override fun getCount(): Int {
        return cList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}