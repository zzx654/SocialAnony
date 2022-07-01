package com.example.appportfolio.data.entities
data class SendData(
    val roomid:String,
    val senderid:Int,
    val receiverid:Int,
    val content:String,
    val type:String
)