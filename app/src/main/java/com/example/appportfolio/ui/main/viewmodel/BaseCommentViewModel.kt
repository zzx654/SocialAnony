package com.example.appportfolio.ui.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.responses.NicknameResponse
import com.example.appportfolio.api.responses.commentResponse
import com.example.appportfolio.api.responses.intResponse
import com.example.appportfolio.data.entities.Comment
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.Resource
import com.example.appportfolio.repositories.MainRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


abstract class BaseCommentViewModel( private val repository: MainRepository,
private val dispatcher: CoroutineDispatcher = Dispatchers.Main
): ViewModel() {
    //var comments:MutableList<Comment> = mutableListOf()

    //private val _beforesize= MutableLiveData<Event<Resource<Int>>>()
    //val beforesize: LiveData<Event<Resource<Int>>> = _beforesize
    //private  val _curcomments= MutableLiveData<Event<Resource<List<Comment>>>>()
    //val curcomments:LiveData<Event<Resource<List<Comment>>>> = _curcomments

    abstract val getCommentResponse:LiveData<Event<Resource<commentResponse>>>

    private val _toggleCommentResponse=MutableLiveData<Event<Resource<intResponse>>>()
    val toggleCommentResponse:LiveData<Event<Resource<intResponse>>> = _toggleCommentResponse

    private val _deletecommentResponse=MutableLiveData<Event<Resource<intResponse>>>()
    val deletecommentResponse:LiveData<Event<Resource<intResponse>>> = _deletecommentResponse

    abstract val postCommentResponse:LiveData<Event<Resource<intResponse>>>

    private val _anonymousnick=MutableLiveData<String>()
    val anonymousnick:LiveData<String> = _anonymousnick

    private val _getAnonymousResponse= MutableLiveData<Event<Resource<NicknameResponse>>>()
    val getAnonymousResponse: LiveData<Event<Resource<NicknameResponse>>> = _getAnonymousResponse

    private val _checkSelectedCommentResponse=MutableLiveData<Event<Resource<commentResponse>>>()
    val checkSelectedCommentResponse:LiveData<Event<Resource<commentResponse>>> = _checkSelectedCommentResponse

    fun deletecomment(ref:Int,api:MainApi)
    {
        _deletecommentResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.deletecomment(ref,api)
            _deletecommentResponse.postValue(Event(result))
        }
    }
    fun toggleComment(rootcommentid:Int?,commentuserid:Int?,depth:Int?,time:String?,postid:String?,commentid: Int,isLiked: Int,api: MainApi)
    {
        _toggleCommentResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.toggleComment(rootcommentid,commentuserid,depth,time,postid,commentid, isLiked, api)
            _toggleCommentResponse.postValue(Event(result))
        }

    }

    fun setAnony(nickname:String)
    {
        _anonymousnick.postValue(nickname)
    }
    fun getAnonymous(postid:String,api: MainApi)
    {
        _getAnonymousResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch (dispatcher){
            val result=repository.getAnonymous(postid, api)
            _getAnonymousResponse.postValue(Event(result))
        }
    }
    fun checkSelectedComment(postuserid:Int,commentuserid:Int,commentid:Int,postid: String,api:MainApi)
    {
        _checkSelectedCommentResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.checkSelectedComment(postuserid,commentuserid,commentid,postid, api)
            _checkSelectedCommentResponse.postValue(Event(result))
        }
    }
    //fun setbeforeSize(size:Int)
    //{
     //   _beforesize.postValue(Event(Resource.Success(size)))
    //}
    //fun addcomments(post:List<Comment>)
    //{
     //   comments.addAll(post)
      //  _curcomments.postValue(Event(Resource.Success(comments.toList())))
    //}
    //fun clearcomments()
    //{
     //   comments= mutableListOf()
      //  _curcomments.postValue(Event(Resource.Success(comments)))
    //}
}