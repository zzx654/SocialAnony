package com.example.appportfolio.ui.main.viewmodel

import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appportfolio.api.build.MainApi

import com.example.appportfolio.api.responses.intResponse
import com.example.appportfolio.api.responses.uploadImageResponse
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.Resource
import com.example.appportfolio.repositories.MainRepository
import kotlinx.coroutines.*
import okhttp3.MultipartBody

class ProfileEditViewModel @ViewModelInject constructor(private val mainRepository: MainRepository,
                                                        private val dispatcher: CoroutineDispatcher = Dispatchers.Main
): ViewModel() {
    private val _curImageUri= MutableLiveData<Uri>()
    val curImageUri: LiveData<Uri> = _curImageUri
    private val _uploadimgResponse=MutableLiveData<Event<Resource<uploadImageResponse>>>()
    val uploadimgResponse:LiveData<Event<Resource<uploadImageResponse>>> = _uploadimgResponse
    private val _profileeditResponse=MutableLiveData<Event<Resource<intResponse>>>()
    val profileeditResponse:LiveData<Event<Resource<intResponse>>> = _profileeditResponse

    private val _checknickResponse=MutableLiveData<Event<Resource<intResponse>>>()
    val checknickResponse:LiveData<Event<Resource<intResponse>>> = _checknickResponse
    fun setCurImageUri(uri:Uri){
        _curImageUri.postValue(uri)
    }
    fun checknick(
        nickname: String,
        api: MainApi
    ){

        _checknickResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=mainRepository.checknick(nickname, api)
            _checknickResponse.postValue(Event(result))
        }
    }
    fun uploadprofileimg(
        image:MultipartBody.Part,
        api: MainApi
    ){
        _uploadimgResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=mainRepository.uploadprofileimg(image, api)
            _uploadimgResponse.postValue(Event(result))
        }
    }
    fun editprofile(
        imageuri:String?,
        nickname:String,
        api:MainApi
    ){
        _profileeditResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=mainRepository.editprofile( imageuri, nickname, api)
            _profileeditResponse.postValue(Event(result))
        }
    }

}