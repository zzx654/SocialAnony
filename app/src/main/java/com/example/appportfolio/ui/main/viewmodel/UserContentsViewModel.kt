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
class UserContentsViewModel@Inject constructor(
    private val repository: MainRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
):BasePostViewModel(repository, dispatcher) {

    private val _getPostsResponse = MutableLiveData<Event<Resource<getPostResponse>>>()

    override val getPostsResponse: LiveData<Event<Resource<getPostResponse>>>
        get() = _getPostsResponse

    private val _getUserImagesResponse = MutableLiveData<Event<Resource<getPostResponse>>>()
    val getUserImagesResponse: LiveData<Event<Resource<getPostResponse>>> = _getUserImagesResponse

    private val _getUserAudioResponse = MutableLiveData<Event<Resource<getPostResponse>>>()
    val getUserAudioResponse: LiveData<Event<Resource<getPostResponse>>> = _getUserAudioResponse

    private val _getUserVoteResponse = MutableLiveData<Event<Resource<getPostResponse>>>()
    val getUserVoteResponse: LiveData<Event<Resource<getPostResponse>>> = _getUserVoteResponse

    fun getUserContents(
        userid:Int,
        lastpostnum: Int?,
        lastpostdate: String?,
        latitude: Double?,
        longitude: Double?,
        type: String,
        api: MainApi
    ) {
        _getPostsResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.getuserContents(
                userid,
                lastpostnum,
                lastpostdate,
                latitude,
                longitude,
                20,
                type,
                api
            )
            _getPostsResponse.postValue(Event(result))
        }
    }

    fun getUserImages(
        userid:Int,
        lastpostnum: Int?,
        lastpostdate: String?,
        latitude: Double?,
        longitude: Double?,
        api: MainApi
    ) {
        _getUserImagesResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.getuserContents(
                userid,
                lastpostnum,
                lastpostdate,
                latitude,
                longitude,
                10,
                "IMAGE",
                api
            )
            _getUserImagesResponse.postValue(Event(result))
        }
    }

    fun getUserAudio(
        userid: Int,
        lastpostnum: Int?,
        lastpostdate: String?,
        latitude: Double?,
        longitude: Double?,
        api: MainApi
    ) {
        _getUserAudioResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.getuserContents(
                userid,
                lastpostnum,
                lastpostdate,
                latitude,
                longitude,
                10,
                "AUDIO",
                api
            )
            _getUserAudioResponse.postValue(Event(result))
        }
    }

    fun getUserVotes(
        userid: Int,
        lastpostnum: Int?,
        lastpostdate: String?,
        latitude: Double?,
        longitude: Double?,
        api: MainApi
    ) {
        _getUserVoteResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.getuserContents(
                userid,
                lastpostnum,
                lastpostdate,
                latitude,
                longitude,
                10,
                "VOTE",
                api
            )
            _getUserVoteResponse.postValue(Event(result))
        }
    }
}