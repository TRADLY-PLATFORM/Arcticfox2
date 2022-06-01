package tradly
object Dependencies {

    const val appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    const val legacy_support_v4 = "androidx.legacy:legacy-support-v4:${Versions.legacy_support_v4}"
    const val core_ktx = "androidx.core:core-ktx:${Versions.core_ktx}"
    const val android_material_design = "com.google.android.material:material:${Versions.android_material_design}"
    const val androidx_constraint_layout = "androidx.constraintlayout:constraintlayout:${Versions.androidx_constraint_layout}"
    const val android_support_multidex = "com.android.support:multidex:${Versions.android_support_multidex}"

    object Navigation{
        const val navigation_fragment = "androidx.navigation:navigation-fragment-ktx:${Versions.navigation_fragment}"
        const val navigation_ui = "androidx.navigation:navigation-ui-ktx:${Versions.navigation_ui}"
    }

    object Billing{
        const val google_play_billing = "com.android.billingclient:billing-ktx:${Versions.google_play_billing}"
    }

    object ViewModel{
        const val view_model = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.view_model}"
    }

    object LiveData{
        const val livedata = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.livedata}"
    }

    object Room{
        const val room_runtime = "androidx.room:room-runtime:${Versions.room}"
        const val room_compiler = "androidx.room:room-compiler:${Versions.room}"
        const val room_ktx = "androidx.room:room-ktx:${Versions.room}"
    }

    const val viewpager_dots = "com.afollestad:viewpagerdots:${Versions.viewpager_dots}"
    const val support_annotation = "com.android.support:support-annotations:${Versions.support_annotation}"


    object ImageView{
        const val shape_imageview = "com.github.siyamed:android-shape-imageview:${Versions.shape_imageview}"
        const val circular_imageview = "de.hdodenhof:circleimageview:${Versions.circular_imageview}"
        const val photoView = "com.github.chrisbanes:PhotoView:${Versions.photoView}"
    }

    object Payment{
        const val stripe = "com.stripe:stripe-android:${Versions.stripe}"
    }

    object Facebook{
        const val facebook_android_sdk = "com.facebook.android:facebook-android-sdk:${Versions.facebook_android_sdk}"
        const val facebook_share = "com.facebook.android:facebook-share:${Versions.facebook_share}"
    }

    object Analytics{
        const val firebase = "com.google.firebase:firebase-analytics:${Versions.firebase_analytics}"
        const val moengage = "com.moengage:moe-android-sdk:${Versions.moengage}"
        const val io_branch = "io.branch.sdk.android:library:${Versions.io_branch_sdk}"
        const val roll_bar = "com.rollbar:rollbar-android:${Versions.roll_bar}"
    }

    object Crashlytics{
        const val firebase = "com.google.firebase:firebase-crashlytics:${Versions.firebase_crashlytics}"
        const val sentry = "io.sentry:sentry-android:${Versions.sentry_android}"
    }

    object PlayServices{
        const val maps = "com.google.android.gms:play-services-maps:${Versions.gms_play_services_maps}"
        const val locations = "com.google.android.gms:play-services-location:${Versions.gms_play_services_locations}"
        const val auth = "com.google.android.gms:play-services-auth:${Versions.gms_play_services_auth}"
        const val ad_mob = "com.google.android.gms:play-services-ads:${Versions.gms_ad_mob}"

    }

    object CropCompress{
        const val uCrop = "com.github.yalantis:ucrop:${Versions.uCrop}"
        const val compressor = "id.zelory:compressor:${Versions.compressor}"
    }

    object MaterialChips{
        const val material_chips_input = "com.github.pchmn:MaterialChipsInput:1.0.8"
        const val nachos = "com.hootsuite.android:nachos:${Versions.nachos}"
        const val chips_input_layout = "com.github.tylersuehr7:chips-input-layout:${Versions.chips_input_layout}"
    }

    object Crypto{
        const val scytale = "com.yakivmospan:scytale:${Versions.scytale}"
    }

    object Firebase{
        const val firebase_auth = "com.google.firebase:firebase-auth:${Versions.firebase_auth}"
        const val firebase_messaging = "com.google.firebase:firebase-messaging:${Versions.firebase_messaging}"
        const val firebase_database = "com.google.firebase:firebase-database-ktx:${Versions.firebase_database}"
    }

    object Kotlin{
        const val kotlinx_coroutines_play_services = "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:${Versions.kotlinx_coroutines_play_services}"
        const val kotlin_stdlib_jdk7 = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin_stdlib_jdk7}"
    }

    object Retrofit{
      const val square_retrofit2 = "com.squareup.retrofit2:retrofit:${Versions.squareup_retrofit2}"
      const val square_retrofit2_converter_gson = "com.squareup.retrofit2:converter-gson:${Versions.squareup_retrofit2}"
      const val retrofit2_kotlin_coroutines_adapter = "com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:${Versions.retrofit2_kotlin_coroutines_adapter}"
    }

    object Moshi{
        const val square_moshi = "com.squareup.moshi:moshi:${Versions.square_moshi}"
        const val moshi_kotlin = "com.squareup.moshi:moshi-kotlin:${Versions.moshi_kotlin}"
        const val moshi_kotlin_codegen = "com.squareup.moshi:moshi-kotlin-codegen:${Versions.moshi_kotlin}"
        const val square_retrofit2_converter_moshi = "com.squareup.retrofit2:converter-moshi:${Versions.square_retrofit2_converter_moshi}"
    }

    object OkHttp3{
        const val okhttp3_logging_interceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp3_logging_interceptor}"
    }

    object Coroutine{
        const val kotlinx_coroutines_android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinx_coroutine_android}"
        const val kotlinx_coroutines_core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinx_coroutines_core}"
    }

    object ArchLifeCycle{
        const val arch_lifecycle_compiler = "android.arch.lifecycle:compiler:${Versions.arch_lifecycle_compiler}"   //kapt
        const val androidx_lifecycle_extension = "androidx.lifecycle:lifecycle-extensions:${Versions.androidx_lifecycle_extension}"
    }

    object Glide{
        const val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
        const val glide_compiler = "com.github.bumptech.glide:compiler:${Versions.glide_compiler}"
    }

    object Calender{
        const val horZCalender = "com.michalsvec:single-row-calednar:${Versions.horZCalender}"
    }

    object Test{
        const val junit = "junit:junit:${Versions.junit}"
        const val ext_junit = "androidx.test.ext:junit:${Versions.ext_junit}"
        const val espresso_core = "androidx.test.espresso:espresso-core:${Versions.espresso_core}"
    }
}