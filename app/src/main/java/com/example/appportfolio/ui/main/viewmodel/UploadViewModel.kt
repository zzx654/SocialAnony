package com.example.appportfolio.ui.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.responses.TagSearchResponse
import com.example.appportfolio.data.entities.Voteoption
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.Resource
import com.example.appportfolio.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(private val mainRepository: MainRepository,
                                          private val dispatcher: CoroutineDispatcher = Dispatchers.Main
): ViewModel() {
        private val _tagSearchResponse= MutableLiveData<Event<Resource<TagSearchResponse>>>()
        val tagSearchResponse: LiveData<Event<Resource<TagSearchResponse>>> = _tagSearchResponse
        private val _recordedPath= MutableLiveData<String>()
        val recordedPath: LiveData<String> = _recordedPath

        private val _anonymous= MutableLiveData<Boolean>()
        val anonymous: LiveData<Boolean> = _anonymous
    //위치정보 관련 변수들

        private val _gpsAllowed= MutableLiveData<Boolean>()
        val gpsAllowed: LiveData<Boolean> = _gpsAllowed


        private val _curPlatform= MutableLiveData<String?>()
        val curPlatform: LiveData<String?> = _curPlatform

        private val _curAccount= MutableLiveData<String?>()
        val curAccount: LiveData<String?> = _curAccount

        private val _voteoptions= MutableLiveData<List<Voteoption>?>()
         val voteoptions: LiveData<List<Voteoption>?> = _voteoptions

        fun setvoteoptions(voteoptions:List<Voteoption>?)
        {
            _voteoptions.postValue(voteoptions)
        }

        fun setAnonymous(ischeckd:Boolean)
        {
            _anonymous.postValue(ischeckd)
        }
        fun setGpsAllowed(isAllowed:Boolean)
        {
            _gpsAllowed.postValue(isAllowed)
        }
        fun setRecordedPath(path:String)
        {
            _recordedPath.postValue(path)
        }
        fun searchTag(tagname:String,api: MainApi)
        {
            _tagSearchResponse.postValue(Event(Resource.Loading()))
            viewModelScope.launch(dispatcher) {
                val result=mainRepository.searchTag(tagname,api)
                _tagSearchResponse.postValue(Event(result))
            }
        }
}