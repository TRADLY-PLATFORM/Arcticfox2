package tradly.social.common.network.converters

import tradly.social.common.network.feature.eventbooking.model.BookingDetailEntity
import tradly.social.domain.entities.EventBooking

object EventBookingConverter {

    fun mapFrom(list: List<BookingDetailEntity>) = list.map { mapFrom(it) }

    fun mapFrom(bookingDetailEntity: BookingDetailEntity) =
        EventBooking(
            id = bookingDetailEntity.id,
            referenceNo = bookingDetailEntity.referenceNumber,
            offerPrice = ProductConverter.mapFrom(bookingDetailEntity.offerTotal),
            price = ProductConverter.mapFrom(bookingDetailEntity.grandTotal),
            store = StoreModelConverter.mapFrom(bookingDetailEntity.account),
            tickets = bookingDetailEntity.bookingEvents[0].quantity,
            event = EventConverter.mapFrom(bookingDetailEntity.bookingEvents[0].listing)
        )

}