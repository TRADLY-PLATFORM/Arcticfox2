package tradly.social.adapter

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import tradly.social.R
import tradly.social.adapter.ChatThreadAdapter.ViewType.VIEW_TYPE_RECEIVER
import tradly.social.adapter.ChatThreadAdapter.ViewType.VIEW_TYPE_SENDER
import tradly.social.common.*
import tradly.social.ui.product.addProduct.productDetail.ProductDetailActivity
import com.github.siyamed.shapeimageview.RoundedImageView
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query
import tradly.social.common.base.*
import tradly.social.common.views.custom.CustomTextView
import tradly.social.common.network.converters.ProductConverter
import tradly.social.domain.entities.*

class ChatThreadAdapter(
    var ctx: Context,
    var chatRoomId:String?,
    val receiver: User,
    var query: Query,
    list: ArrayList<Chat>?,
    keys: ArrayList<String>?,
    var recyclerView: RecyclerView
) : FirebaseChatsRecyclerAdapter<RecyclerView.ViewHolder, Chat>(query, list, keys) {

    var userId: String? = null
    private val inflater: LayoutInflater
    private var chatListener:ChatThreadListener?=null

    init {
        inflater = LayoutInflater.from(ctx)
        userId = AppController.appController.getUser()?.id
    }

    object ViewType {
        val VIEW_TYPE_SENDER = 0
        val VIEW_TYPE_RECEIVER = 1
    }

    class SenderViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var chatText: TextView? = null
        var chatTime: TextView? = null
        var docLayout: LinearLayout? = null
        var productLayout:RelativeLayout?=null
        var documentPlaceholder: ImageView? = null
        var fileName: TextView? = null
        var txtProductName:TextView?=null
        var txtFinalPrice:TextView?=null
        var txtPrice:TextView?=null
        var txtOffer:TextView?=null
        var imgProduct:RoundedImageView?=null
        var deliveryStatus:ImageView
        var clMessage:ConstraintLayout
        var btnAccept:MaterialButton
        var btnDecline:MaterialButton
        var offerExpireTime: CustomTextView
        var requestedOfferValue: CustomTextView
        var offerRequestedTxt: CustomTextView
        var negotiationChatTime: CustomTextView
        var negotiationIconDeliveryStatus:ImageView
        var productImage:RoundedImageView
        var productName: CustomTextView
        var ratingBar:RatingBar
        var txtRatingValue: CustomTextView
        var tvRatingCount: CustomTextView
        var productPrice: CustomTextView
        var offerStatus: CustomTextView
        var clNegotiationRight:ConstraintLayout
        var ratingInfoGroup:Group

        init {
            chatText = item.findViewById(R.id.chatText)
            chatTime = item.findViewById(R.id.ChatTime)
            docLayout = item.findViewById(R.id.documentLayout)
            documentPlaceholder = item.findViewById(R.id.documentPlaceholder)
            productLayout = item.findViewById(R.id.productLayout)
            fileName = item.findViewById(R.id.documentName)
            txtProductName = item.findViewById(R.id.txtProductName)
            txtFinalPrice = item.findViewById(R.id.txtFinalPrice)
            txtPrice = item.findViewById(R.id.txtPrice)
            txtOffer = item.findViewById(R.id.txtOffer)
            imgProduct = item.findViewById(R.id.imgProduct)
            deliveryStatus = item.findViewById(R.id.iconDeliveryStatus)
            clMessage = item.findViewById(R.id.clMessage)
            btnAccept = item.findViewById(R.id.btnAccept)
            btnDecline = item.findViewById(R.id.btnDecline)
            requestedOfferValue = item.findViewById(R.id.requestedOfferValue)
            offerRequestedTxt = item.findViewById(R.id.offerRequestedTxt)
            offerExpireTime = item.findViewById(R.id.offerExpireTime)
            negotiationChatTime = item.findViewById(R.id.negotiationChatTime)
            negotiationIconDeliveryStatus = item.findViewById(R.id.negotiationIconDeliveryStatus)
            productImage = item.findViewById(R.id.productImage)
            productName = item.findViewById(R.id.productName)
            ratingBar = item.findViewById(R.id.ratingBar)
            txtRatingValue = item.findViewById(R.id.txtRatingValue)
            tvRatingCount = item.findViewById(R.id.tvRatingCount)
            productPrice = item.findViewById(R.id.productPrice)
            offerStatus = item.findViewById(R.id.offerStatus)
            clNegotiationRight = item.findViewById(R.id.clNegotiationRight)
            ratingInfoGroup = item.findViewById(R.id.ratingInfoGroup)
        }
    }

    class ReceiverViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var chatText: TextView? = null
        var chatTime: TextView? = null
        var docLayout: LinearLayout? = null
        var productLayout:RelativeLayout?=null
        var documentPlaceholder: ImageView? = null
        var fileName: TextView? = null
        var txtProductName:TextView?=null
        var txtFinalPrice:TextView?=null
        var txtPrice:TextView?=null
        var txtOffer:TextView?=null
        var imgProduct:RoundedImageView?=null
        val llMessage:LinearLayout
        var btnAccept:MaterialButton
        var btnDecline:MaterialButton
        var offerRequestedTxt: CustomTextView
        var requestedOfferValue: CustomTextView
        var offerStatus: CustomTextView
        var offerExpireTime: CustomTextView
        var negotiationChatTime: CustomTextView
        var negotiationIconDeliveryStatus:ImageView
        var productImage:RoundedImageView
        var productName: CustomTextView
        var ratingBar:RatingBar
        var txtRatingValue: CustomTextView
        var tvRatingCount: CustomTextView
        var productPrice: CustomTextView
        var clNegotiationLeft:ConstraintLayout
        var ratingInfoGroup:Group

        init {
            chatText = item.findViewById(R.id.chatText)
            chatTime = item.findViewById(R.id.ChatTime)
            docLayout = item.findViewById(R.id.documentLayout)
            productLayout = item.findViewById(R.id.productLayout)
            documentPlaceholder = item.findViewById(R.id.documentPlaceholder)
            fileName = item.findViewById(R.id.documentName)
            txtProductName = item.findViewById(R.id.txtProductName)
            txtFinalPrice = item.findViewById(R.id.txtFinalPrice)
            txtPrice = item.findViewById(R.id.txtPrice)
            txtOffer = item.findViewById(R.id.txtOffer)
            imgProduct = item.findViewById(R.id.imgProduct)
            llMessage = item.findViewById(R.id.llMessageLayout)
            btnAccept = item.findViewById(R.id.btnAccept)
            btnDecline = item.findViewById(R.id.btnDecline)
            requestedOfferValue = item.findViewById(R.id.requestedOfferValue)
            offerRequestedTxt = item.findViewById(R.id.offerRequestedTxt)
            offerExpireTime = item.findViewById(R.id.offerExpireTime)
            negotiationChatTime = item.findViewById(R.id.negotiationChatTime)
            negotiationIconDeliveryStatus = item.findViewById(R.id.negotiationIconDeliveryStatus)
            productImage = item.findViewById(R.id.productImage)
            productName = item.findViewById(R.id.productName)
            ratingBar = item.findViewById(R.id.ratingBar)
            txtRatingValue = item.findViewById(R.id.txtRatingValue)
            tvRatingCount = item.findViewById(R.id.tvRatingCount)
            productPrice = item.findViewById(R.id.productPrice)
            offerStatus = item.findViewById(R.id.offerStatus)
            clNegotiationLeft = item.findViewById(R.id.clNegotiationLeft)
            ratingInfoGroup = item.findViewById(R.id.ratingInfoGroup)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            VIEW_TYPE_SENDER -> {
                val view = inflater.inflate(R.layout.layout_chat_right, parent, false)
                return SenderViewHolder(view)
            }
            else->{
                val view = inflater.inflate(R.layout.layout_chat_left, parent, false)
                return ReceiverViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chat = getItem(position)
        if(holder is SenderViewHolder){
            val senderViewHolder = holder as? SenderViewHolder
            if (chat.mimeType != MessageHelper.Companion.Type.MAKE_OFFER){
                senderViewHolder?.chatTime?.text = DateTimeHelper.TimeAgo.getDate(chat.timeStamp.toString())
                MessageHelper.setReadStatus(senderViewHolder?.deliveryStatus,chat.deliveryStatus)
            }
            senderViewHolder?.clMessage.setGone()
            senderViewHolder?.clNegotiationRight?.setGone()
            when(chat.mimeType){
                MessageHelper.Companion.Type.MSG->{
                    senderViewHolder?.clMessage.setVisible()
                    senderViewHolder?.chatText?.text =  chat.message
                    senderViewHolder?.chatText?.visibility = View.VISIBLE
                    senderViewHolder?.docLayout?.visibility = View.GONE
                    senderViewHolder?.productLayout?.visibility = View.GONE
                }
                MessageHelper.Companion.Type.PRODUCT->{
                    val product = ProductConverter.mapFromString(chat.message)
                    senderViewHolder?.clMessage.setVisible()
                    senderViewHolder?.docLayout?.visibility = View.GONE
                    senderViewHolder?.docLayout?.visibility = View.GONE
                    senderViewHolder?.chatText?.visibility = View.GONE
                    senderViewHolder?.productLayout?.visibility = View.VISIBLE
                    product.let {
                        senderViewHolder?.txtProductName?.text = it.title
                        senderViewHolder?.txtPrice?.text =it.listPrice?.displayCurrency
                        senderViewHolder?.txtPrice?.let {
                            it.paintFlags = it.paintFlags or  Paint.STRIKE_THRU_TEXT_FLAG
                        }
                        senderViewHolder?.txtFinalPrice?.text = it.offerPrice?.displayCurrency
                        senderViewHolder?.txtOffer?.text = String.format(ctx.getString(R.string.off_data),it.offerPercent.toString())

                        var productImage = Constant.EMPTY
                        if(product.images.count()>0){
                            productImage = product.images[0]
                        }
                        ImageHelper.getInstance().showImage(ctx,productImage,senderViewHolder?.imgProduct,R.drawable.placeholder_image,R.drawable.placeholder_image)
                    }

                }
                MessageHelper.Companion.Type.MAKE_OFFER->{
                    val product = ProductConverter.mapFromString(chat.makeOffer!!.content)
                    senderViewHolder?.clNegotiationRight?.apply {
                        setVisible()
                        setOnClickListener {
                            chatListener?.onclickBuy(product.id)
                        }
                    }
                    senderViewHolder?.negotiationChatTime?.text = DateTimeHelper.TimeAgo.getDate(chat.timeStamp.toString())
                    MessageHelper.setReadStatus(senderViewHolder?.negotiationIconDeliveryStatus,chat.deliveryStatus)
                    ImageHelper.getInstance().showImage(ctx,product.images.getOrElse(0) { AppConstant.EMPTY },senderViewHolder?.productImage,R.drawable.placeholder_image,R.drawable.placeholder_image)
                    senderViewHolder?.productName?.text = product.title
                    senderViewHolder?.productPrice?.text = product.offerPrice?.displayCurrency
                    product.rating?.let {
                        senderViewHolder?.ratingInfoGroup?.setVisible()
                        senderViewHolder?.ratingBar?.rating = it.ratingAverage.toFloat()
                        senderViewHolder?.txtRatingValue?.text = it.ratingAverage.toString()
                        senderViewHolder?.tvRatingCount?.text = ctx.getString(R.string.chatdetail_ratings,it.ratingCount.toString())
                    }
                    chat.makeOffer?.let {
                        val status = if (it.buyer.userId == userId) it.buyer.offerStatus else it.seller.offerStatus
                        senderViewHolder?.requestedOfferValue?.text = chat.makeOffer?.negotiation?.price
                        when(status){
                            MessageHelper.Companion.Type.MakeOfferType.SENT_REQUEST->{
                                senderViewHolder?.btnAccept?.setGone()
                                senderViewHolder?.btnDecline?.setGone()
                            }
                            MessageHelper.Companion.Type.MakeOfferType.RECEIVED_REQUEST->{
                                senderViewHolder?.offerRequestedTxt?.text = ctx.getString(R.string.chat_offer)
                                senderViewHolder?.btnAccept?.setVisible()
                                senderViewHolder?.btnDecline?.setVisible()
                                senderViewHolder?.btnAccept?.setOnClickListener {
                                    val seller = Person(userId!!,MessageHelper.Companion.Type.MakeOfferType.SKIP)
                                    val buyer = Person(receiver.id,MessageHelper.Companion.Type.MakeOfferType.READY_TO_BUY)
                                    val makeOffer = MakeOffer(chat.makeOffer!!.content,buyer,seller,chat.makeOffer!!.negotiation)
                                    chatListener?.onClickAccept(getKeyAtPosition(position),product.id,makeOffer)
                                }
                                senderViewHolder?.btnDecline?.setOnClickListener {
                                    val seller = Person(userId!!,MessageHelper.Companion.Type.MakeOfferType.SKIP)
                                    val buyer = Person(receiver.id,MessageHelper.Companion.Type.MakeOfferType.DENIED_REQUEST)
                                    val makeOffer = MakeOffer(chat.makeOffer!!.content,buyer,seller,chat.makeOffer!!.negotiation)
                                    chatListener?.onClickDecline(getKeyAtPosition(position),product.id,makeOffer)
                                }
                            }
                            MessageHelper.Companion.Type.MakeOfferType.READY_TO_BUY->{
                                senderViewHolder?.btnAccept?.setVisible()
                                senderViewHolder?.btnAccept?.text = ctx.getString(R.string.chat_buy_now)
                                senderViewHolder?.offerStatus?.apply {
                                    background = Utils.getDrawable(ctx,0,0,R.color.colorGreen,
                                        Utils.getPixel(ctx,16).toFloat())
                                    text = ctx.getString(R.string.chat_offer_accepted)
                                }

                                senderViewHolder?.btnAccept?.setOnClickListener {
                                    chatListener?.onclickBuy(product.id)
                                }
                                senderViewHolder?.offerRequestedTxt?.text = ctx.getString(R.string.chat_accepted)
                                val noOfHours = DateTimeHelper.TimeAgo.getHours(chat.timeStamp as Long).toInt()
                                if (noOfHours>=24){
                                    senderViewHolder?.offerExpireTime?.text =  ctx.getString(R.string.chat_expired)
                                }
                                else{
                                    senderViewHolder?.offerExpireTime?.text =  ctx.resources.getQuantityString(R.plurals.chat_expires,(24-noOfHours),(24-noOfHours))
                                }
                                senderViewHolder?.offerExpireTime?.setVisible()
                            }
                             MessageHelper.Companion.Type.MakeOfferType.ACCEPTED_REQUEST->{
                                 senderViewHolder?.btnAccept?.setGone()
                                 senderViewHolder?.btnDecline?.setGone()
                                 senderViewHolder?.offerStatus?.apply {
                                    background = Utils.getDrawable(ctx,0,0,R.color.colorGreen,
                                        Utils.getPixel(ctx,16).toFloat())
                                    text = ctx.getString(R.string.chat_accepted)
                                }
                                senderViewHolder?.offerRequestedTxt?.text = ctx.getString(R.string.chat_offer)
                            }
                            MessageHelper.Companion.Type.MakeOfferType.DENIED_REQUEST->{
                                senderViewHolder?.btnAccept?.setGone()
                                senderViewHolder?.btnDecline?.setGone()
                                senderViewHolder?.offerRequestedTxt?.text = ctx.getString(R.string.chat_offer)
                                senderViewHolder?.offerStatus?.apply {
                                    setVisible()
                                    background = Utils.getDrawable(ctx,0,0,R.color.colorRed,
                                        Utils.getPixel(ctx,16).toFloat())
                                    text = ctx.getString(R.string.chat_declined)
                                }
                            }
                            else ->{}
                        }
                    }
                }
                else->{
                    senderViewHolder?.clMessage.setVisible()
                    senderViewHolder?.chatText?.visibility = View.GONE
                    senderViewHolder?.docLayout?.visibility = View.VISIBLE
                    senderViewHolder?.productLayout?.visibility = View.GONE
                    senderViewHolder?.documentPlaceholder?.setImageResource(FileHelper().getFileImage(chat.mimeType))
                    senderViewHolder?.documentPlaceholder?.setColorFilter(ContextCompat.getColor(ctx, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN)
                    senderViewHolder?.fileName?.text = chat.fileName
                }
            }

            senderViewHolder?.itemView?.setOnClickListener {
                when(FileHelper().getFileType(chat.mimeType)){
                    MessageHelper.Companion.Type.PRODUCT->{
                        val product = ProductConverter.mapFromString(chat.message)
                        val intent = Intent(ctx, ProductDetailActivity::class.java)
                        intent.putExtra("productId",product.id)
                        ctx.startActivity(intent)
                    }
                    MessageHelper.Companion.Type.APPLICATION,
                    MessageHelper.Companion.Type.AUDIO,
                    MessageHelper.Companion.Type.VIDEO,
                    MessageHelper.Companion.Type.IMAGE,
                    MessageHelper.Companion.Type.TEXT->{
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(chat.message)
                        try{
                            ctx.startActivity(intent)
                        }catch (ex:ActivityNotFoundException){
                        }
                    }
                    else->{}
                }
            }
        }
        else{
            val receiverViewHolder = holder as? ReceiverViewHolder
            if(chat.mimeType != MessageHelper.Companion.Type.MAKE_OFFER){
                receiverViewHolder?.chatTime?.text = DateTimeHelper.TimeAgo.getDate(chat.timeStamp.toString())
            }
            else{
                receiverViewHolder?.negotiationChatTime?.text = DateTimeHelper.TimeAgo.getDate(chat.timeStamp.toString())
            }
            receiverViewHolder?.llMessage?.setGone()
            receiverViewHolder?.clNegotiationLeft?.setGone()
            when(chat.mimeType){
                MessageHelper.Companion.Type.MSG->{
                    receiverViewHolder?.llMessage?.setVisible()
                    receiverViewHolder?.chatText?.text =  chat.message
                    receiverViewHolder?.chatText?.visibility = View.VISIBLE
                    receiverViewHolder?.docLayout?.visibility = View.GONE
                    receiverViewHolder?.productLayout?.visibility = View.GONE
                }
                MessageHelper.Companion.Type.PRODUCT->{
                    val product = ProductConverter.mapFromString(chat.message)
                    receiverViewHolder?.llMessage?.setVisible()
                    receiverViewHolder?.docLayout?.visibility = View.GONE
                    receiverViewHolder?.docLayout?.visibility = View.GONE
                    receiverViewHolder?.chatText?.visibility = View.GONE
                    receiverViewHolder?.productLayout?.visibility = View.VISIBLE
                    product.let {
                        receiverViewHolder?.txtProductName?.text = it.title
                        receiverViewHolder?.txtPrice?.text = it.listPrice?.displayCurrency
                        receiverViewHolder?.txtPrice?.let {
                            it.paintFlags = it.paintFlags or  Paint.STRIKE_THRU_TEXT_FLAG
                        }
                        receiverViewHolder?.txtFinalPrice?.text = it.offerPrice?.displayCurrency
                        receiverViewHolder?.txtOffer?.text = String.format(ctx.getString(R.string.off_data),it.offerPercent.toString())

                        var productImage = Constant.EMPTY
                        if(product.images.count()>0){
                            productImage = product.images[0]
                        }
                        ImageHelper.getInstance().showImage(ctx,productImage,receiverViewHolder?.imgProduct,R.drawable.placeholder_image,R.drawable.placeholder_image)
                    }
                }
                MessageHelper.Companion.Type.MAKE_OFFER->{
                    val product = ProductConverter.mapFromString(chat.makeOffer!!.content)
                    receiverViewHolder?.negotiationIconDeliveryStatus?.setGone()
                    receiverViewHolder?.clNegotiationLeft?.apply {
                        setVisible()
                        setOnClickListener {
                            chatListener?.onclickBuy(product.id)
                        }
                    }
                    ImageHelper.getInstance().showImage(ctx,product.images.getOrElse(0) { AppConstant.EMPTY },receiverViewHolder?.productImage,R.drawable.placeholder_image,R.drawable.placeholder_image)
                    receiverViewHolder?.productName?.text = product.title
                    receiverViewHolder?.productPrice?.text = product.offerPrice?.displayCurrency
                    receiverViewHolder?.requestedOfferValue?.text = chat.makeOffer?.negotiation?.price
                    product.rating?.let {
                        receiverViewHolder?.ratingInfoGroup?.setVisible()
                        receiverViewHolder?.ratingBar?.rating = it.ratingAverage.toFloat()
                        receiverViewHolder?.txtRatingValue?.text = it.ratingAverage.toString()
                        receiverViewHolder?.tvRatingCount?.text = ctx.getString(R.string.chatdetail_ratings,it.ratingCount.toString())
                    }
                    chat.makeOffer?.let {
                        val status = if (it.buyer.userId == userId) it.buyer.offerStatus else it.seller.offerStatus
                        when(status){
                            MessageHelper.Companion.Type.MakeOfferType.SENT_REQUEST->{
                                receiverViewHolder?.btnAccept?.setGone()
                                receiverViewHolder?.btnDecline?.setGone()
                            }
                            MessageHelper.Companion.Type.MakeOfferType.RECEIVED_REQUEST->{
                                receiverViewHolder?.offerRequestedTxt?.text = ctx.getString(R.string.chat_offer)
                                receiverViewHolder?.btnAccept?.setVisible()
                                receiverViewHolder?.btnDecline?.setVisible()
                                receiverViewHolder?.btnAccept?.setOnClickListener {
                                    val seller = Person(userId!!,MessageHelper.Companion.Type.MakeOfferType.SKIP)
                                    val buyer = Person(receiver.id,MessageHelper.Companion.Type.MakeOfferType.READY_TO_BUY)
                                    val makeOffer = MakeOffer(chat.makeOffer!!.content,buyer,seller,chat.makeOffer!!.negotiation)
                                    chatListener?.onClickAccept(getKeyAtPosition(position),product.id,makeOffer)
                                }
                                receiverViewHolder?.btnDecline?.setOnClickListener {
                                    val seller = Person(userId!!,MessageHelper.Companion.Type.MakeOfferType.SKIP)
                                    val buyer = Person(receiver.id,MessageHelper.Companion.Type.MakeOfferType.DENIED_REQUEST)
                                    val makeOffer = MakeOffer(chat.makeOffer!!.content,buyer,seller,chat.makeOffer!!.negotiation)
                                    chatListener?.onClickDecline(getKeyAtPosition(position),product.id,makeOffer)
                                }
                            }
                            MessageHelper.Companion.Type.MakeOfferType.READY_TO_BUY->{
                                receiverViewHolder?.btnAccept?.setVisible()
                                receiverViewHolder?.btnDecline?.setGone()
                                receiverViewHolder?.btnAccept?.text = ctx.getString(R.string.chat_buy_now)
                                receiverViewHolder?.offerRequestedTxt?.text = ctx.getString(R.string.chat_offer)
                                receiverViewHolder?.offerStatus?.apply {
                                    background = Utils.getDrawable(ctx,0,0,R.color.colorGreen,
                                        Utils.getPixel(ctx,16).toFloat())
                                    text = ctx.getString(R.string.chat_accepted)
                                }

                                receiverViewHolder?.btnAccept?.setOnClickListener {
                                    chatListener?.onclickBuy(product.id)
                                }

                                val noOfHours = DateTimeHelper.TimeAgo.getHours(chat.timeStamp as Long).toInt()
                                if (noOfHours>=24){
                                    receiverViewHolder?.offerExpireTime?.setTextColor(ContextCompat.getColor(ctx,R.color.colorRed))
                                    receiverViewHolder?.offerExpireTime?.text =  ctx.getString(R.string.chat_expired)
                                }
                                else{
                                    receiverViewHolder?.offerExpireTime?.setTextColor(ContextCompat.getColor(ctx,R.color.colorGreen))
                                    receiverViewHolder?.offerExpireTime?.text =   ctx.resources.getQuantityString(R.plurals.chat_expires,(24-noOfHours),(24-noOfHours))
                                }
                                receiverViewHolder?.offerExpireTime?.setVisible()
                            }

                             MessageHelper.Companion.Type.MakeOfferType.ACCEPTED_REQUEST->{
                                 receiverViewHolder?.btnAccept?.setGone()
                                 receiverViewHolder?.btnDecline?.setGone()
                                 receiverViewHolder?.offerStatus?.apply {
                                    setVisible()
                                    background = Utils.getDrawable(ctx,0,0,R.color.colorGreen,
                                        Utils.getPixel(ctx,16).toFloat())
                                    text = ctx.getString(R.string.chat_accepted)
                                 }
                                receiverViewHolder?.offerRequestedTxt?.text = ctx.getString(R.string.chat_offer)
                            }

                            MessageHelper.Companion.Type.MakeOfferType.DENIED_REQUEST->{
                                receiverViewHolder?.btnAccept?.setGone()
                                receiverViewHolder?.btnDecline?.setGone()
                                receiverViewHolder?.offerRequestedTxt?.text = ctx.getString(R.string.chat_offer)
                                receiverViewHolder?.offerStatus?.apply {
                                    setVisible()
                                    background = Utils.getDrawable(ctx,0,0,R.color.colorRed,
                                        Utils.getPixel(ctx,16).toFloat())
                                    text = ctx.getString(R.string.chat_declined)
                                }
                            }
                            else ->{}
                        }
                    }
                }
                else->{
                    receiverViewHolder?.llMessage?.setVisible()
                    receiverViewHolder?.chatText?.visibility = View.GONE
                    receiverViewHolder?.docLayout?.visibility = View.VISIBLE
                    receiverViewHolder?.productLayout?.visibility = View.GONE
                    receiverViewHolder?.documentPlaceholder?.setImageResource(FileHelper().getFileImage(chat.mimeType))
                    receiverViewHolder?.documentPlaceholder?.setColorFilter(ContextCompat.getColor(ctx, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN)
                    receiverViewHolder?.fileName?.text = chat.fileName
                }
            }

            receiverViewHolder?.itemView?.setOnClickListener {
                when(chat.mimeType){
                    MessageHelper.Companion.Type.PRODUCT->{
                        val product = ProductConverter.mapFromString(chat.message)
                        val intent = Intent(ctx, ProductDetailActivity::class.java)
                        intent.putExtra("productId",product.id)
                        ctx.startActivity(intent)
                    }
                    MessageHelper.Companion.Type.APPLICATION,
                    MessageHelper.Companion.Type.AUDIO,
                    MessageHelper.Companion.Type.VIDEO,
                    MessageHelper.Companion.Type.IMAGE,
                    MessageHelper.Companion.Type.TEXT->{
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(chat.message)
                        ctx.startActivity(intent)
                    }
                    else->{}
                }
            }
        }
    }

    fun setChatListener(chatThreadListener: ChatThreadListener){
        this.chatListener = chatThreadListener
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).userId == userId) VIEW_TYPE_SENDER else VIEW_TYPE_RECEIVER
    }

    override fun itemAdded(item: Chat?, key: String?, position: Int) {
        recyclerView.scrollToPosition(0)
    }

    override fun updateReadStatus() {
        val chatKeys = keys
        val messages = items
        val map = hashMapOf<String,Any?>()
        for(chatKeyIndex in chatKeys.indices){
            val path = chatKeys[chatKeyIndex]+"/"+MessageHelper.Companion.Keys.DELIVERY_STATUS
            if(!map.containsKey(path)){
                val message = messages[chatKeyIndex]
                if(userId!=null && userId!=message.userId && message.deliveryStatus != MessageHelper.Companion.ChatDeliveryStatus.READ){
                    map[path] = MessageHelper.Companion.ChatDeliveryStatus.READ
                }
            }
        }
        if(map.size>0){
            chatRoomId?.let {id->
                MessageHelper.updateReadStatus(id,map)
            }
        }
    }

    fun detachListener(databaseReference:DatabaseReference) = detachChildListener(databaseReference)

    override fun shouldSkip(chat: Chat): Boolean {
        if (chat.makeOffer?.seller?.userId == userId){
            return chat.makeOffer?.seller?.offerStatus == MessageHelper.Companion.Type.MakeOfferType.SKIP
        }
        return false
    }

}

interface ChatThreadListener{
    fun onClickAccept(messageId:String,productId: String,makeOffer: MakeOffer)
    fun onClickDecline(messageId:String,productId: String,makeOffer: MakeOffer)
    fun onclickBuy(productId:String)
}