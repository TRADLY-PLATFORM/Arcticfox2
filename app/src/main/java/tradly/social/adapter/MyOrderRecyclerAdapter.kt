package tradly.social.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.siyamed.shapeimageview.RoundedImageView
import tradly.social.R
import tradly.social.common.base.DateTimeHelper
import tradly.social.common.base.ImageHelper
import tradly.social.common.base.OnLoadMoreListener
import tradly.social.domain.entities.Order

class MyOrderRecyclerAdapter(
    private val context: Context,
    private val inflator: LayoutInflater,
    private val orderList: List<Order>,
    var recyclerView: RecyclerView,
    private val onClickListener: OnClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var totalItemCount = 0
    private var lastVisibleItem = 0
    private val visibleThreshold = 5
    var loading: Boolean = false
    private var OnLoadMoreListener: OnLoadMoreListener? = null

    init {
        setScrolling()
    }
    interface OnClickListener {
        fun onClick(id: String)
    }

    private fun setScrolling() {
        if (recyclerView.layoutManager is LinearLayoutManager) {
            val linearLayoutManager = recyclerView.getLayoutManager() as LinearLayoutManager
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int, dy: Int
                ) {
                    super.onScrolled(recyclerView, dx, dy)
                    totalItemCount = linearLayoutManager.itemCount
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
                    if (!loading && totalItemCount <= lastVisibleItem + visibleThreshold) {
                        // End has been reached
                        OnLoadMoreListener?.onLoadMore()
                        loading = true
                    }

                }
            })
        }
    }

    fun setLoaded() {
        loading = false
    }

    fun setOnLoadMoreListener(listener: OnLoadMoreListener) {
        this.OnLoadMoreListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OrderDetailViewHolder(
            inflator.inflate(
                R.layout.list_item_my_order,
                parent, false
            ), onClickListener
        )
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    fun getItem(position: Int): Order {
        return orderList[position]
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as OrderDetailViewHolder).bind(context,getItem(position))
    }

    class OrderDetailViewHolder(
        private val containerView: View,
        private val onClickListener: OnClickListener
    ) :
        RecyclerView.ViewHolder(containerView) {

        private val productImage: RoundedImageView = containerView.findViewById(R.id.productImage)
        private val productTitle: AppCompatTextView = containerView.findViewById(R.id.productTitle)
        private val productOrderId: AppCompatTextView =
            containerView.findViewById(R.id.productOrderId)
        private val productTimestamp: AppCompatTextView =
            containerView.findViewById(R.id.productTimeStamp)
        private val productTotalPrice: AppCompatTextView =
            containerView.findViewById(R.id.productTotalPrice)

        fun bind(context:Context ,order: Order) {
            ImageHelper.getInstance().loadThumbnailThenMain(
                containerView.context,
                order.image,
                productImage,
                R.drawable.placeholder_image,
                R.drawable.placeholder_image
            )
            productTitle.text = order.listingName
            productOrderId.text = String.format(context.getString(R.string._order_id),order.orderId)
            productTimestamp.text = DateTimeHelper.getDateFromTimeMillis(order.timeStamp, DateTimeHelper.FORMAT_DATE_DD_MM_YYYY)
            productTotalPrice.text = order.price
            containerView.setOnClickListener {
                onClickListener.onClick(order.id)
            }
        }

    }

}