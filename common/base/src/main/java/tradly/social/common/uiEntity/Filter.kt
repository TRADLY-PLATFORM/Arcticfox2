package tradly.social.common.uiEntity

import androidx.databinding.BaseObservable
import tradly.social.domain.entities.Filter

class Filter(override var isSelected: Boolean = false) :Filter(),BaseSelection