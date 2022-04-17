package com.example.appportfolio.data.entities

data class Noti (
    val notiid:Int,
    val type:Int,
    val text:String,
    val date:String,
    val postid:String,
    val commentid:Int?,
    var isread:Int

        )