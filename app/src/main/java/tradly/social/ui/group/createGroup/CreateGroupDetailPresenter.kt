package tradly.social.ui.group.createGroup

import tradly.social.R
import tradly.social.common.base.FileHelper
import tradly.social.common.base.NetworkUtil
import tradly.social.common.base.Utils
import tradly.social.data.model.dataSource.ParseGroupDataSource
import tradly.social.common.network.feature.common.datasource.ParseMediaDataSource
import tradly.social.domain.entities.*
import tradly.social.domain.repository.GroupRepository
import tradly.social.domain.repository.MediaRepository
import tradly.social.domain.usecases.AddGroup
import tradly.social.domain.usecases.GetGroups
import tradly.social.domain.usecases.UploadMedia
import kotlinx.coroutines.*
import tradly.social.common.network.CustomError
import java.io.File
import kotlin.coroutines.CoroutineContext

class CreateGroupDetailPresenter(var view: View?) : CoroutineScope {
    interface View {
        fun onSuccess()
        fun onFailure(appError: AppError)
        fun onGroupTypeLoaded(list: List<GroupType>)
        fun groupTypeLoadFailed()
        fun showProgressDialog()
        fun hideProgressDialog()
        fun fieldError(id: Int, msg: Int)
    }

    private var job: Job
    private var getGroups: GetGroups? = null
    private var addGroup: AddGroup? = null
    private var uploadMedia: UploadMedia? = null

    init {
        val parseGroupDataSource = ParseGroupDataSource()
        val groupRepository = GroupRepository(parseGroupDataSource)
        val mediaDataSource = ParseMediaDataSource()
        val mediaRepository = MediaRepository(mediaDataSource)
        uploadMedia = UploadMedia(mediaRepository)
        addGroup = AddGroup(groupRepository)
        getGroups = GetGroups(groupRepository)
        job = Job()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    fun addGroup(
        grpName: String?,
        grpDesc: String?,
        address:Address?,
        selectedGroupType: GroupType?,
        selectedImage: String?
    ) {
        if (NetworkUtil.isConnectingToInternet()) {
            if (validate(grpName, grpDesc,address, selectedGroupType, selectedImage)) {
                view?.showProgressDialog()
                launch(Dispatchers.Main) {
                    val fileInfo = FileInfo(
                        fileUri = selectedImage,
                        type = FileHelper.getMimeType(selectedImage),
                        name = File(selectedImage).name
                    )
                    val uploadCall =
                        async(Dispatchers.IO) { uploadMedia?.invoke(listOf(fileInfo), false) }
                    when (val uploadResult = uploadCall.await()) {
                        is Result.Success -> {
                            if (uploadResult.data.count() > 0) {
                                val group = formGroup(
                                    grpName,
                                    grpDesc,
                                    address,
                                    selectedGroupType,
                                    uploadResult.data[0].uploadedUrl
                                )
                                val call = async(Dispatchers.IO) { addGroup?.addGroup(group) }
                                when (val result = call.await()) {
                                    is Result.Success -> {
                                        view?.onSuccess()
                                    }
                                    is Result.Error -> {
                                        view?.onFailure(result.exception)
                                    }
                                }
                            }
                        }
                        is Result.Error -> {
                            view?.onFailure(AppError(code = CustomError.ADD_GROUP_ERROR))
                        }
                    }

                    view?.hideProgressDialog()
                }
            }
        }

    }

    private fun formGroup(
        grpName: String?,
        grpDesc: String?,
        address: Address?,
        selectedGroupType: GroupType?,
        selectedImage: String?
    ): Group {
        val group = Group()
        group.apply {
            this.groupPic = selectedImage
            this.groupName = grpName
            this.groupAddress = address?.formattedAddress
            this.groupDescription = grpDesc
            this.groupType = selectedGroupType?.id?.toInt()
            this.location = address?.geoPoint
            this.membersCount = 0
        }
        return group
    }

    fun getGroupTypes() {
        if (NetworkUtil.isConnectingToInternet()) {
            view?.showProgressDialog()
            launch(Dispatchers.Main) {
                val call = async(Dispatchers.IO) { getGroups?.getGroupTypes() }
                when (val result = call.await()) {
                    is Result.Success -> {
                        view?.onGroupTypeLoaded(result.data)
                    }
                    is Result.Error -> {
                        view?.groupTypeLoadFailed()
                    }
                }
                view?.hideProgressDialog()
            }
        }
    }


    private fun validate(
        grpName: String?,
        grpDesc: String?,
        address: Address?,
        selectedGroupType: GroupType?,
        selectedImage: String?
    ): Boolean {
        var isValid = true
        if (grpName.isNullOrEmpty()) {
            isValid = false
            view?.fieldError(R.id.edGroupName, R.string.group_required)
        } else if (grpDesc.isNullOrEmpty()) {
            isValid = false
            view?.fieldError(R.id.edGroupDesc, R.string.group_required)
        }
        else if(!Utils.isValidUrl(grpDesc)){
            isValid = false
            view?.fieldError(R.id.edGroupDesc, R.string.group_invalid_email)
        }
        else if (selectedGroupType == null) {
            isValid = false
            view?.fieldError(R.id.recycler_view, R.string.group_required)
        } else if (selectedImage.isNullOrEmpty()) {
            isValid = false
            view?.fieldError(R.id.selectedImage, R.string.group_required)
        }
        else if(address ==null){
            isValid = false
            view?.fieldError(R.id.txtLocation,R.string.addstore_alert_please_select_address)
        }
        return isValid

    }
}