package tradly.social.domain.entities

open class Filter(
    var filterName:String = Constant.EMPTY,
    var filterValue:ArrayList<FilterValue> = arrayListOf(),
    var categoryValues:List<Category> = arrayListOf(),
    var viewType:Int = 0,
    var subViewType:String = Constant.EMPTY,
    var minValue:Int = 0,
    var maxValue:Int = 0,
    var steps:Int = 0,
    var unit:String = Constant.EMPTY,
    var queryKey:String = Constant.EMPTY,
    var selectedValue:String = Constant.EMPTY
)

data class FilterValue(
    var filterId: String = Constant.EMPTY,
    var filterName:String = Constant.EMPTY,
    var active:Boolean = false,
    var values:List<FilterValue> = emptyList()
)