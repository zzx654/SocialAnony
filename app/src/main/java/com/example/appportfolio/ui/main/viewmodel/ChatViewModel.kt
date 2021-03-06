package com.example.appportfolio.ui.main.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.responses.*
import com.example.appportfolio.data.entities.ChatData
import com.example.appportfolio.data.entities.ChatRequests
import com.example.appportfolio.data.entities.Chatroom
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.Resource
import com.example.appportfolio.repositories.ChatRepository
import com.example.appportfolio.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class ChatViewModel@Inject constructor(
    private val repository: MainRepository,
    private val chatRepository: ChatRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
): ViewModel() {
    private val _uploadimgResponse=MutableLiveData<Event<Resource<uploadImageResponse>>>()
    val uploadimgResponse:LiveData<Event<Resource<uploadImageResponse>>> = _uploadimgResponse

    private val _mychatRequests= MutableLiveData<List<ChatRequests>>()
    val mychatRequests: LiveData<List<ChatRequests>> = _mychatRequests

    private val _mychats= MutableLiveData<List<Chatroom>>()
    val mychats: LiveData<List<Chatroom>> = _mychats

    private val _acceptchatResponse= MutableLiveData<Event<Resource<getchatrequestsResponse>>>()
    val acceptchatResponse: LiveData<Event<Resource<getchatrequestsResponse>>> = _acceptchatResponse

    private val _refusechatResponse= MutableLiveData<Event<Resource<getchatrequestsResponse>>>()
    val refusechatResponse: LiveData<Event<Resource<getchatrequestsResponse>>> = _refusechatResponse

    private val _getchatrequestsResponse= MutableLiveData<Event<Resource<getchatrequestsResponse>>>()
    val getchatrequestsResponse: LiveData<Event<Resource<getchatrequestsResponse>>> = _getchatrequestsResponse


    private val _getroomProfilesResponse= MutableLiveData<Event<Resource<getRoomProfilesResponse>>>()
    val getroomProfilesResponse: LiveData<Event<Resource<getRoomProfilesResponse>>> = _getroomProfilesResponse

    private val _getprofileResponse= MutableLiveData<Event<Resource<getprofileResponse>>>()
    val getprofileResponse: LiveData<Event<Resource<getprofileResponse>>> = _getprofileResponse

    private val _blockuserResponse= MutableLiveData<Event<Resource<intResponse>>>()
    val blockuserResponse: LiveData<Event<Resource<intResponse>>> = _blockuserResponse
    init{
        _mychatRequests.postValue(listOf())
        _mychats.postValue(listOf())
    }
    fun blockchatuser(anonymous:Boolean,blockuserid:Int,api:MainApi){

        _blockuserResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.blockchatuser(anonymous,blockuserid,api)
            _blockuserResponse.postValue(Event(result))
        }
    }
    fun insertChat(chatdata:ChatData)=viewModelScope.launch (Dispatchers.IO){
            chatRepository.insertChat(chatdata)
    }
    fun readChats(roomid:String)=viewModelScope.launch(Dispatchers.IO) {
        chatRepository.readChats(roomid)
    }
    fun deleteroom(roomid:String)=viewModelScope.launch(Dispatchers.IO){
        chatRepository.deleteroom(roomid)
    }

    fun deleteAllrooms()=viewModelScope.launch(Dispatchers.IO){
        chatRepository.deleteAllroom()
    }
    fun uploadimg(
        image: MultipartBody.Part,
        api: MainApi
    ){
        _uploadimgResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.uploadimg(image, api)
            _uploadimgResponse.postValue(Event(result))
        }
    }
    fun getRoomProfiles(api:MainApi){
        _getroomProfilesResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.getChatProfiles(api)
            _getroomProfilesResponse.postValue(Event(result))
        }
    }
    fun getuserprofile(userid: Int,api: MainApi) {
        _getprofileResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.getuserprofile(userid, api)
            _getprofileResponse.postValue(Event(result))
        }
    }
    fun getchatrequests(api: MainApi)
    {
        _getchatrequestsResponse.postValue(Event(Resource.Loading()))

        viewModelScope.launch(dispatcher) {
            val result=repository.getchatrequests(api)
            _getchatrequestsResponse.postValue(Event(result))
        }
    }
    fun getAllChats() = chatRepository.getAllChats()

    fun getOpponentChat(roomid:String,myid:Int) = chatRepository.getOpponentChat(roomid,myid)
    fun getLastChats(roomid: String,lastid:Int) = chatRepository.getLastChats(roomid,lastid)
    fun getAddedChats(roomid: String) = chatRepository.getAddedChat(roomid)

    fun loadimages(roomid: String) = chatRepository.loadimages(roomid)
    fun loadchatContents(roomid:String) = chatRepository.loadchatContents(roomid)

    fun loadbeforechatContents(roomid:String,id:Int) = chatRepository.loadbeforechatContents(roomid,id)

    fun refusechat(roomid:String,userid:Int,api: MainApi)
    {
        _refusechatResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.refusechat(roomid,userid,api)
            _refusechatResponse.postValue(Event(result))
        }
    }
    fun acceptchat(roomid: String,organizer: Int,participant: Int,api: MainApi)
    {
        _acceptchatResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=repository.acceptchat(roomid,organizer,participant,api)

            _acceptchatResponse.postValue(Event(result))
        }
    }
    fun setChatRequests(requests:List<ChatRequests>)
    {
        _mychatRequests.postValue(requests)
    }
    fun setChats(chatrooms:List<Chatroom>)
    {
        _mychats.postValue(chatrooms)
    }

}