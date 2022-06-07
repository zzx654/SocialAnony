package com.example.appportfolio.ui.main.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.responses.commentResponse
import com.example.appportfolio.api.responses.intResponse
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.Resource
import com.example.appportfolio.repositories.MainRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReplyViewModel@ViewModelInject constructor(
    private val repository: MainRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
): BaseCommentViewModel(repository, dispatcher) {

    private val _postCommentResponse=MutableLiveData<Event<Resource<commentResponse>>>()
    override val postCommentResponse: LiveData<Event<Resource<commentResponse>>>
        get() = _postCommentResponse

    private val _getCommentResponse= MutableLiveData<Event<Resource<commentResponse>>>()
    override val getCommentResponse:LiveData<Event<Resource<commentResponse>>>
        get()= _getCommentResponse

    private val _deletereplyResponse=MutableLiveData<Event<Resource<intResponse>>>()
    val deletereplyResponse:LiveData<Event<Resource<intResponse>>> = _deletereplyResponse

    fun deletereply(commentid: Int,api:MainApi)
    {
        _deletereplyResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.deletereply(commentid, api)
            _deletereplyResponse.postValue(Event(result))
        }
    }
    fun postReply(ref:Int,postid: String,commentid: Int,time: String,anonymous: String,text: String,postuserid:Int,commentuserid:Int,api: MainApi)
    {
        _postCommentResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.postReply(ref,postid,commentid,time,anonymous, text,postuserid,commentuserid, api)
            _postCommentResponse.postValue(Event(result))
        }

    }

    fun getReply(ref: Int,commentid:Int?,time:String?,api: MainApi)
    {
        _getCommentResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch (dispatcher){
            val result=repository.getReply(ref,commentid,time, api)
            _getCommentResponse.postValue(Event(result))
        }


    }
}