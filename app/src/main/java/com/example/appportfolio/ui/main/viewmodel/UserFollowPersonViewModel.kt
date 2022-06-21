package com.example.appportfolio.ui.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.responses.getpersonResponse
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.Resource
import com.example.appportfolio.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserFollowPersonViewModel@Inject constructor(private val repository: MainRepository,
                                                   private val dispatcher: CoroutineDispatcher = Dispatchers.Main
):BasePersonViewModel(repository,dispatcher) {
    private val _getsearchedPersonResponse= MutableLiveData<Event<Resource<getpersonResponse>>>()
    override val getsearchedPersonResponse: LiveData<Event<Resource<getpersonResponse>>>
        get() = _getsearchedPersonResponse

    private val _getfollowPersonResponse= MutableLiveData<Event<Resource<getpersonResponse>>>()
    val getfollowPersonResponse: LiveData<Event<Resource<getpersonResponse>>> = _getfollowPersonResponse

    fun getFollowingPersons(lastuserid: Int?,userid:Int?, api: MainApi) {
        _getfollowPersonResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.getFollowingPerson(lastuserid, userid,api)
            _getfollowPersonResponse.postValue(Event(result))
        }
    }
    fun getFollowerPersons(lastuserid: Int?,userid:Int?, api: MainApi) {
        _getfollowPersonResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.getFollowerPerson(lastuserid, userid,api)
            _getfollowPersonResponse.postValue(Event(result))
        }
    }
}