package com.example.appportfolio.api.responses

import com.example.appportfolio.data.entities.Noti

data class getNotiResponse (
    val resultCode:Int,
    val notis:List<Noti>
        )