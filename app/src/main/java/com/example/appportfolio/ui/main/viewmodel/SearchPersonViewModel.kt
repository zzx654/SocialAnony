package com.example.appportfolio.ui.main.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.responses.getpersonResponse
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.Resource
import com.example.appportfolio.repositories.MainRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchPersonViewModel@ViewModelInject constructor(
    private val repository: MainRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
):BasePersonViewModel(repository,dispatcher) {
    private val _getsearchedPersonResponse= MutableLiveData<Event<Resource<getpersonResponse>>>()
    override val getsearchedPersonResponse: LiveData<Event<Resource<getpersonResponse>>>
        get() = _getsearchedPersonResponse

    override fun getsearchedPersons(lastuserid: Int?, nickname: String, api: MainApi) {
        _getsearchedPersonResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.getSearchedPerson(lastuserid,nickname, api)
            _getsearchedPersonResponse.postValue(Event(result))
        }
    }
}