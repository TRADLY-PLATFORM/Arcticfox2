package tradly.social.event.addevent.ui.variant.addVariant

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import tradly.social.common.*
import tradly.social.common.adapter.ImageGridAdapter
import tradly.social.common.base.*
import tradly.social.common.util.parser.extension.toJson
import tradly.social.domain.entities.ImageFeed
import tradly.social.domain.entities.Variant
import tradly.social.event.addevent.BR
import tradly.social.event.addevent.R
import tradly.social.event.addevent.databinding.AddVariantDetailFragmentBinding
import tradly.social.event.addevent.ui.variant.viewmodel.VariantFormViewModel
import tradly.social.common.event.addEvent.viewmodel.VariantViewModel
import tradly.social.common.navigation.Activities
import tradly.social.common.network.RequestID
import tradly.social.common.resources.*
import tradly.social.common.util.common.UtilConstant
import tradly.social.common.util.parser.extension.toList
import tradly.social.domain.entities.VariantProperties
import tradly.social.domain.entities.VariantType
import tradly.social.event.addevent.ui.variant.viewmodel.SharedVariantViewModel
import tradly.social.event.explore.common.CommonBaseResourceId
import tradly.social.common.base.BaseFragment
import java.util.*

class AddVariantDetailFragment : BaseFragment(),CustomOnClickListener.OnCustomClickListener,MediaHandler.View,GenericAdapter.OnClickItemListener<ImageFeed> {

    private lateinit var addVariantDetailBinding:AddVariantDetailFragmentBinding
    private lateinit var sharedVariantViewModel: SharedVariantViewModel
    private lateinit var variantFormViewModel:VariantFormViewModel
    private lateinit var variantViewModel: VariantViewModel
    private val mediaHandler: MediaHandler by lazy { MediaHandler(this) }
    private lateinit var imageGridAdapter:ImageGridAdapter
    private var imageList:MutableList<ImageFeed> = mutableListOf()
    private var variant:Variant? = null
    private var isEditVariant:Boolean = false
    private var action:Int =0
    private var eventId:String = AppConstant.EMPTY
    private val imageUploadCount by lazy { AppConfigHelper.getConfigKey<String>(AppConfigHelper.Keys.LISTING_PHOTO_UPLOAD_COUNT,"1").toInt() }

