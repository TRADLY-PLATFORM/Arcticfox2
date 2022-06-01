package tradly.social.ui.map

import tradly.social.R
import tradly.social.common.base.NetworkUtil
import tradly.social.data.model.dataSource.ParseShippingAddressDataSource
import tradly.social.domain.entities.Address
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.GeoPoint
import tradly.social.domain.entities.Result
import tradly.social.domain.repository.ShippingAddressRepository
import tradly.social.domain.usecases.ManageShippingAddress
import kotlinx.coroutines.*
import tradly.social.domain.usecases.GetAddressBySearch
import kotlin.coroutines.CoroutineContext

class MapPresenter(var view: View?) : CoroutineScope {

    private var job: Job
    private var manageShippingAddress: ManageShippingAddress? = null
    private var getAddressBySearch:GetAddressBySearch

    interface View {
        fun showProgressLoader()
        fun hideProgressLoader()
        fun onFailure(appError: AppError)
        fun networkError(msg:Int)
        fun showAddress(address: Address)
        fun showAddresses(addressList:List<Address>)
    }


    init {
        val shippingAddressDataSource = ParseShippingAddressDataSource()
        val shippingAddressRepository = ShippingAddressRepository(shippingAddressDataSource)
        manageShippingAddress = ManageShippingAddress(shippingAddressRepository)
        getAddressBySearch = GetAddressBySearch(shippingAddressRepository)

        job = Job()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    fun getAddress(geoPoint: GeoPoint){
        if(NetworkUtil.isConnectingToInternet()){
            view?.showProgressLoader()
           launch(Dispatchers.Main){
               val call = async(Dispatchers.IO){ manageShippingAddress?.getCurrentAddressLocation(geoPoint) }
               when(val result = call.await()){
                   is Result.Success->{view?.showAddress(result.data.also { it.geoPoint = geoPoint })}
                   is Result.Error->view?.onFailure(result.exception)
               }
               view?.hideProgressLoader()
           }
        }
        else{
            view?.networkError(R.string.no_internet)
        }
    }

    fun search(key:String){
        synchronized(this){
            if(NetworkUtil.isConnectingToInternet(true)){
                view?.showProgressLoader()
                job = CoroutineScope(Dispatchers.Main).launch {
                    delay(1000)
                    val call = async(Dispatchers.IO){ getAddressBySearch.search(key) }
                    when(val result = call.await()){
                        is Result.Success->view?.showAddresses(result.data)
                        is Result.Error->view?.onFailure(result.exception)
                    }
                    view?.hideProgressLoader()
                }
            }
        }
    }

    fun cancelSearch() = job.cancel()

    fun onDestroy(){
        view = null
        job.cancel()
    }
}