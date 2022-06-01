package tradly.social.ui.map

import android.app.SearchManager
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.layout_search_dialog.*
import kotlinx.android.synthetic.main.layout_search_dialog.progress
import tradly.social.R
import tradly.social.adapter.ListingAdapter
import tradly.social.common.base.AppConstant
import tradly.social.common.DialogListener
import tradly.social.common.base.ThemeUtil
import tradly.social.common.base.showToast
import tradly.social.common.base.ErrorHandler
import tradly.social.domain.entities.Address
import tradly.social.domain.entities.AppError

class LocationSearchFragment : DialogFragment(),MapPresenter.View {
    lateinit var presenter:MapPresenter
    var mView:View?=null
    lateinit var adapter:ListingAdapter
    var dialogListener:DialogListener?=null
    var addressList = mutableListOf<Address>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = MapPresenter(this)
        setHasOptionsMenu(true)
        setStyle(DialogFragment.STYLE_NORMAL, ThemeUtil.getSelectedTheme())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.layout_search_dialog, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setProgressDrawable()
        toolbar?.navigationIcon?.setColorFilter(resources.getColor(R.color.colorDarkBlack), PorterDuff.Mode.SRC_ATOP)
        toolbar?.setNavigationOnClickListener{dismiss()}
        recycler_view?.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        adapter = ListingAdapter(requireContext(),addressList,
            AppConstant.ListingType.LOCATION_SEARCH,recycler_view){ position, obj ->
            dialogListener?.onClick(obj)
            dismiss()
        }
        recycler_view?.adapter = adapter
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        toolbar?.inflateMenu(R.menu.toolbar_menu)
        toolbar?.menu?.findItem(R.id.menu_search)?.let {searchMenu->
            searchMenu.isVisible = true
            val searchView = MenuItemCompat.getActionView(searchMenu) as SearchView
            val searchManager = requireContext().getSystemService(Context.SEARCH_SERVICE) as SearchManager
            searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()))
            searchView.queryHint = getString(R.string.search_location)
            searchView.setIconifiedByDefault(false)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextChange(key: String?): Boolean {
                    if(!key.isNullOrEmpty()){
                        presenter.search(key)
                    }
                    else{
                        presenter.cancelSearch()
                        addressList.clear()
                        adapter.notifyDataSetChanged()
                        hideProgressLoader()
                    }
                    return false
                }

                override fun onQueryTextSubmit(p0: String?): Boolean {
                    return false;
                }
            })
            val closeSearchIcon = searchView.findViewById<ImageView>(R.id.search_close_btn)
            closeSearchIcon.setImageResource(R.drawable.ic_clear_black_24dp)
            closeSearchIcon.setColorFilter(Color.BLACK)
            searchView.setOnCloseListener {
            addressList.clear()
            adapter.notifyDataSetChanged()
                presenter.cancelSearch()
            true
            }
            val editText = searchView.findViewById<EditText>(R.id.search_src_text)
            editText.setHintTextColor(resources.getColor(R.color.colorMediumGrey))
            searchView.maxWidth = Int.MAX_VALUE
            editText?.requestFocus()
            editText?.setTextColor(resources.getColor(R.color.colorTextBlack))
            val magImage = searchView.findViewById<View>(androidx.appcompat.R.id.search_mag_icon) as ImageView
            magImage.visibility = View.GONE
            magImage.setImageDrawable(null)
            val backgroundView = searchView.findViewById(androidx.appcompat.R.id.search_plate) as? View
            backgroundView?.background = null
        }
        super.onPrepareOptionsMenu(menu)
    }
    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
        }
    }

    private fun setProgressDrawable(){
       /* val progressDrawable: Drawable = progress.progressDrawable.mutate()
        progressDrawable.setColorFilter(
            ContextCompat.getColor(requireContext(), R.color.colorBlueLight),
            PorterDuff.Mode.SRC_IN
        )
        progress.progressDrawable = progressDrawable*/
    }

    override fun showProgressLoader() {
        progress?.visibility = View.VISIBLE
    }

    override fun hideProgressLoader() {
        progress?.visibility = View.GONE
    }

    override fun onFailure(appError: AppError) {
        ErrorHandler.handleError(exception = appError)
    }

    override fun networkError(msg: Int) {
       requireContext().showToast(getString(R.string.no_internet))
    }

    override fun showAddress(address: Address) {
    }

    fun setListener(listener: DialogListener){
        this.dialogListener = listener
    }

    override fun showAddresses(addressList: List<Address>) {
        this.addressList.clear()
        this.addressList.addAll(addressList)
        adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroy()
    }
}