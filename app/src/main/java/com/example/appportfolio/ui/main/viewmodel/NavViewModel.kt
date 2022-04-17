package com.example.appportfolio.ui.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appportfolio.other.Constants.TAG_HOME

class NavViewModel: ViewModel() {
    private val _destinationFragment= MutableLiveData<String>()
    val destinationFragment: LiveData<String> = _destinationFragment

    init{
        _destinationFragment.postValue(TAG_HOME)
    }
    fun setFrag(fragment:String)
    {
        _destinationFragment.postValue(fragment)
    }
}