    companion object{
        const val TAG = "AddVariantDetailFragment"
        const val ARG_VARIANT_TYPES = "variantTypes"

        fun newInstance(variantTypeList:List<VariantType>) = AddVariantDetailFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_VARIANT_TYPES,variantTypeList.toJson<VariantType>())
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        sharedVariantViewModel = requireActivity().getViewModel { SharedVariantViewModel() }
        variantFormViewModel = getViewModel { VariantFormViewModel() }
        variantViewModel=  getViewModel { VariantViewModel() }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        addVariantDetailBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_add_variant_detail,container,false)
        setBindingVariable()
        return addVariantDetailBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDataFromIntent()
        setAdapter()
        observeLiveData()
    }

    private fun setDataFromIntent(){
        eventId = requireActivity().getIntentExtra<String>(Activities.VariantHostActivity.EXTRAS_LISTING_ID)
        action = requireActivity().getIntentExtra<Int>(Activities.VariantHostActivity.EXTRAS_CRUD_ACTION)
        val variantTypeList = arguments!!.getString(ARG_VARIANT_TYPES).toList<VariantType>()
        if (variantTypeList.isEmpty()){
            variantViewModel.getVariantTypes()
        }
        else{
            sharedVariantViewModel.setVariantTypeLiveData(variantTypeList)
        }
    }

    private fun setAdapter(){
        if (sharedVariantViewModel.getSelectedVariant() == null){
            this.imageList.add(ImageFeed(isAddItem = true))
        }
        imageGridAdapter = ImageGridAdapter()
        imageGridAdapter.items = imageList
        imageGridAdapter.onClickListener = this
        addVariantDetailBinding.rvGridView.apply {
            layoutManager = GridLayoutManager(requireContext(),2)
            adapter = imageGridAdapter
        }
    }

    private fun setBindingVariable(){
        addVariantDetailBinding.lifecycleOwner = viewLifecycleOwner
        addVariantDetailBinding.focusChangeListener = onFocusChangeListener
        addVariantDetailBinding.onClickListener = CustomOnClickListener(this)
        addVariantDetailBinding.executePendingBindings()
    }

    private fun observeLiveData(){
        sharedVariantViewModel.selectedVariant.observe(viewLifecycleOwner,{
            it?.let {
                populateVariant(it)
            }
        })
        sharedVariantViewModel.selectedCurrency.observe(viewLifecycleOwner,{
            it?.let {
                addVariantDetailBinding.setVariable(BR.selectedSpinnerValue,it.code)
            }
        })
        viewLifecycleOwner.observeEvent(variantFormViewModel.onFieldErrorLiveData,this::onFieldError)
        viewLifecycleOwner.observeEvent(variantViewModel.uiState,this::handleUIState)
        variantFormViewModel.onValidationSuccess.observe(viewLifecycleOwner,this::onValidationSuccess)
        variantViewModel.onUploadImageLiveData.observe(viewLifecycleOwner,this::onImageUploaded)
        variantViewModel.variantTypeLiveData.observe(viewLifecycleOwner,{
            sharedVariantViewModel.setVariantTypeLiveData(it)
        })
        sharedVariantViewModel.selectedVariantValueList.observe(viewLifecycleOwner,{showSelectedVariants(it)})
        viewLifecycleOwner.observeEvent(variantViewModel.onDeleteVariantLiveData,{onDeleteSuccess()})
        viewLifecycleOwner.observeEvent(variantViewModel.onUpdateVariantLiveData,{onFinish()})
        viewLifecycleOwner.observeEvent(variantViewModel.onAddVariantLiveData,{
            this.variant = it
            onFinish()
        })
    }

    private fun onImageUploaded(list:List<String>){
        this.variant!!.images = list
        onAddVariant()
    }

    private fun populateVariant(variant: Variant){
        this.variant = variant
        this.isEditVariant = !variant.id.startsWith(UtilConstant.NEW_ID)
        requireActivity().invalidateOptionsMenu()
        addVariantDetailBinding.setVariable(BR.variant,variant)
        val images = variant.images.map { ImageFeed(filePath = it) }.toMutableList()
        if (variant.images.size< imageUploadCount){
            images.add(ImageFeed(isAddItem = true))
        }
        this.imageList.apply {
            clear()
            addAll(images)
        }
        this.imageGridAdapter.notifyDataSetChanged()
        if (this.imageList.any { !it.isAddItem }){
            addVariantDetailBinding.addViewCard.setGone()
            addVariantDetailBinding.rvGridView.setVisible()
        }
    }

    override fun onCustomClick(view: View) {
        when(view.id){
            R.id.btnAddVariant-> {
                sharedVariantViewModel.let {
                    if (it.variantTypeLiveData.value!=null){
                        it.setFragmentNavigation(AddVariantFragment.TAG)
                    }
                    else{
                        variantViewModel.getVariantTypes()
                    }
                }
            }
            R.id.btnSaveChanges-> onValidate()
            R.id.addViewCard -> showMediaChooserDialog()
        }
    }

    private fun onAddVariant(){
        if (eventId.isNotEmpty()){
            if (action ==Activities.VariantHostActivity.CRUD_ACTION.ADD){
                variantViewModel.addVariant(eventId,this.variant!!)
            }
            else{
                if (!this.variant!!.id.startsWith(tradly.social.common.util.common.AppConstant.NEW_ID)){
                    variantViewModel.updateVariant(this.variant!!,eventId)
                }
                else{
                    onFinish()
                }
            }
        }
        else{
            onFinish()
        }
    }

    private fun onValidate(){
        variantFormViewModel.isValid(sharedVariantViewModel.getSelectedCurrency(),imageList.filterNot { it.isAddItem }.toList(),
        addVariantDetailBinding.textInputEventTitle.editText.getString(),
        addVariantDetailBinding.textInputEventDescription.editText.getString(),
        addVariantDetailBinding.textInputPriceLayout.textInputEdDropDown.editText.getString(),
        addVariantDetailBinding.textInputOffer.editText.getString().toSafeInt(),
        addVariantDetailBinding.textInputTicket.editText.getString().toSafeInt(),sharedVariantViewModel.getSelectedVariantValueList())
    }

    private fun onValidationSuccess(variant: Variant){
        this.variant = variant.apply {
            if (this@AddVariantDetailFragment.variant!=null){
                this.id = this@AddVariantDetailFragment.variant!!.id
            }
        }
        this.variant!!.values = sharedVariantViewModel.getSelectedVariantValueList()!!
        if (this.imageList.any { !it.filePath.startsWith("http") }){
            variantViewModel.uploadImages(this.imageList.filterNot { it.isAddItem })
        }
        else{
            onAddVariant()
        }
    }

    private fun onDeleteSuccess(){
        val bundle = Bundle()
        this.variant?.values = sharedVariantViewModel.getSelectedVariantValueList()!!
        bundle.putString(Activities.VariantHostActivity.EXTRAS_VARIANT,this.variant.toJson<Variant>())
        bundle.putInt(Activities.VariantHostActivity.EXTRAS_CRUD_ACTION,action)
        sharedVariantViewModel.setOnFinish(bundle)
    }

    private fun onFinish(){
        val bundle = Bundle()
        this.variant?.values = sharedVariantViewModel.getSelectedVariantValueList()!!
        val action = requireActivity().getIntentExtra<Int>(Activities.VariantHostActivity.EXTRAS_CRUD_ACTION)
        if (Activities.VariantHostActivity.CRUD_ACTION.ADD == action){
            this.variant?.id = UtilConstant.NEW_ID+sharedVariantViewModel.selectedVariantList.value!!.size
        }
        bundle.putString(Activities.VariantHostActivity.EXTRAS_VARIANT,this.variant.toJson<Variant>())
        bundle.putInt(Activities.VariantHostActivity.EXTRAS_CRUD_ACTION,action)
        sharedVariantViewModel.setOnFinish(bundle)
    }

    private fun onClickVariantDelete(){
        if (!this.variant!!.id.startsWith(UtilConstant.NEW_ID)){
            variantViewModel.deleteVariant(this.variant!!.id,eventId)
        }
        else{
            onFinish()
        }
    }

    override fun onClick(value: ImageFeed, view: View, itemPosition: Int) {
        when(view.id){
            CommonBaseResourceId.cvImageParent->{
                if (value.isAddItem){
                    showMediaChooserDialog()
                }
            }
            CommonBaseResourceId.ivCancel-> onRemoveImage(itemPosition)
        }

    }

    private fun onRemoveImage(position: Int){
        this.imageList.removeAt(position)
        imageGridAdapter.notifyItemRemoved(position)
        takeIf { this.imageList.find{ it.isAddItem } == null }?.let {
            this.imageList.add(ImageFeed(isAddItem = true))
        }
        imageGridAdapter.notifyItemInserted(this.imageList.size-1)
    }

    private fun showMediaChooserDialog(){
        Utils.showMediaChooseDialog(requireContext()){
            if (it == CommonBaseResourceId.clPhotoView){
                mediaHandler.openCamera(this)
            }
            else if (BottomChooserDialog.Type.GALLERY == it){
                mediaHandler.openGallery(this)
            }
        }
    }

    private val onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
        if (!hasFocus) {
            val editText = v as? TextInputEditText
            if (editText?.parent?.parent is TextInputLayout) {
                val textInputLayout = editText.parent?.parent as TextInputLayout
                textInputLayout.error = null
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mediaHandler.onActivityResult(this,requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mediaHandler.onRequestPermissionsResult(this,requestCode, permissions, grantResults)
    }

    override fun onMediaResult(filePath: String) {
        this.imageList.add(this.imageList.size-1,ImageFeed(filePath))
        if (this.imageList.filterNot { it.isAddItem }.size == imageUploadCount){
            this.imageList.removeAll { it.isAddItem }
        }
        imageGridAdapter.notifyDataSetChanged()
        addVariantDetailBinding.addViewCard.setGone()
        addVariantDetailBinding.rvGridView.setVisible()
    }

    private fun onFieldError(pair:Pair<Int,Int>){
        val (fieldId,errorResId) = pair
        when(fieldId){
            R.id.textInputEventTitle-> addVariantDetailBinding.textInputEventTitle.error = getString(errorResId)
            R.id.textInputEventDescription-> addVariantDetailBinding.textInputEventDescription.error = getString(errorResId)
            R.id.textInputPriceLayout-> addVariantDetailBinding.textInputPriceLayout.textInputEdDropDown.error = getString(errorResId)
            R.id.textInputOffer-> addVariantDetailBinding.textInputOffer.error = getString(errorResId)
            R.id.textInputTicket-> addVariantDetailBinding.textInputTicket.error = getString(errorResId)
            R.id.addViewCard -> requireContext().showToast(errorResId)
            R.id.btnAddVariant ->requireContext().showToast(errorResId)

        }
    }

    private fun handleUIState(uiState: UIState<Unit>){
        when(uiState){
            is UIState.Loading->{
                when(uiState.apiId){
                    RequestID.UPLOAD_IMAGES,
                    RequestID.GET_VARIANT_TYPES,
                    RequestID.DELETE_VARIANT,
                    RequestID.ADD_VARIANT,
                    RequestID.UPDATE_VARIANT->{
                        if(uiState.isLoading){
                            showLoader(CommonResourceString.please_wait)
                        }
                        else{
                            hideLoader()
                        }
                    }
                }
            }
            is UIState.Failure-> ErrorHandler.handleError(uiState.errorData)
        }
    }

    private fun showSelectedVariants(list: List<VariantProperties>){
        addVariantDetailBinding.selectedVariantChipGroup.removeAllViews()
        addVariantDetailBinding.selectedVariantChipGroup.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
        addVariantDetailBinding.btnAddVariant.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        list.forEach { variantProperties->
            val chip = Chip(requireContext())
            chip.shapeAppearanceModel = ShapeAppearanceModel().withCornerSize(8f).toBuilder().build()
            chip.closeIconTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),CommonResourceColor.white))
            chip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),CommonResourceColor.colorPrimary))
            chip.tag = variantProperties
            chip.text = variantProperties.variantValue
            chip.setTextColor(ContextCompat.getColor(requireContext(),CommonResourceColor.white))
            chip.setOnClickListener {
                sharedVariantViewModel.setFragmentNavigation(AddVariantFragment.TAG)
            }
            addVariantDetailBinding.selectedVariantChipGroup.addView(chip)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(CommonResourceMenu.toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(CommonResourceId.action_delete)?.isVisible = this.isEditVariant
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            CommonResourceId.action_delete-> onClickVariantDelete()
        }
        return super.onOptionsItemSelected(item)
    }
}