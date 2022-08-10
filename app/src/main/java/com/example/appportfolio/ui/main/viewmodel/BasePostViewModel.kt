package com.example.appportfolio.ui.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.responses.getPostResponse
import com.example.appportfolio.data.entities.Post
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.Resource
import com.example.appportfolio.repositories.MainRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BasePostViewModel(
    private val repository: MainRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
): ViewModel() {

    protected val _getPostsResponse=MutableLiveData<Event<Resource<getPostResponse>>>()
    val getPostsResponse: LiveData<Event<Resource<getPostResponse>>> = _getPostsResponse
    private val _getPostResponse=MutableLiveData<Event<Resource<getPostResponse>>>()
    val getPostResponse:LiveData<Event<Resource<getPostResponse>>> = _getPostResponse

    fun getSelectedPost(postid:String, latitude:Double?, longitude:Double?, api: MainApi)
    {
        _getPostResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.getSelectedPost(postid,latitude,longitude,api)
            _getPostResponse.postValue(Event(result))
        }
    }
}