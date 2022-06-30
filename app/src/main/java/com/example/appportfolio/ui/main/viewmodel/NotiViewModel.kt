package com.example.appportfolio.ui.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.responses.*
import com.example.appportfolio.data.entities.Noti
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.Resource
import com.example.appportfolio.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotiViewModel @Inject constructor(private val mainRepository: MainRepository,
                                        private val dispatcher: CoroutineDispatcher = Dispatchers.Main
): ViewModel() {


    private val _curnotis= MutableLiveData<List<Noti>>()
    val curnotis: LiveData<List<Noti>> = _curnotis

    private val _selectedNoti= MutableLiveData<Noti>()
    val selectedNoti: LiveData<Noti> = _selectedNoti

    private val _getNotisResponse=MutableLiveData<Event<Resource<getNotiResponse>>>()
    val getNotiResponse:LiveData<Event<Resource<getNotiResponse>>> = _getNotisResponse

    private val _getNewNotisResponse=MutableLiveData<Event<Resource<getNotiResponse>>>()
    val getNewNotiResponse:LiveData<Event<Resource<getNotiResponse>>> = _getNewNotisResponse

    private val _checkSelectedCommentResponse=MutableLiveData<Event<Resource<commentResponse>>>()
    val checkSelectedCommentResponse:LiveData<Event<Resource<commentResponse>>> = _checkSelectedCommentResponse

    private val _checkNotiUnreadResponse=MutableLiveData<Event<Resource<intResponse>>>()
    val checkNotiUnreadResponse:LiveData<Event<Resource<intResponse>>> = _checkNotiUnreadResponse

    private val _readNotiResponse=MutableLiveData<Event<Resource<intResponse>>>()
    val readNotiResponse:LiveData<Event<Resource<intResponse>>> = _readNotiResponse

    private val _readAllNotiResponse=MutableLiveData<Event<Resource<intResponse>>>()
    val readAllNotiResponse:LiveData<Event<Resource<intResponse>>> = _readAllNotiResponse

    private val _deleteAllNotiResponse=MutableLiveData<Event<Resource<intResponse>>>()
    val deleteAllNotiResponse:LiveData<Event<Resource<intResponse>>> = _deleteAllNotiResponse

    private val _getPostResponse=MutableLiveData<Event<Resource<getPostResponse>>>()
    val getPostResponse:LiveData<Event<Resource<getPostResponse>>> = _getPostResponse
    private val _checkuserResponse= MutableLiveData<Event<Resource<checkUserResponse>>>()
    val checkuserResponse: LiveData<Event<Resource<checkUserResponse>>> = _checkuserResponse
    init{
        _curnotis.postValue(listOf())
    }
    fun checkuser(userid: Int,api: MainApi){
        _checkuserResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=mainRepository.checkuser(userid,api)
            _checkuserResponse.postValue(Event(result))
        }
    }
    fun setNotis(noti:List<Noti>)
    {
        _curnotis.postValue(noti)
    }

    fun readAllNoti(api:MainApi)
    {
        _readAllNotiResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=mainRepository.readAllNoti( api)
            _readAllNotiResponse.postValue(Event(result))
        }
    }

    fun deleteAllNoti(api:MainApi)
    {
        _deleteAllNotiResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=mainRepository.deleteAllNoti(api)
            _deleteAllNotiResponse.postValue(Event(result))
        }
    }


    fun getSelectedPost(postid:String, latitude:Double?, longitude:Double?, api: MainApi)
    {
        _getPostResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=mainRepository.getSelectedPost(postid,latitude,longitude,api)
            _getPostResponse.postValue(Event(result))
        }
    }

    fun checkSelectedComment(commentuserid:Int?,postuserid:Int?,commentid:Int,postid: String,api:MainApi)
    {
        _checkSelectedCommentResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=mainRepository.checkSelectedComment(commentuserid,postuserid,commentid,postid, api)
            _checkSelectedCommentResponse.postValue(Event(result))
        }
    }
    fun setSelectedNoti(
        noti:Noti
    ){
        _selectedNoti.postValue(noti)
    }
    fun readNoti(
        notiid: Int,
        api: MainApi
    ){
        _readNotiResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch {
            val result=mainRepository.readNoti(notiid,api)
            _readNotiResponse.postValue(Event(result))
        }
    }
    fun checkNotiUnread(
        api:MainApi
    ){
        _checkNotiUnreadResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch {
            val result=mainRepository.checkNotiUnread(api)
            _checkNotiUnreadResponse.postValue(Event(result))
        }
    }
    fun getNotis(
        notiid:Int?,
        date:String?,
        api: MainApi
    ){
        _getNotisResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch {
            val result=mainRepository.getNotis(notiid,date, api)
            _getNotisResponse.postValue(Event(result))
        }
    }
    fun getNewNotis(
        api: MainApi
    ){
        _getNewNotisResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch {
            val result=mainRepository.getNotis(null,null, api)
            _getNewNotisResponse.postValue(Event(result))
        }
    }


}