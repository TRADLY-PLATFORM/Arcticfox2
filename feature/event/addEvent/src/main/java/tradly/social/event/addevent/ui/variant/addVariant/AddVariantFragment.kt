package tradly.social.event.addevent.ui.variant.addVariant

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.chip.Chip
import com.google.android.material.shape.ShapeAppearanceModel
import tradly.social.common.base.CustomOnClickListener
import tradly.social.common.base.getViewModel
import tradly.social.common.resources.*
import tradly.social.domain.entities.VariantProperties
import tradly.social.domain.entities.VariantType
import tradly.social.event.addevent.R
import tradly.social.event.addevent.databinding.AddVariantBinding
import tradly.social.event.addevent.ui.variant.viewmodel.SharedVariantViewModel
import tradly.social.common.base.BaseFragment

class AddVariantFragment : BaseFragment(),CustomOnClickListener.OnCustomClickListener {

    private lateinit var addVariantBinding: AddVariantBinding
    private lateinit var sharedVariantViewModel: SharedVariantViewModel
    private var selectedVariantTypeId:Int = 0
    private var selectedVariantValue:VariantType?=null
    private var variantValueList:MutableList<VariantProperties> = mutableListOf()
    private var variantTypeInitSelectionIndex = -1
    private var variantValuesInitSelectionIndex = -1

    companion object{
      const val TAG = "AddVariantFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        sharedVariantViewModel = requireActivity().getViewModel { SharedVariantViewModel() }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        addVariantBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_add_variant,container,false)
        addVariantBinding.onClickListsner = CustomOnClickListener(this)
        addVariantBinding.lifecycleOwner = viewLifecycleOwner
        return addVariantBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSelectedValue()
        initSelectedChips()
    }

    private fun initSelectedValue(){
        sharedVariantViewModel.getSelectedVariantValueList()?.let { variantList ->
            variantTypeInitSelectionIndex = sharedVariantViewModel.variantTypeLiveData.value!!.indexOfFirst {
                it.id == variantList.getOrNull(0)?.variantTypeId
            }
            if (variantTypeInitSelectionIndex!=-1){
                val variantTypeValues = sharedVariantViewModel.variantTypeLiveData.value!![variantTypeInitSelectionIndex].variantTypeValues
                val selectedVariantValueId = variantList.getOrNull(0)?.variantValueId
                variantValuesInitSelectionIndex = variantTypeValues.indexOfFirst { it.id == selectedVariantValueId }
            }
        }
        sharedVariantViewModel.variantTypeLiveData.value?.let { list->
            setVariantTypeAdapter(list.filter { it.isActive }.map { it.name })
        }
    }

    private fun initSelectedChips(){
        sharedVariantViewModel.getSelectedVariantValueList()?.let { variantList ->
            variantValueList.addAll(variantList)
            showSelectedChipVariant()
        }
    }

    private fun setVariantTypeAdapter(variantTypes: List<String>){
        val variantTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item,variantTypes)
        variantTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        addVariantBinding.spinnerVariantTypes.adapter = variantTypeAdapter
        addVariantBinding.spinnerVariantTypes.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
               onVariantTypeSelected(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

    }

    private fun setVariantValuesAdapter(variantValues:List<String>){
        val variantValuesAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item,variantValues)
        variantValuesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        addVariantBinding.spinnerVariantValues.adapter = variantValuesAdapter
        addVariantBinding.spinnerVariantValues.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                sharedVariantViewModel.variantTypeLiveData.value!!.find { it.id == selectedVariantTypeId }?.let {
                    selectedVariantValue = it.variantTypeValues[position]
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        if(variantValuesInitSelectionIndex!=-1){
            addVariantBinding.spinnerVariantValues.setSelection(variantValuesInitSelectionIndex,true)
        }
    }

    private fun onVariantTypeSelected(position:Int){
        sharedVariantViewModel.variantTypeLiveData.value?.getOrNull(position)?.let { selectedVariantType ->
            selectedVariantTypeId = selectedVariantType.id
            setVariantValuesAdapter(selectedVariantType.variantTypeValues.map { i-> i.name })
        }
        if (variantTypeInitSelectionIndex!=-1){
            addVariantBinding.spinnerVariantTypes.setSelection(variantTypeInitSelectionIndex,true)
        }
    }

    private fun showSelectedChipVariant(){
        addVariantBinding.chipGroupSelectedVariant.removeAllViews()
        addVariantBinding.chipGroupSelectedVariant.visibility = if (variantValueList.isEmpty()) View.GONE else View.VISIBLE
        variantValueList.forEach { addChipVariant(it) }
    }

    private fun addChipVariant(variantProperties:VariantProperties){
        val chip = Chip(requireContext())
        chip.shapeAppearanceModel = ShapeAppearanceModel().withCornerSize(8f).toBuilder().build()
        chip.closeIcon = ContextCompat.getDrawable(requireContext(),CommonResourceDrawable.ic_cancel_black_24dp)
        chip.closeIconTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),CommonResourceColor.white))
        chip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),CommonResourceColor.colorPrimary))
        chip.tag = variantProperties
        chip.isCloseIconVisible = true
        chip.text = variantProperties.variantValue
        chip.setTextColor(ContextCompat.getColor(requireContext(),CommonResourceColor.white))
        chip.setOnCloseIconClickListener {
            val index = variantValueList.indexOfFirst { it.variantValueId == (chip.tag as VariantProperties).variantValueId }
            if (index!=-1){
                variantValueList.removeAt(index)
                showSelectedChipVariant()
            }
        }
        addVariantBinding.chipGroupSelectedVariant.addView(chip)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(CommonResourceMenu.toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(CommonResourceId.action_save)?.isVisible = true
        menu.findItem(CommonResourceId.action_delete)?.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == CommonResourceId.action_save){
            onSaveVariant()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCustomClick(view: View) {
        when(view.id){
            R.id.btnAddVariant-> onClickAddVariant()
        }
    }

    private fun onClickAddVariant(){
        selectedVariantValue?.let { selectedVariantValue->
            val isVariantTypeSelected = variantValueList.any { it.variantTypeId == selectedVariantTypeId }
            takeIf { isVariantTypeSelected && variantValueList.any{ i->i.variantValueId == selectedVariantValue.id} }.let { value->
                if (value == null){
                    variantValueList.removeAll { it.variantTypeId == selectedVariantTypeId }
                }
                else{
                    return
                }
                val variantProperties = VariantProperties(
                    variantTypeId = selectedVariantTypeId,
                    variantValue = selectedVariantValue.name,
                    variantValueId = selectedVariantValue.id
                )
                variantValueList.add(variantProperties)
                showSelectedChipVariant()
            }
        }
    }

    private fun onSaveVariant(){
        sharedVariantViewModel.setSelectedVariantValueList(variantValueList)
        sharedVariantViewModel.popFragment()
    }

}