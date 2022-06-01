package tradly.social.common.base

class ReviewHelper {
    companion object{
        fun getReviewTitle(title: Int):String{
            with(AppController.appController){
                return when(title){
                    1-> getString(R.string.addreview_opinion_one)
                    2-> getString(R.string.addreview_opinion_two)
                    3-> getString(R.string.addreview_opinion_three)
                    4-> getString(R.string.addreview_opinion_four)
                    else->""
                }
            }
        }
    }
}