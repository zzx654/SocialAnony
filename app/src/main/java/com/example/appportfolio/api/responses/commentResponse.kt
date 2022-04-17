package com.example.appportfolio.api.responses

import com.example.appportfolio.data.entities.Comment

data class commentResponse(
    val resultCode:Int,
    val comments:List<Comment>
)