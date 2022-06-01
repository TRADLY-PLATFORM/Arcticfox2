-keep class tradly.social.common.base.GenericAdapter
 -keep interface tradly.social.common.base.GenericAdapter$OnClickItemListListener {*;}

-dontwarn androidx.databinding.**
-keep class androidx.databinding.** { *; }
-keep class * extends androidx.databinding.DataBinderMapper { *; }
-keep class tradly.social.common.base.DataBinderMapperImpl.**{ *; }
-keep class tradly.social.common.base.databinding.** { *; }
-keep class tradly.social.event.addevent.databinding.** { *; }
-keep class tradly.social.event.addevent.DataBinderMapperImpl{ *; }
-keep class tradly.social.event.explore.databinding.** { *; }
-keep class tradly.social.event.explore.DataBinderMapperImpl{ *; }
-keep class tradly.social.event.eventbooking.databinding.** { *; }
-keep class tradly.social.event.eventbooking.DataBinderMapperImpl{ *; }
-keep class * extends androidx.databinding.DataBinderMapper { *; }
-keepclassmembers class * extends android.app.Activity {
       public void on*Click(android.view.View);
}
-ignorewarnings