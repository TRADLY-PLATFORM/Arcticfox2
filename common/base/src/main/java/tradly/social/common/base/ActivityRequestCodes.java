package tradly.social.common.base;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class ActivityRequestCodes {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            ActivityRequestCode.VariantHostActivity,
            ActivityRequestCode.EventTimeHostActivity,
            ActivityRequestCode.ProductDetailActivity,
            ActivityRequestCode.REQUEST_CODE_GOOGLE_SIGN_IN
    })
    public @interface Code {
    }
}
