package com.example.appportfolio.ui.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.responses.getPostResponse
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.Resource
import com.example.appportfolio.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class nearPostViewModel @Inject constructor(
    private val repository: MainRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
):BasePostViewModel(repository, dispatcher) {

    private val _getPostsResponse= MutableLiveData<Event<Resource<getPostResponse>>>()

    override val getPostsResponse: LiveData<Event<Resource<getPostResponse>>>
        get() = _getPostsResponse

    fun getNearPosts(lastpostnum:Int?,lastpostdate:String?,distancemax:Int,latitude:Double,longitude:Double,api: MainApi)
    {
        _getPostsResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.getNearPosts(lastpostnum,lastpostdate,distancemax,latitude,longitude,api)
            _getPostsResponse.postValue(Event(result))
        }
    }

}