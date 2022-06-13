package com.example.appportfolio.ui.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appportfolio.data.entities.ToggleState

class applyFollowViewModel: ViewModel() {
    private val _curtoggling = MutableLiveData<ToggleState>()

    val curtoggling: LiveData<ToggleState> = _curtoggling


    fun setcurtoggle(curtoggleuser:Int,toggle:Int)
    {
        _curtoggling.postValue(ToggleState(curtoggleuser,toggle))
    }
}