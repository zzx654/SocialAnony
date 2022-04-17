package com.example.appportfolio.api.responses

import com.example.appportfolio.data.entities.polloption

data class getpolloptionResponse (
    val resultCode:Int,
    val polloptions:List<polloption>
        )