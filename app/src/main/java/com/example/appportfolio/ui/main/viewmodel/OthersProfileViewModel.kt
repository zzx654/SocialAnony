package com.example.appportfolio.ui.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.responses.getPostResponse
import com.example.appportfolio.api.responses.getprofileResponse
import com.example.appportfolio.api.responses.intResponse
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.Resource
import com.example.appportfolio.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OthersProfileViewModel@Inject constructor(private val repository: MainRepository,
                                                private val dispatcher: CoroutineDispatcher = Dispatchers.Main
):BasePostViewModel(repository, dispatcher) {
    private val _requestchatResponse= MutableLiveData<Event<Resource<intResponse>>>()
    val requestchatResponse: LiveData<Event<Resource<intResponse>>> = _requestchatResponse
    private val _getprofileResponse= MutableLiveData<Event<Resource<getprofileResponse>>>()
    val getprofileResponse: LiveData<Event<Resource<getprofileResponse>>> = _getprofileResponse
    private val _getPostsResponse= MutableLiveData<Event<Resource<getPostResponse>>>()
    override val getPostsResponse: LiveData<Event<Resource<getPostResponse>>>
        get() = _getPostsResponse


    fun getuserPosts(userid:Int,lastpostnum:Int?,lastpostdate: String?,latitude: Double?,longitude: Double?,api: MainApi)
    {
        _getPostsResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.getuserPosts(userid,lastpostnum,lastpostdate,latitude,longitude,api)
            _getPostsResponse.postValue(Event(result))
        }

    }
    fun requestchat(userid: Int,roomid:String,api: MainApi) {
        _requestchatResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.requestchat( userid, roomid, api)
            _requestchatResponse.postValue(Event(result))
        }
    }
    fun getuserProfile(userid: Int,api: MainApi) {
        _getprofileResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.getuserprofile( userid, api)
            _getprofileResponse.postValue(Event(result))
        }
    }

}