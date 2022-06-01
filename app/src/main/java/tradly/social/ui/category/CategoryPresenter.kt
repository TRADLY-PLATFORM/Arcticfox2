package tradly.social.ui.category

import tradly.social.common.base.NetworkUtil
import tradly.social.common.network.feature.common.datasource.CategoryDataSourceImpl
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Category
import tradly.social.domain.entities.Constant
import tradly.social.domain.entities.Result
import tradly.social.domain.repository.CategoryRepository
import tradly.social.domain.usecases.GetCategories
import kotlinx.coroutines.*
import tradly.social.common.base.AppConstant
import kotlin.coroutines.CoroutineContext

class
CategoryPresenter(var view:View?) : CoroutineScope {
    private var job: Job
    private var getCategories: GetCategories? = null
    interface View{
        fun onLoadCategories(list: List<Category>)
        fun showProgressLoader()
        fun hideProgressLoader()
        fun onFailure(appError: AppError)
        fun noNetwork()
    }

    init {
        val parseCategoryDataSource = CategoryDataSourceImpl()
        val categoryRepository = CategoryRepository(parseCategoryDataSource)
        getCategories = GetCategories(categoryRepository)
        job = Job()
    }
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    fun getCategories(categoryId:String= Constant.EMPTY){
        if(NetworkUtil.isConnectingToInternet()){
            view?.showProgressLoader()
            launch(Dispatchers.Main) {
                val call = async(Dispatchers.IO) { getCategories?.getCategories(categoryId,
                    AppConstant.CategoryType.LISTINGS) }
                when(val result = call.await()){
                    is Result.Success->{view?.onLoadCategories(result.data)}
                    is Result.Error->{view?.onFailure(result.exception)}
                }
                view?.hideProgressLoader()
            }
        }
    }

    fun onDestroy(){
        job.cancel()
        view =null
    }
}