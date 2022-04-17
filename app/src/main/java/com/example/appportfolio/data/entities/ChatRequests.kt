package com.example.appportfolio.data.entities

data class ChatRequests(
    val roomid:String,
    val organizer:Int,
    val participant:Int,
    val joined:Int,
    val nickname:String,
    val gender:String,
    val profileimage:String
)