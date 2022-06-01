package tradly

import org.gradle.api.JavaVersion

/*Use this DSL file to configure SDK Keys and app version*/

object Config {
    const val minSDK = 21
    const val targetSDK = 30
    const val compileSDK = 30
    const val enableMultiDex = true
    const val useSupportLibrary = true
    const val jvmTarget = "1.8"
    const val MO_ENGAGE = ""
    const val sentryDSN = ""
    val sourceCompatibility = JavaVersion.VERSION_1_8
    val targetCompatibility = JavaVersion.VERSION_1_8
    const val ADMIN_KEY = ""

    object Version {

        object TradlySocial {
            const val VERSION_CODE = 7
            const val VERSION_NAME = "1.6"

        }
    }

    object BaseUrl {
        const val PRODUCTION = "https://api.tradly.app/"
        const val SANDBOX = "https://api.sandbox.tradly.app/"
        const val DEBUG = "https://api.dev.tradly.app/"
    }

    object Package {
        const val TRADLY_SOCIAL = "tradly.social"
    }

    object BuildTypes {
        const val enableReleaseDebug = false

        object Release {
            const val MINIFY_ENABLED = true
            const val USE_PROGUARD = true
            const val SHRINK_RES = false
            const val CRUNCH_PNGS = false
        }

        object Debug {
            const val MINIFY_ENABLED = false
            const val USE_PROGUARD = false
            const val SHRINK_RES = false
            const val CRUNCH_PNGS = false
        }
    }

    object Tenant {
        const val TRADLY_SOCIAL = "tradlysocial"
    }
}