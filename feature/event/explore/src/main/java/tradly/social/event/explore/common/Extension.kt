package tradly.social.event.explore.common

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import tradly.social.domain.entities.GeoPoint

fun Int.toBitmap(context: Context, @ColorRes tintColor: Int? = null): Bitmap {

    // retrieve the actual drawable
    val drawable = ContextCompat.getDrawable(context, this) ?: throw Resources.NotFoundException("Resource Not found")
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    val bm = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)

    // add the tint if it exists
    tintColor?.let {
        DrawableCompat.setTint(drawable, ContextCompat.getColor(context, it))
    }
    // draw it onto the bitmap
    val canvas = Canvas(bm)
    drawable.draw(canvas)
    return bm
}

fun GoogleMap?.addMarker(position:GeoPoint,icon:Bitmap?,info:Pair<String,String>, tag:Any?,isDraggable:Boolean = false):Marker?{
    if (this!=null){
        val (title,snippet) = info
        val markerOptions = MarkerOptions().apply {
            position(LatLng(position.latitude,position.longitude))
            if (title.isNotEmpty()){
                title(title)
            }
            if (snippet.isNotEmpty()){
                snippet(snippet)
            }
            if (icon!=null){
                icon(BitmapDescriptorFactory.fromBitmap(icon))
            }
            else{
                icon(BitmapDescriptorFactory.defaultMarker())
            }
            draggable(isDraggable)
        }
        val marker = this.addMarker(markerOptions)
        if (tag!=null){
            marker?.tag = tag
        }
        return marker
    }
    return null
}