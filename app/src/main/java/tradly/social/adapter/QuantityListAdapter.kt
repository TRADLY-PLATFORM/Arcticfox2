package tradly.social.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import tradly.social.R
class QuantityListAdapter (internal var cList: List<String>, internal var context: Context) :
    ArrayAdapter<String>(context, 0, cList) {

    class ViewHolder {
        internal var qtyValue: TextView? = null

    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    fun getCustomView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val viewHolder: ViewHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_qty, parent, false)
            viewHolder = ViewHolder()
            viewHolder.qtyValue = convertView!!.findViewById(R.id.txtValue)
            convertView.tag = viewHolder

        } else {
            viewHolder = convertView.tag as ViewHolder

        }
        if(cList.count()-1==position){
            viewHolder.qtyValue?.visibility = View.GONE
            viewHolder.qtyValue?.height = 0
        }
        viewHolder.qtyValue?.text = cList[position]
        return convertView
    }

    override fun getItem(position: Int): String? {
        return cList[position]
    }

    override fun getCount(): Int {
        return cList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}