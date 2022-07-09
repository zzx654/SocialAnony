package com.example.appportfolio.ui.main.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appportfolio.api.build.LocApi
import com.example.appportfolio.api.responses.SearchLocResponse
import com.example.appportfolio.data.entities.LocationLatLngEntity
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.Resource
import com.example.appportfolio.repositories.LocRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class LocViewModel@Inject constructor(private val locRepository: LocRepository, private val dispatcher: CoroutineDispatcher = Dispatchers.Main): ViewModel(){

    private val _curloc=MutableLiveData<LocationLatLngEntity>()
    val curloc: LiveData<LocationLatLngEntity> = _curloc
    private val _getLocResponse = MutableLiveData<Event<Resource<Response<SearchLocResponse>>>>()
    val getLocResponse: LiveData<Event<Resource<Response<SearchLocResponse>>>> = _getLocResponse



    fun setLoc(latlng:LocationLatLngEntity)
    {
        _curloc.postValue(latlng)
    }
    fun getSearchLocation(keyword:String,page:Int,api:LocApi){
        _getLocResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=locRepository.getSearchLocation(keyword, page, api)
            _getLocResponse.postValue(Event(result))
        }
    }

}