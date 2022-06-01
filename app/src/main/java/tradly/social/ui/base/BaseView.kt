package tradly.social.ui.base

import tradly.social.domain.entities.Constant

interface BaseView {
    fun showProgressDialog(msg:Int)
    fun hideProgressDialog()
}

interface FieldView{
    fun inputError(id: Int, msg: Int)
}

interface StripeView:BaseView{
    fun onCheckOutStatus(orderReference: String= Constant.EMPTY, paymentTypeId:Int=0, paymentType:String= Constant.EMPTY, channel:String= Constant.EMPTY, isSuccess:Boolean=false)

}