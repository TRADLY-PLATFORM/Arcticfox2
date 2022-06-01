package tradly.social.adapter

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tradly.social.R
import tradly.social.domain.entities.Constant
import tradly.social.domain.entities.Value
import tradly.social.domain.entities.ProductAttribute
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.hootsuite.nachos.NachoTextView
import com.hootsuite.nachos.terminator.ChipTerminatorHandler
import tradly.social.common.*
import tradly.social.common.base.*
import tradly.social.common.views.custom.CustomTextView
import tradly.social.domain.entities.FileInfo

class AttributeListAdapter(var ctx:Context, var list:List<ProductAttribute>, var onClick:((isFor:Int, obj:Any)->Unit)?=null):RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var inflater:LayoutInflater = LayoutInflater.from(ctx)

    object ActionType {
        const val UPLOAD_FIELD = 1
    }

    private class SingleSelectHolder(item: View) : RecyclerView.ViewHolder(item) {
        var spinnerHint:TextView?=null
        var txtSingleSelectValue:TextView?=null
        var attrSpinner:Spinner?=null
        var singleSelectionLayout:RelativeLayout?=null
        init {
            singleSelectionLayout = item.findViewById(R.id.singleSelectionLayout)
            spinnerHint = item.findViewById(R.id.spinnerHint)
            txtSingleSelectValue = item.findViewById(R.id.txtSingleSelectValue)
            attrSpinner = item.findViewById(R.id.attrSpinner)
        }
    }

    private class MultiSelectHolder(item: View) : RecyclerView.ViewHolder(item) {
        var multiSelectTitle:TextView?=null
        var  llMultiSelect:LinearLayout?=null
        var chipGroupScrollView:ScrollView?=null
        var chipGroupMultiSelect:ChipGroup?=null
        var iconAddMultiSelect:FrameLayout?=null
        init {
            multiSelectTitle = item.findViewById(R.id.multiSelectTitle)
            llMultiSelect= item.findViewById(R.id.llMultiSelect)
            chipGroupScrollView = item.findViewById(R.id.chipGroupScrollView)
            chipGroupMultiSelect = item.findViewById(R.id.chipGroupMultiSelect)
            iconAddMultiSelect = item.findViewById(R.id.iconAddMultiSelect)
        }
    }

    private class UploadFileHolder(item: View):RecyclerView.ViewHolder(item){
        var docUploadHint: CustomTextView = item.findViewById(R.id.docUploadHint)
        var uploadFileRecyclerView:RecyclerView = item.findViewById(R.id.uploadFileRecyclerView)
        var uploadFileLayout:ConstraintLayout = item.findViewById(R.id.uploadFileLayout)
        var fileUploadDesc: CustomTextView = item.findViewById(R.id.fileUploadDesc)
        val colorPrimary = ThemeUtil.getResourceValue(item.context, R.attr.colorPrimary)
    }

    private class OpenEditText(item: View) : RecyclerView.ViewHolder(item) {
        var edAttrTitle:TextView?=null
        var edAttr:EditText?=null
        init {
            edAttrTitle = item.findViewById(R.id.edAttrTitle)
            edAttr = item.findViewById(R.id.edAttr)
        }
    }

    private class ChipEditText(item: View) : RecyclerView.ViewHolder(item) {
        var edOpenValuesTitle:TextView?=null
        var edOpenValues:NachoTextView?=null
        init {
            edOpenValuesTitle = item.findViewById(R.id.edOpenValuesTitle)
            edOpenValues = item.findViewById(R.id.edOpenValues)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when(viewType){
            AppConstant.AttrTypes.SINGLE_SELECT->{
                val view = inflater.inflate(R.layout.layout_item_attribute_single_select, parent, false)
                SingleSelectHolder(view)
            }
            AppConstant.AttrTypes.MULTI_SELECT->{
                val view = inflater.inflate(R.layout.layout_item_attribute_multi_select, parent, false)
                MultiSelectHolder(view)
            }
            AppConstant.AttrTypes.OPEN_VALUE->{
                val view = inflater.inflate(R.layout.layout_item_attribute_edittext, parent, false)
                OpenEditText(view)
            }
            AppConstant.AttrTypes.OPEN_VALUES->{
                val view = inflater.inflate(R.layout.layout_item_attribute_chip_edittext, parent, false)
                ChipEditText(view)
            }
            AppConstant.AttrTypes.UPLOAD_FIELD-> UploadFileHolder(inflater.inflate(R.layout.layout_doc_upload, parent, false))
            else -> {
                val view = inflater.inflate(R.layout.layout_item_attribute_edittext, parent, false)
                OpenEditText(view)
            }
        }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType){
            AppConstant.AttrTypes.SINGLE_SELECT->bindSingleSelectHolder(holder as SingleSelectHolder,position)
            AppConstant.AttrTypes.MULTI_SELECT->bindMultiSelectHolder(holder as MultiSelectHolder,position)
            AppConstant.AttrTypes.OPEN_VALUE->bindOpenEditText(holder as OpenEditText,position)
            AppConstant.AttrTypes.OPEN_VALUES->bindChipEditText(holder as ChipEditText , position)
            AppConstant.AttrTypes.UPLOAD_FIELD->bindUploadFile(holder as UploadFileHolder,position)
        }
    }

    private fun bindSingleSelectHolder(holder:SingleSelectHolder,position: Int){
        var attribute = list[position]
        holder.spinnerHint?.text = if(attribute.optional)attribute.name else Utils.getSpannableMandatoryTitle(attribute.name)
        val mCategoryList = attribute.attributeValues.map { it.name }
        val adapter = ArrayAdapter<String>(ctx, android.R.layout.simple_spinner_item, mCategoryList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.attrSpinner?.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                holder.txtSingleSelectValue?.text = mCategoryList[position]
                val singleSelectionAttrValue = attribute.attributeValues[position]
                attribute.selectedValues.clear()
                attribute.selectedValues.add(singleSelectionAttrValue)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        })
        holder.attrSpinner?.adapter = adapter
        holder.attrSpinner?.let { spinner ->
            var pos = 0
            if(attribute.selectedValues.isNotEmpty()){
                pos = attribute.attributeValues.indexOf(attribute.selectedValues[0])
            }
            spinner.setSelection(pos)
        }
        holder.singleSelectionLayout?.setOnClickListener { holder.attrSpinner?.performClick() }
    }

    private fun bindMultiSelectHolder(holder:MultiSelectHolder,position: Int){
        var attribute = list[position]
        holder.multiSelectTitle?.text = if(attribute.optional)attribute.name else Utils.getSpannableMandatoryTitle(attribute.name)
        holder.iconAddMultiSelect?.setOnClickListener {
            showMultiSelectBottomSheet(attribute.selectedValues,attribute,position)
        }

        if(attribute.selectedValues.isNotEmpty()){
            holder.iconAddMultiSelect?.visibility = View.GONE
            holder.llMultiSelect?.visibility = View.VISIBLE
            holder.chipGroupMultiSelect?.removeAllViews()
            attribute.selectedValues.forEach {
                val chip = Chip(ctx)
                chip.isCheckable = false
                chip.isFocusable = false
                chip.isCheckedIconVisible = false
                chip.isCloseIconVisible = false
                chip.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
                chip.isCloseIconEnabled = false
                chip.chipBackgroundColor = ctx.resources.getColorStateList(R.color.colorWhiteLight)
                chip.setTextColor(ctx.resources.getColorStateList(R.color.colorBlueLight))
                chip.text = it.name
                chip.id = it.id
                chip.setOnClickListener { showMultiSelectBottomSheet(attribute.selectedValues,attribute,position) }
                holder.chipGroupMultiSelect?.addView(chip)
            }
            holder.chipGroupMultiSelect?.setOnClickListener {
                showMultiSelectBottomSheet(attribute.selectedValues,attribute,position)
            }
        }
        else{
            holder.iconAddMultiSelect?.visibility = View.VISIBLE
            holder.llMultiSelect?.visibility = View.GONE
            holder.chipGroupMultiSelect?.removeAllViews()
        }
    }

    private fun bindOpenEditText(holder:OpenEditText,position: Int){
        val attribute = list[position]
        holder.edAttrTitle?.text = if(attribute.optional)attribute.name else Utils.getSpannableMandatoryTitle(attribute.name)
        holder.edAttr?.setText(attribute.openValues)
        holder.edAttr?.addTextChangedListener(object:TextWatcher{
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                attribute.openValues = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })
    }

    private fun bindChipEditText(holder:ChipEditText,position: Int){
        val attribute = list[position]
        holder.edOpenValuesTitle?.text = if(attribute.optional)attribute.name else Utils.getSpannableMandatoryTitle(attribute.name)
        holder.edOpenValues?.addChipTerminator(',',ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR)
        if(attribute.openValues.isNotEmpty()){
            holder.edOpenValues?.setText(attribute.openValues.split(","))
        }

        holder.edOpenValues?.addTextChangedListener(object :TextWatcher{
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if(p0.toString().contains(" ")){
                    attribute.openValues = holder.edOpenValues?.chipValues?.joinToString(" , ")?:Constant.EMPTY
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })
    }

    private fun bindUploadFile(holder:UploadFileHolder,position: Int){
        val attribute = list[position]
        holder.docUploadHint.text = attribute.name
        if(attribute.selectedValues.isNotEmpty()){
            holder.uploadFileRecyclerView.setVisible()
            holder.uploadFileRecyclerView.layoutManager = LinearLayoutManager(ctx,LinearLayoutManager.VERTICAL,false)
            var adapter:ListingAdapter?=null
            var fileList:MutableList<FileInfo> = attribute.selectedValues.map { it.any as FileInfo }.toMutableList()
            adapter = ListingAdapter(ctx,fileList,
                AppConstant.ListingType.ATTACHMENT_LIST,holder.uploadFileRecyclerView){ position, obj ->
                fileList.removeAt(position)
                attribute.selectedValues.removeAt(position)
                adapter?.notifyDataSetChanged()
                holder.uploadFileLayout.visibility = if(fileList.isEmpty()) View.VISIBLE else View.GONE
            }
            holder.uploadFileRecyclerView.adapter = adapter
            if(attribute.selectedValues.isNotEmpty()){
                holder.uploadFileLayout.setGone()
            }
        }
        else{
            holder.uploadFileRecyclerView.setGone()
            holder.uploadFileLayout.setVisible()
        }
        val drawable = ContextCompat.getDrawable(ctx,R.drawable.bg_dotted_line) as GradientDrawable
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.cornerRadius = 8f
        drawable.setStroke(Utils.getPixel(ctx,1), ContextCompat.getColor(ctx,holder.colorPrimary),10f,10f)
        holder.uploadFileLayout.background = drawable
        holder.uploadFileLayout.safeClickListener {
            onClick?.let { it(ActionType.UPLOAD_FIELD,list[holder.adapterPosition]) }
        }
    }


    private fun showMultiSelectBottomSheet(multiSelectAttributeValues:ArrayList<Value>, attribute: ProductAttribute, position: Int){
        val multiSelectChipBottomSheet = MultiSelectChipBottomSheet()
        val bundle = Bundle()
        bundle.putString("name",attribute.name)
        multiSelectChipBottomSheet.arguments = bundle
        multiSelectChipBottomSheet.setList(multiSelectAttributeValues,attribute.attributeValues)
        multiSelectChipBottomSheet.setListener { selectedList->
            attribute.selectedValues.clear()
            attribute.selectedValues.addAll(selectedList)
            notifyItemChanged(position)
        }
        multiSelectChipBottomSheet.show((ctx as BaseActivity).supportFragmentManager, AppConstant.FragmentTag.MULTI_CHIP_FRAGMENT)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].fieldType
    }

    fun getValues():List<ProductAttribute> = list
}