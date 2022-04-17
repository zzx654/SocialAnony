package com.example.appportfolio.api.responses

import com.example.appportfolio.data.entities.Post

data class getPostResponse (
    val resultCode:Int,
    val posts:List<Post>
    )