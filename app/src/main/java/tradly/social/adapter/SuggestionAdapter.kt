package tradly.social.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import tradly.social.R
import tradly.social.domain.entities.Tag

class SuggestionAdapter(internal var context: Context, var list: List<Tag> ,val clickItem:(position:Int)->Unit) :
    RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder>(){
    private val inflater: LayoutInflater
    init {
        inflater = LayoutInflater.from(context)
    }

    class SuggestionViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var txtOne: TextView? = null
        init {
            txtOne = item.findViewById(R.id.txtName)
        }
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val tag = list[position]
        holder.txtOne?.text = String.format(context.getString(R.string.hast_tag),tag.name)
        holder.itemView.setOnClickListener {
            clickItem(holder.adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = inflater.inflate(R.layout.list_item_suggestion, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun getItemCount(): Int {
       return list.count()
    }
}