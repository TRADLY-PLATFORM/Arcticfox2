# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class storeName to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file storeName.
#-renamesourcefileattribute SourceFile

#coroutines
# ServiceLoader support
        -keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}


#ucrop library
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }

#Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

#Parse Sdk
-keepattributes *Annotation*
-keep class com.parse.** { *; }

#Model class
-keep class tradly.social.common.network.entity.**{*;}
-keep class tradly.social.viewEntity.**{*;}
-keep class tradly.social.domain.entities.**{*;}
-keep class tradly.social.common.CircleView
-keep class tradly.social.ui.main.MainActivity
-keep class tradly.social.event.explore.ui.EventExploreActivity
-keep class tradly.social.event.addevent.ui.variant.addTime.EventTimeHostActivity
-keep class tradly.social.event.addevent.ui.variant.addVariant.VariantHostActivity
-keep public class * extends android.app.Activity

-keepclassmembers class * extends android.app.Activity {
     public void *(android.view.View);
 }

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



#Retrofit

# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

#Okhttp 3

# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform

-keep class org.xmlpull.v1.** { *; }

#Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-ignorewarnings
-optimizations !class/unboxing/enum


