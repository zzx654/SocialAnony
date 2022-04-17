package com.example.appportfolio.ui.main.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.responses.*
import com.example.appportfolio.data.entities.Post
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.Resource
import com.example.appportfolio.repositories.MainRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.http.Field

class interactViewModel  @ViewModelInject constructor(
    private val repository: MainRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
): ViewModel() {
    private val _requestchatResponse= MutableLiveData<Event<Resource<intResponse>>>()
    val requestchatResponse: LiveData<Event<Resource<intResponse>>> = _requestchatResponse

    private val _getprofileResponse= MutableLiveData<Event<Resource<getprofileResponse>>>()
    val getprofileResponse: LiveData<Event<Resource<getprofileResponse>>> = _getprofileResponse

    private val _deletepostResponse= MutableLiveData<Event<Resource<intResponse>>>()
    val deletepostResponse: LiveData<Event<Resource<intResponse>>> = _deletepostResponse

    private val _reportResponse= MutableLiveData<Event<Resource<intResponse>>>()
    val reportResponse: LiveData<Event<Resource<intResponse>>> = _reportResponse

    private val _blockuserResponse= MutableLiveData<Event<Resource<intResponse>>>()
    val blockuserResponse: LiveData<Event<Resource<intResponse>>> = _blockuserResponse

    private val _toggleLikeResponse= MutableLiveData<Event<Resource<togglepostResponse>>>()
    val toggleLikeResponse: LiveData<Event<Resource<togglepostResponse>>> = _toggleLikeResponse

    private val _toggleBookmarkResponse= MutableLiveData<Event<Resource<togglepostResponse>>>()
    val toggleBookmarkResponse: LiveData<Event<Resource<togglepostResponse>>> = _toggleBookmarkResponse

    private val _getPostResponse=MutableLiveData<Event<Resource<getPostResponse>>>()
    val getPostResponse:LiveData<Event<Resource<getPostResponse>>> = _getPostResponse

    private val _getPollResponse=MutableLiveData<Event<Resource<getpolloptionResponse>>>()
    val getPollResponse:LiveData<Event<Resource<getpolloptionResponse>>> = _getPollResponse

    private val _getVoteResultResponse=MutableLiveData<Event<Resource<getvoteresultResponse>>>()
    val getVoteResultResponse:LiveData<Event<Resource<getvoteresultResponse>>> = _getVoteResultResponse

    fun getvoteresult(postid: String,api:MainApi){
        _getVoteResultResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.getVoteResult( postid, api)
            _getVoteResultResponse.postValue(Event(result))
        }
    }

    fun vote(postid:String,optionid:Int,api:MainApi){
        _getVoteResultResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.vote( postid, optionid,api)
            _getVoteResultResponse.postValue(Event(result))
        }
    }
    fun getpolloptions(postid: String,api:MainApi){
        _getPollResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.getpolloptions( postid, api)
            _getPollResponse.postValue(Event(result))
        }
    }
    fun requestchat(userid: Int,roomid:String,api: MainApi) {
        _requestchatResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.requestchat( userid, roomid, api)
            _requestchatResponse.postValue(Event(result))
        }
    }
    fun getuserprofile(userid: Int,api: MainApi) {
        _getprofileResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.getuserprofile(userid, api)
            _getprofileResponse.postValue(Event(result))
        }
    }
    fun deletepost(postid: String,api: MainApi)
    {
        _deletepostResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.deletepost(postid, api)
            _deletepostResponse.postValue(Event(result))
        }
    }
    fun report(postid: String?,commentid:Int?,reporttype:String,api:MainApi)
    {
        _reportResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.report(postid,commentid, reporttype, api)
            _reportResponse.postValue(Event(result))
        }
    }
    fun blockcommentuser(anonymous: Boolean,blockuserid:Int,popback:Boolean,time:String,api:MainApi)
    {
        _blockuserResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.blockcommentuser(anonymous,blockuserid,popback,time,api)
            _blockuserResponse.postValue(Event(result))
        }
    }
    fun blockpostuser(anonymous:Boolean,blockuserid:Int,time:String,api:MainApi){

        _blockuserResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.blockpostuser(anonymous,blockuserid,time,api)
            _blockuserResponse.postValue(Event(result))
        }
    }
    fun toggleLikePost(date:String,togglemy:Boolean,postuserid:Int,postid:String,isLiked:Int,api:MainApi)
    {
        _toggleLikeResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.toggleLikePost(date,togglemy,postuserid,postid,isLiked,api)
            _toggleLikeResponse.postValue(Event(result))
        }
    }
    fun toggleBookmarkPost(postid:String,isMarked:Int,api:MainApi)
    {
        _toggleBookmarkResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.toggleBookmarkPost(postid,isMarked,api)
            _toggleBookmarkResponse.postValue(Event(result))
        }
    }
    fun getSelectedPost(postid:String, latitude:Double?, longitude:Double?, api: MainApi)
    {
        _getPostResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.getSelectedPost(postid,latitude,longitude,api)
            _getPostResponse.postValue(Event(result))
        }


    }


}