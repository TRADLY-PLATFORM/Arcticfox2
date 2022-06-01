package tradly.social.ui.tags

import tradly.social.common.base.NetworkUtil
import tradly.social.data.model.dataSource.ParseTagDataSource
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Result
import tradly.social.domain.entities.Tag
import tradly.social.domain.repository.TagRepository
import tradly.social.domain.usecases.SearchTags
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class TagPresenter(var view: View?): CoroutineScope {
    private var job: Job
    private var searchTags: SearchTags?=null
    interface View {
        fun showProgress()
        fun hideProgress()
        fun showTags(list: List<Tag>, keyword:String)
        fun networkError()
        fun onFailure(appError: AppError)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    init {
        val parseTagDataSource = ParseTagDataSource()
        val tagRepository = TagRepository(parseTagDataSource)
        searchTags = SearchTags(tagRepository)
        job = Job()
    }

    fun searchTags(keyWord:String){
        if(NetworkUtil.isConnectingToInternet()){
            launch(Dispatchers.Main) {
                view?.showProgress()
                val call = async (Dispatchers.IO){ searchTags?.searchTag(keyWord) }
                when(val result = call.await()){
                    is Result.Success->{view?.showTags(result.data,keyWord)}
                    is Result.Error->view?.onFailure(result.exception)
                }
                view?.hideProgress()
            }
        }
    }

    fun cancelRequest() {
        job.cancel()
    }
}