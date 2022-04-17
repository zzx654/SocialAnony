package com.example.appportfolio.api.responses

import com.example.appportfolio.data.entities.TagResult


data class TagSearchResponse(
    val resultCode:Int,
    val tags:List<TagResult>
)