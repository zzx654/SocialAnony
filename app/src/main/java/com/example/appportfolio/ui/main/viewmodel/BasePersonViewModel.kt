package com.example.appportfolio.ui.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.responses.getPostResponse
import com.example.appportfolio.api.responses.getpersonResponse
import com.example.appportfolio.api.responses.intResponse
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.Resource
import com.example.appportfolio.repositories.MainRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BasePersonViewModel(private val repository: MainRepository,
                          private val dispatcher: CoroutineDispatcher = Dispatchers.Main
): ViewModel() {
    abstract val getsearchedPersonResponse: LiveData<Event<Resource<getpersonResponse>>>
    abstract fun getsearchedPersons(lastuserid:Int?,nickname:String,api: MainApi)

    private val _togglefollowResponse= MutableLiveData<Event<Resource<intResponse>>>()
    val togglefollowResponse: LiveData<Event<Resource<intResponse>>> = _togglefollowResponse

    private val _checkuserResponse= MutableLiveData<Event<Resource<intResponse>>>()
    val checkuserResponse: LiveData<Event<Resource<intResponse>>> = _checkuserResponse

    private val _curtoggle = MutableLiveData<Int>()
    val curtoggle:LiveData<Int> = _curtoggle

    fun toggleFollow(userid:Int,following:Int,api:MainApi){

        _togglefollowResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.toggleFollow(userid,following,api)
            _togglefollowResponse.postValue(Event(result))
        }
    }
    fun setcurtoggle(curtoggleuser:Int)
    {
        _curtoggle.postValue(curtoggleuser)
    }

    fun checkuser(userid: Int,api: MainApi){
        _checkuserResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.checkuser(userid,api)
            _checkuserResponse.postValue(Event(result))
        }
    }

}