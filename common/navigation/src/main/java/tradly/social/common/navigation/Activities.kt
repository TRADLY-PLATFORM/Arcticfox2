package tradly.social.common.navigation

import android.content.Intent
import android.os.Bundle

internal const val BASE_PACKAGE = "tradly.social"




interface AddressableActivity {
    /**
     * The activity class name.
     */
    val className: String
}

object Activities {

    object MainActivity:AddressableActivity{
         override val className = "$BASE_PACKAGE.ui.main.MainActivity"
     }

    object EventExploreActivity:AddressableActivity{
        override val className = "$BASE_PACKAGE.event.explore.ui.EventExploreActivity"
    }

    object EventTimeHostActivity:AddressableActivity{
        const val EXTRAS_FROM_MILLIS = "fromMillis"
        const val EXTRAS_TO_MILLIS = "toMillis"
        override val className = "$BASE_PACKAGE.event.addevent.ui.variant.addTime.EventTimeHostActivity"
    }

    object BookingHostActivity : AddressableActivity {
        const val EXTRAS_EVENT = "event"
        const val EXTRAS_HOST_FRAGMENT_ID = "hostFragmentId"
        object HostFragment {
            const val BOOKING_CONFIRM = 1
            const val BOOKING_LIST = 2
        }
        override val className = "$BASE_PACKAGE.event.eventbooking.BookingHostActivity"
    }

    object SearchActivity:AddressableActivity{
        override val className = "$BASE_PACKAGE.search.ui.SearchActivity"
    }

    object VariantHostActivity:AddressableActivity{
        const val EXTRAS_LISTING_ID = "listingId"
        const val EXTRAS_VARIANT = "variant"
        const val EXTRAS_IS_FOR_ADD = "isForAdd"
        const val EXTRAS_CRUD_ACTION = "crudAction"
        const val EXTRAS_VARIANT_LIST = "variantList"
        const val EXTRAS_VARIANT_TYPE_LIST = "variantTypeList"
        const val EXTRAS_CURRENCY = "currency"
        object CRUD_ACTION{
            const val ADD = 1
            const val DELETE = 2
            const val EDIT = 3
        }
        override val className = "$BASE_PACKAGE.event.addevent.ui.variant.addVariant.VariantHostActivity"
    }

    fun getIntent(addressableActivity: AddressableActivity,bundle: Bundle? = null) =
        Intent(Intent.ACTION_VIEW).setClassName(BuildConfig.APP_ID,addressableActivity.className).apply {
        bundle?.let {
            this.putExtras(bundle)
        }
    }
}