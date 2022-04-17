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
    var posts:MutableList<Post> = mutableListOf()

    private val _beforesize=MutableLiveData<Int>()
    val beforesize:LiveData<Int> = _beforesize
    private  val _curposts= MutableLiveData<List<Post>>()
    val curposts:LiveData<List<Post>> = _curposts

    abstract val getPostsResponse: LiveData<Event<Resource<getPostResponse>>>
    private val _getPostResponse=MutableLiveData<Event<Resource<getPostResponse>>>()
    val getPostResponse:LiveData<Event<Resource<getPostResponse>>> = _getPostResponse
    init{
        clearposts()
    }

    fun getSelectedPost(postid:String, latitude:Double?, longitude:Double?, api: MainApi)
    {
        _getPostResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.getSelectedPost(postid,latitude,longitude,api)
            _getPostResponse.postValue(Event(result))
        }


    }
    fun setbeforeSize(size:Int)
    {
        _beforesize.postValue(size)
    }

    fun addposts(post:List<Post>)
    {
        posts.addAll(post)
        _curposts.postValue(posts.toList())
    }
    fun clearposts()
    {
        posts= mutableListOf()
        _curposts.postValue(posts)
    }

}