package com.example.appportfolio.ui.main.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.responses.TagSearchResponse
import com.example.appportfolio.api.responses.intResponse
import com.example.appportfolio.data.entities.TagResult
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.Resource
import com.example.appportfolio.repositories.MainRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TagViewModel@ViewModelInject constructor(
    private val repository: MainRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
): ViewModel() {

    private val _isLiked=MutableLiveData<Int>()
    val isLiked:LiveData<Int>
    get() = _isLiked
    private val _toggleTagResponse = MutableLiveData<Event<Resource<TagResult>>>()
    val toggleTagResponse:LiveData<Event<Resource<TagResult>>> = _toggleTagResponse
    private val _tagSearchResponse= MutableLiveData<Event<Resource<TagSearchResponse>>>()
    val tagSearchResponse: LiveData<Event<Resource<TagSearchResponse>>> = _tagSearchResponse

    private val _populartagResponse= MutableLiveData<Event<Resource<TagSearchResponse>>>()
    val populartagResponse: LiveData<Event<Resource<TagSearchResponse>>> = _populartagResponse

    private val _favoritetagResponse= MutableLiveData<Event<Resource<TagSearchResponse>>>()
    val favoritetagResponse: LiveData<Event<Resource<TagSearchResponse>>> = _favoritetagResponse

    private val _getisLikedResponse = MutableLiveData<Event<Resource<intResponse>>>()
    val getisLikedResponse:LiveData<Event<Resource<intResponse>>> = _getisLikedResponse

    fun setisLiked(isliked: Int)
    {
        _isLiked.value=isliked
    }
    fun toggleLikeTag(tagname: String,count:Int,isLiked:Int,api: MainApi){
        _toggleTagResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.toggleLikeTag(tagname,count,isLiked,api)
            _toggleTagResponse.postValue(Event(result))
        }
    }
    fun getTagLiked(tagname: String,api: MainApi){
        _getisLikedResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.getTagLiked(tagname, api)
            _getisLikedResponse.postValue(Event(result))
        }
    }
    fun getSearchedTag(tagname:String,api: MainApi)
    {
        _tagSearchResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.getSearchedTag(tagname, api)
            _tagSearchResponse.postValue(Event(result))
        }
    }
    fun getPopularTag(api:MainApi)
    {
        _populartagResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.getPopularTag(api)
            _populartagResponse.postValue(Event(result))
        }
    }
    fun getFavoriteTag(api: MainApi)
    {
        _favoritetagResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.getFavoriteTag(api)
            _favoritetagResponse.postValue(Event(result))
        }
    }

}