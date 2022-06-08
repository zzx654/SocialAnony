package com.example.appportfolio.ui.main.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.responses.*
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.Resource
import com.example.appportfolio.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val repository: MainRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
): BaseCommentViewModel(repository, dispatcher) {

    private val _postCommentResponse=MutableLiveData<Event<Resource<commentResponse>>>()
    override val postCommentResponse: LiveData<Event<Resource<commentResponse>>>
        get() = _postCommentResponse
    private val _getCommentResponse=MutableLiveData<Event<Resource<commentResponse>>>()
    override val getCommentResponse:LiveData<Event<Resource<commentResponse>>>
        get()= _getCommentResponse

    fun postComment(postuserid:Int?,postid: String,time: String,anonymous: String,text: String,api: MainApi)
    {
        _postCommentResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.postComment(postuserid,postid,time,anonymous, text, api)
            _postCommentResponse.postValue(Event(result))
        }
    }
    fun getComment(commentid:Int?,postid:String,time:String?,api: MainApi)
    {
        _getCommentResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch (dispatcher){
            val result=repository.getComment(commentid,postid,time, api)
            _getCommentResponse.postValue(Event(result))
        }
    }
    fun getHotComment(commentid:Int?,postid:String,likecount:Int?,api: MainApi)
    {
        _getCommentResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch (dispatcher){
            val result=repository.getHotComment(commentid,postid,likecount, api)
            _getCommentResponse.postValue(Event(result))
        }
    }
}