package com.example.appportfolio.data.entities

data class Chatroom (
    val userid:Int?,
    var profileimage:String?,
    val gender:String?,
    var nickname:String?,
    val ismy:Int,
    val senderid:Int,
    val roomid:String,
    val date:String,
    val type:String,
    val content:String?,
    var isread:Int

    )