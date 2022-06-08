package com.example.appportfolio.ui.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.responses.blocksResponse
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
class BlockViewModel@Inject constructor(
    private val repository: MainRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
): ViewModel() {

    private val _getBlockResponse= MutableLiveData<Event<Resource<blocksResponse>>>()
    val getBlockResponse: LiveData<Event<Resource<blocksResponse>>> = _getBlockResponse

    private val _deleteBlockResponse=MutableLiveData<Event<Resource<intResponse>>>()
    val deleteBlockResponse: LiveData<Event<Resource<intResponse>>> = _deleteBlockResponse

    fun getBlocks(api: MainApi)
    {
        _getBlockResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.getBlocks(api)
            _getBlockResponse.postValue(Event(result))
        }
    }
    fun deleteBlock(userid:Int,blockeduserid:Int,api:MainApi)
    {
        _deleteBlockResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.deleteBlock(userid,blockeduserid,api)
            _deleteBlockResponse.postValue(Event(result))
        }
    }
}