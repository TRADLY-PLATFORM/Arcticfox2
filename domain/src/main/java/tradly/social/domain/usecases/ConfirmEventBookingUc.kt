package tradly.social.domain.usecases

import tradly.social.domain.repository.EventBookingRepository

class ConfirmEventBookingUc(private val eventBookingRepository: EventBookingRepository) {
    suspend fun confirmBooking(listingId:String,variantId:Int,paymentMethodId:Int,quantity:Int,type:String) = eventBookingRepository.confirmBooking(listingId,variantId, paymentMethodId, quantity, type)
}