package com.example.appportfolio.data.entities

data class postcontent(
    val postid:String,
    val account:String,
    val platform:String,
    val anonymous:String,
    val text:String,
    val tags:String?,
    val latitude:Double?,
    val longitude:Double?,
    val date:String,
    val image:String,
    val audio:String
)