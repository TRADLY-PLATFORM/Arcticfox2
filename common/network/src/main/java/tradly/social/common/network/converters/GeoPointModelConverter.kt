package tradly.social.common.network.converters

import tradly.social.domain.entities.GeoPoint
import com.parse.ParseGeoPoint

object GeoPointModelConverter:BaseConverter() {
    fun mapFrom(from: ParseGeoPoint?): GeoPoint? {
        from?.run {
            return GeoPoint(
                from.latitude,
                from.longitude
            )
        }
        return GeoPoint()
    }

    fun mapFrom(geoPoint: GeoPoint?): ParseGeoPoint? =
        if (geoPoint != null) {
            ParseGeoPoint().apply {
                latitude = geoPoint.latitude
                longitude = geoPoint.longitude
            }
        } else {
            null
        }
}