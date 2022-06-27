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
class hotContentsViewModel@Inject constructor(
    private val repository: MainRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
):BasePostViewModel(repository, dispatcher) {

    private val _getPostsResponse = MutableLiveData<Event<Resource<getPostResponse>>>()

    override val getPostsResponse: LiveData<Event<Resource<getPostResponse>>>
        get() = _getPostsResponse

    private val _getHotImagesResponse = MutableLiveData<Event<Resource<getPostResponse>>>()
    val getHotImagesResponse: LiveData<Event<Resource<getPostResponse>>> = _getHotImagesResponse

    private val _getHotAudioResponse = MutableLiveData<Event<Resource<getPostResponse>>>()
    val getHotAudioResponse: LiveData<Event<Resource<getPostResponse>>> = _getHotAudioResponse

    private val _getHotVoteResponse = MutableLiveData<Event<Resource<getPostResponse>>>()
    val getHotVoteResponse: LiveData<Event<Resource<getPostResponse>>> = _getHotVoteResponse

    fun getHotContents(
        lastpostnum: Int?,
        lastposthot: Int?,
        latitude: Double?,
        longitude: Double?,
        limit: Int,
        type: String,
        api: MainApi
    ) {
        _getPostsResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.getHotContents(
                lastpostnum,
                lastposthot,
                latitude,
                longitude,
                limit,
                type,
                api
            )
            _getPostsResponse.postValue(Event(result))
        }
    }

    fun getHotImages(
        lastpostnum: Int?,
        lastposthot: Int?,
        latitude: Double?,
        longitude: Double?,
        api: MainApi
    ) {
        _getHotImagesResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.getHotContents(
                lastpostnum,
                lastposthot,
                latitude,
                longitude,
                10,
                "IMAGE",
                api
            )
            _getHotImagesResponse.postValue(Event(result))
        }
    }

    fun getHotAudio(
        lastpostnum: Int?,
        lastposthot: Int?,
        latitude: Double?,
        longitude: Double?,
        api: MainApi
    ) {
        _getHotAudioResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.getHotContents(
                lastpostnum,
                lastposthot,
                latitude,
                longitude,
                10,
                "AUDIO",
                api
            )
            _getHotAudioResponse.postValue(Event(result))
        }
    }

    fun getHotVotes(
        lastpostnum: Int?,
        lastposthot: Int?,
        latitude: Double?,
        longitude: Double?,
        api: MainApi
    ) {
        _getHotVoteResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.getHotContents(
                lastpostnum,
                lastposthot,
                latitude,
                longitude,
                10,
                "VOTE",
                api
            )
            _getHotVoteResponse.postValue(Event(result))
        }
    }
}