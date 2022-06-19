package com.example.appportfolio.ui.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appportfolio.data.entities.ToggleState

class applyFollowViewModel: ViewModel() {
    private val _curtoggling = MutableLiveData<List<ToggleState>>()

    val curtoggling: LiveData<List<ToggleState>> = _curtoggling


    init{
        _curtoggling.postValue(listOf())
    }
    fun setcurtoggle(curtoggleuser:Int,toggle:Int)
    {
        var oldlist=curtoggling.value!!.toMutableList()
        oldlist=oldlist.filter { toggleState -> toggleState.toggleuser!=curtoggleuser  }.toMutableList()
        _curtoggling.postValue(oldlist+ToggleState(curtoggleuser,toggle))
    }
}