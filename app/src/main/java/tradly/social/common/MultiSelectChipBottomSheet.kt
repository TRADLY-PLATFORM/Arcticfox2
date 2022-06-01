package tradly.social.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import tradly.social.R
import tradly.social.domain.entities.Value
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.bottom_sheet_multi_select_chip.*
import tradly.social.common.base.ThemeUtil

class MultiSelectChipBottomSheet : BottomSheetDialogFragment() {
    private var mView: View? = null
    private var onSubmit: ((selectedList:ArrayList<Value>) -> Unit)? = null
    private var mList = ArrayList<Value>()
    private var originalList = listOf<Value>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.bottom_sheet_multi_select_chip, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { bundle ->
            bundle.getString("name")?.let {
                sheetTitle.text = it
                addChipViews()
            }
        }
        btnDone.setOnClickListener {
            onSubmit?.let {
                it(mList)
                dismiss()
            }
        }
        actionClose.setOnClickListener { dismiss() }
    }

    private fun addChipViews() {
        chipGroup.removeAllViews()
        for (element in originalList) {
            val item = element
            val chip = Chip(requireContext())
            chip.isCheckable = true
            chip.isFocusable = true
            chip.isCheckedIconVisible = false
            chip.isCloseIconVisible = false
            chip.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
            chip.isCloseIconEnabled = false
            if(mList.contains(element)){
                chip.chipBackgroundColor = resources.getColorStateList(ThemeUtil.getResourceValue(requireContext(), R.attr.colorPrimary))
                chip.setTextColor(resources.getColorStateList(R.color.colorWhite))
            }
            else{
                chip.chipBackgroundColor = resources.getColorStateList(R.color.colorWhiteLight)
                chip.setTextColor(resources.getColorStateList(R.color.colorBlueLight))
            }
            chip.text = item.name
            chip.id = item.id
            chip.setOnClickListener {
                if (mList.contains(item)) {
                    chip.setTextColor(resources.getColorStateList(R.color.colorBlueLight))
                    chip.chipBackgroundColor = resources.getColorStateList(R.color.colorWhiteLight)
                    mList.remove(item)
                } else {
                    chip.setTextColor(resources.getColorStateList(R.color.colorWhite))
                    chip.chipBackgroundColor = resources.getColorStateList(
                        ThemeUtil.getResourceValue(
                            requireContext(),
                            R.attr.colorPrimary
                        )
                    )
                    mList.add(item)
                }
            }
            chipGroup.addView(chip)
        }
    }

    fun setListener(onSubmit: (list: List<Value>) -> Unit) {
        this.onSubmit = onSubmit
    }

    fun setList(selectedList:ArrayList<Value>,originalList:List<Value>){
        mList.clear()
        mList.addAll(ArrayList(selectedList))
        this.originalList = originalList
    }
}